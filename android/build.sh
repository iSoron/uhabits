#!/bin/bash
# Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
# This file is part of Loop Habit Tracker.
#
# Loop Habit Tracker is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by the
# Free Software Foundation, either version 3 of the License, or (at your
# option) any later version.
#
# Loop Habit Tracker is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along
# with this program. If not, see <http://www.gnu.org/licenses/>.

cd "$(dirname "$0")"

ADB="${ANDROID_HOME}/platform-tools/adb"
EMULATOR="${ANDROID_HOME}/tools/emulator"
GRADLE="./gradlew --stacktrace"
PACKAGE_NAME=org.isoron.uhabits
OUTPUTS_DIR=uhabits-android/build/outputs
VERSION=$(cat gradle.properties | grep VERSION_NAME | sed -e 's/.*=//g;s/ //g')

if [ ! -f "${ANDROID_HOME}/platform-tools/adb" ]; then
	echo "Error: ANDROID_HOME is not set correctly"
	exit 1
fi

log_error() {
	if [ ! -z "$TEAMCITY_VERSION" ]; then
		echo "###teamcity[progressMessage '$1']"
	else
		local COLOR='\033[1;31m'
		local NC='\033[0m'
		echo -e "$COLOR>>> $1 $NC"
	fi
}

log_info() {
	if [ ! -z "$TEAMCITY_VERSION" ]; then
		echo "###teamcity[progressMessage '$1']"
	else
		local COLOR='\033[1;32m'
		local NC='\033[0m'
		echo -e "$COLOR>>> $1 $NC"
	fi
}

fail() {
	if [ ! -z ${AVD_NAME} ]; then
		stop_emulator
		stop_gradle_daemon
	fi
	log_error "BUILD FAILED"
	exit 1
}

start_emulator() {
	log_info "Starting emulator ($AVD_NAME)"
	$EMULATOR -avd ${AVD_NAME} -port ${AVD_SERIAL} -no-audio -no-window &
	$ADB wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
}

stop_emulator() {
	log_info "Stopping emulator"
	$ADB emu kill
}

stop_gradle_daemon() {
	log_info "Stopping gradle daemon"
	$GRADLE --stop
}

run_adb_as_root() {
	log_info "Running adb as root"
	$ADB root
}

build_apk() {
	log_info "Removing old APKs..."
	rm -vf build/*.apk

	if [ ! -z $RELEASE ]; then
		if [ -z "$LOOP_KEY_FILE" -o -z "$LOOP_STORE_PASSWORD" -o -z "$LOOP_KEY_ALIAS" -o -z "$LOOP_KEY_PASSWORD" ]; then
			log_error "Environment variables LOOP_KEY_FILE, LOOP_KEY_ALIAS, LOOP_KEY_PASSWORD and LOOP_STORE_PASSWORD must be defined"
			exit 1
		fi
		log_info "Building release APK"
		./gradlew assembleRelease \
			-Pandroid.injected.signing.store.file=$LOOP_KEY_FILE \
			-Pandroid.injected.signing.store.password=$LOOP_STORE_PASSWORD \
			-Pandroid.injected.signing.key.alias=$LOOP_KEY_ALIAS \
			-Pandroid.injected.signing.key.password=$LOOP_KEY_PASSWORD || fail
		cp -v uhabits-android/build/outputs/apk/release/uhabits-android-release.apk build/loop-$VERSION-release.apk
	fi

	log_info "Building debug APK"
	./gradlew assembleDebug || fail
	cp -v uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk build/loop-$VERSION-debug.apk
}

build_instrumentation_apk() {
	log_info "Building instrumentation APK"
	if [ ! -z $RELEASE ]; then
		$GRADLE assembleAndroidTest  \
			-Pandroid.injected.signing.store.file=$LOOP_KEY_FILE \
			-Pandroid.injected.signing.store.password=$LOOP_STORE_PASSWORD \
			-Pandroid.injected.signing.key.alias=$LOOP_KEY_ALIAS \
			-Pandroid.injected.signing.key.password=$LOOP_KEY_PASSWORD || fail
	else
		$GRADLE assembleAndroidTest || fail
	fi
}

clean_output_dir() {
	log_info "Cleaning output directory"
	rm -rf ${OUTPUTS_DIR}
	mkdir -p ${OUTPUTS_DIR}
}

uninstall_apk() {
	log_info "Uninstalling existing APK"
	$ADB uninstall ${PACKAGE_NAME}
}

install_test_butler() {
	log_info "Installing Test Butler"
	$ADB uninstall com.linkedin.android.testbutler
	$ADB install tools/test-butler-app-2.0.2.apk
}

install_apk() {
	log_info "Installing APK"
	if [ ! -z $RELEASE ]; then
		$ADB install -r ${OUTPUTS_DIR}/apk/release/uhabits-android-release.apk || fail
	else
		$ADB install -t -r ${OUTPUTS_DIR}/apk/debug/uhabits-android-debug.apk || fail
	fi
}

install_test_apk() {
	log_info "Uninstalling existing test APK"
	$ADB uninstall ${PACKAGE_NAME}.test

	log_info "Installing test APK"
	$ADB install -r ${OUTPUTS_DIR}/apk/androidTest/debug/uhabits-android-debug-androidTest.apk || fail
}

run_instrumented_tests() {
	SIZE=$1
	log_info "Running instrumented tests"
	$ADB shell am instrument \
		-r -e coverage true -e size $SIZE \
		-w ${PACKAGE_NAME}.test/android.support.test.runner.AndroidJUnitRunner \
		| tee ${OUTPUTS_DIR}/instrument.txt

	if grep FAILURES $OUTPUTS_DIR/instrument.txt; then
		log_error "Some instrumented tests failed"
		fetch_images
		fetch_logcat
		exit 1
	fi

	#mkdir -p ${OUTPUTS_DIR}/code-coverage/connected/
	#$ADB pull /data/user/0/${PACKAGE_NAME}/files/coverage.ec \
	#	${OUTPUTS_DIR}/code-coverage/connected/ \
	#	|| log_error "COVERAGE REPORT NOT AVAILABLE"
}

parse_instrumentation_results() {
	log_info "Parsing instrumented test results"
	java -jar tools/automator-log-converter-1.5.0.jar ${OUTPUTS_DIR}/instrument.txt || fail
}

generate_coverage_badge() {
	log_info "Generating code coverage badge"
	CORE_REPORT=uhabits-core/build/reports/jacoco/test/jacocoTestReport.xml
	rm -f ${OUTPUTS_DIR}/coverage-badge.svg
	python3 tools/coverage-badge/badge.py -i $CORE_REPORT -o ${OUTPUTS_DIR}/coverage-badge
}

fetch_logcat() {
	log_info "Fetching logcat"
	$ADB logcat -d > ${OUTPUTS_DIR}/logcat.txt
}

run_jvm_tests() {
	log_info "Running JVM tests"
	if [ ! -z $RELEASE ]; then
		$GRADLE testReleaseUnitTest :uhabits-core:check || fail
	else
		$GRADLE testDebugUnitTest :uhabits-core:check || fail
	fi
}

uninstall_test_apk() {
	log_info "Uninstalling test APK"
	$ADB uninstall ${PACKAGE_NAME}.test
}

fetch_images() {
	log_info "Fetching images"
	rm -rf $OUTPUTS_DIR/test-screenshots
	$ADB pull /sdcard/Android/data/${PACKAGE_NAME}/files/test-screenshots/ $OUTPUTS_DIR
	$ADB shell rm -r /sdcard/Android/data/${PACKAGE_NAME}/files/test-screenshots/ 
}

accept_images() {
	find tmp/test-screenshots -name '*.expected*' -delete
	rsync -av tmp/test-screenshots/ uhabits-android/src/androidTest/assets/
}

run_tests() {
	SIZE=$1
	run_adb_as_root
	install_test_butler
	uninstall_apk
	install_apk
	install_test_apk
	run_instrumented_tests $SIZE
	parse_instrumentation_results
	fetch_logcat
	uninstall_test_apk
}

parse_opts() {
	OPTS=`getopt -o ur --long uninstall-first,release -n 'build.sh' -- "$@"`
	if [ $? != 0 ] ; then exit 1; fi
	eval set -- "$OPTS" 

	while true; do
		case "$1" in
			-u | --uninstall-first ) UNINSTALL_FIRST=1; shift ;;
			-r | --release ) RELEASE=1; shift ;;
			* ) break ;;
		esac
	done	
}

case "$1" in
	build)
		shift; parse_opts $*

		build_apk
		build_instrumentation_apk
		run_jvm_tests
		#generate_coverage_badge
		;;

	ci-tests)
		if [ -z $3 ]; then
			cat <<- END
				Usage: $0 ci-tests AVD_NAME AVD_SERIAL [options]

				Parameters:
				    AVD_NAME    name of the virtual android device to start
				    AVD_SERIAL  adb port to use (e.g. 5560)

				Options:
				    -u  --uninstall-first  Uninstall existing APK first
				    -r  --release          Test release APK, instead of debug
			END
			exit 1
		fi

		shift; AVD_NAME=$1
		shift; AVD_SERIAL=$1
		shift; parse_opts $*
		ADB="${ADB} -s emulator-${AVD_SERIAL}"

		start_emulator
		run_tests medium
		stop_emulator
		stop_gradle_daemon
		;;

	medium-tests)
		shift; parse_opts $*
		run_tests medium
		;;

	large-tests)
		shift; parse_opts $*
		run_tests large
 		;;

	fetch-images)
		fetch_images
		;;

	accept-images)
		accept_images
		;;

	install)
		shift; parse_opts $*
		build_apk
		install_apk
		;;

	*)
		cat <<- END
			Usage: $0 <command> [options]
			Builds, installs and tests Loop Habit Tracker

			Commands:
			    ci-tests      Start emulator silently, run tests then kill emulator
			    local-tests   Run all tests on connected device
			    install       Install app on connected device
			    fetch-images  Fetches failed view test images from device
			    accept-images Copies fetched images to corresponding assets folder

			Options:
			    -r  --release           Build and test release APK, instead of debug
		END
		exit 1
esac
