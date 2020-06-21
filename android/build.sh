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
    log_error "BUILD FAILED"
    exit 1
}

if [ ! -z $RELEASE ]; then
    log_info "Reading secret env variables from ../.secret/env"
    source ../.secret/env || fail
fi


run_adb_as_root() {
    log_info "Running adb as root"
    $ADB root
}

build_apk() {
    log_info "Removing old APKs..."
    rm -vf build/*.apk

    if [ ! -z $RELEASE ]; then
        log_info "Building release APK"
        ./gradlew assembleRelease
        cp -v uhabits-android/build/outputs/apk/release/uhabits-android-release.apk build/loop-$VERSION-release.apk
    fi

    log_info "Building debug APK"
    ./gradlew assembleDebug --stacktrace || fail
    cp -v uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk build/loop-$VERSION-debug.apk
}

build_instrumentation_apk() {
    log_info "Building instrumentation APK"
    if [ ! -z $RELEASE ]; then
        $GRADLE assembleAndroidTest  \
            -Pandroid.injected.signing.store.file=$LOOP_KEY_STORE \
            -Pandroid.injected.signing.store.password=$LOOP_STORE_PASSWORD \
            -Pandroid.injected.signing.key.alias=$LOOP_KEY_ALIAS \
            -Pandroid.injected.signing.key.password=$LOOP_KEY_PASSWORD || fail
    else
        $GRADLE assembleAndroidTest || fail
    fi
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
        -w ${PACKAGE_NAME}.test/androidx.test.runner.AndroidJUnitRunner \
        | tee ${OUTPUTS_DIR}/instrument.txt

    if grep "\(INSTRUMENTATION_STATUS_CODE.*-1\|FAILURES\)" $OUTPUTS_DIR/instrument.txt; then
        log_error "Some instrumented tests failed"
        fetch_images
        fetch_logcat
        exit 1
    fi

    #mkdir -p ${OUTPUTS_DIR}/code-coverage/connected/
    #$ADB pull /data/user/0/${PACKAGE_NAME}/files/coverage.ec \
    #       ${OUTPUTS_DIR}/code-coverage/connected/ \
    #       || log_error "COVERAGE REPORT NOT AVAILABLE"
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
    find $OUTPUTS_DIR/test-screenshots -name '*.expected*' -delete
    rsync -av $OUTPUTS_DIR/test-screenshots/ uhabits-android/src/androidTest/assets/
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
    OPTS=`getopt -o r --long release -n 'build.sh' -- "$@"`
    if [ $? != 0 ] ; then exit 1; fi
    eval set -- "$OPTS" 

    while true; do
        case "$1" in
            -r | --release ) RELEASE=1; shift ;;
            * ) break ;;
        esac
    done    
}

remove_build_dir() {
    rm -rfv .gradle
    rm -rfv build
    rm -rfv android-base/build
    rm -rfv android-pickers/build
    rm -rfv uhabits-android/build
    rm -rfv uhabits-core/build
}

case "$1" in
    build)
        shift; parse_opts $*

        build_apk
        build_instrumentation_apk
        run_jvm_tests
        #generate_coverage_badge
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
    
    clean)
        remove_build_dir
        ;;

    *)
cat <<END
Usage: $0 <command> [options]
Builds, installs and tests Loop Habit Tracker

Commands:
    accept-images     Copies fetched images to corresponding assets folder
    build             Build APK and run JVM tests
    clean             Remove build directory
    fetch-images      Fetches failed view test images from device
    install           Install app on connected device
    large-tests       Run large-sized tests on connected device
    medium-tests      Run medium-sized tests on connected device

Options:
    -r  --release       Build and test release APK, instead of debug
END
        exit 1
        ;;
esac
