#!/bin/bash
# Copyright (C) 2016-2021 Álinson Santos Xavier <isoron@gmail.com>
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
AVDMANAGER="${ANDROID_HOME}/tools/bin/avdmanager"
AVDNAME="uhabitsTest"
GRADLE="./gradlew --stacktrace --quiet"
PACKAGE_NAME=org.isoron.uhabits
ANDROID_OUTPUTS_DIR="uhabits-android/uhabits-android/build/outputs"
VERSION=$(cat uhabits-android/gradle.properties | grep VERSION_NAME | sed -e 's/.*=//g;s/ //g')

if [ ! -f "${ANDROID_HOME}/platform-tools/adb" ]; then
    echo "Error: ANDROID_HOME is not set correctly"
    exit 1
fi


# Logging
# -----------------------------------------------------------------------------

log_error() {
    local COLOR='\033[1;31m'
    local NC='\033[0m'
    echo -e "$COLOR* $1 $NC"
}

log_info() {
    local COLOR='\033[1;32m'
    local NC='\033[0m'
    echo -e "$COLOR* $1 $NC"
}

fail() {
    log_error "BUILD FAILED"
    exit 1
}


# Core
# -----------------------------------------------------------------------------

ktlint() {
    log_info "Running ktlint..."
    cd uhabits-android
    $GRADLE ktlintCheck || fail
    cd ..
}

build_core() {
    log_info "Building uhabits-core..."
    cd uhabits-core
    $GRADLE build || fail
    cd ..
}


# Android
# -----------------------------------------------------------------------------

run_adb_as_root() {
    log_info "Running adb as root..."
    $ADB root
}

build_apk() {
    cd uhabits-android

    if [ ! -z $RELEASE ]; then
        log_info "Reading secret..."
        source ../.secret/env || fail
    fi

    log_info "Removing old APKs..."
    rm -vf build/*.apk

    if [ ! -z $RELEASE ]; then
        log_info "Building release APK..."
        $GRADLE assembleRelease
        cp -v \
            uhabits-android/build/outputs/apk/release/uhabits-android-release.apk \
            build/loop-$VERSION-release.apk
    fi

    log_info "Building debug APK..."
    $GRADLE assembleDebug --stacktrace || fail
    cp -v \
        uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk \
        build/loop-$VERSION-debug.apk
    
    cd ..
}

build_instrumentation_apk() {
    log_info "Building instrumentation APK..."
    cd uhabits-android
    if [ ! -z $RELEASE ]; then
        $GRADLE assembleAndroidTest  \
            -Pandroid.injected.signing.store.file=$LOOP_KEY_STORE \
            -Pandroid.injected.signing.store.password=$LOOP_STORE_PASSWORD \
            -Pandroid.injected.signing.key.alias=$LOOP_KEY_ALIAS \
            -Pandroid.injected.signing.key.password=$LOOP_KEY_PASSWORD || fail
    else
        $GRADLE assembleAndroidTest || fail
    fi
    cd ..
}

uninstall_apk() {
    log_info "Uninstalling existing APK..."
    $ADB uninstall ${PACKAGE_NAME}
}

install_test_butler() {
    log_info "Installing Test Butler..."
    $ADB uninstall com.linkedin.android.testbutler
    $ADB install uhabits-android/tools/test-butler-app-2.0.2.apk
}

install_apk() {
    log_info "Installing APK..."
    if [ ! -z $RELEASE ]; then
        $ADB install -r ${ANDROID_OUTPUTS_DIR}/apk/release/uhabits-android-release.apk || fail
    else
        $ADB install -t -r ${ANDROID_OUTPUTS_DIR}/apk/debug/uhabits-android-debug.apk || fail
    fi
}

install_test_apk() {
    log_info "Uninstalling existing test APK..."
    $ADB uninstall ${PACKAGE_NAME}.test

    log_info "Installing test APK..."
    $ADB install -r ${ANDROID_OUTPUTS_DIR}/apk/androidTest/debug/uhabits-android-debug-androidTest.apk || fail
}

run_instrumented_tests() {
    SIZE=$1
    log_info "Running instrumented tests..."
    $ADB shell am instrument \
        -r -e coverage true -e size $SIZE \
        -w ${PACKAGE_NAME}.test/androidx.test.runner.AndroidJUnitRunner \
        | tee ${ANDROID_OUTPUTS_DIR}/instrument.txt

    if grep "\(INSTRUMENTATION_STATUS_CODE.*-1\|FAILURES\)" $ANDROID_OUTPUTS_DIR/instrument.txt; then
        log_error "Some instrumented tests failed"
        fetch_logcat
        exit 1
    fi
}

fetch_logcat() {
    log_info "Fetching logcat..."
    $ADB logcat -d > ${ANDROID_OUTPUTS_DIR}/logcat.txt
}

uninstall_test_apk() {
    log_info "Uninstalling test APK..."
    $ADB uninstall ${PACKAGE_NAME}.test
}

remove_avd() {
    log_info "Removing AVD..."
    $AVDMANAGER delete avd --name $AVDNAME
}

create_avd() {
    API=$1
    log_info "Creating AVD..."
    $AVDMANAGER create avd \
            --name $AVDNAME \
            --package "system-images;android-$API;default;x86_64" \
            --device pixel_xl || fail
}

wait_for_device() {
    log_info "Waiting for device..."
    adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
    sleep 15
}

run_avd() {
    log_info "Launching emulator..."
    $EMULATOR @$AVDNAME &
    wait_for_device
}

stop_avd() {
    log_info "Stopping emulator..."
    # https://stackoverflow.com/a/38652520
    adb devices | grep emulator | cut -f1 | while read line; do
        adb -s $line emu kill
    done
    while [[ ! -z $(pgrep emulator) ]]; do sleep 1; done
}

run_tests() {
    SIZE=$1
    run_adb_as_root
    install_test_butler
    uninstall_apk
    install_apk
    install_test_apk
    run_instrumented_tests $SIZE
    fetch_logcat
    uninstall_test_apk
}

build_android() {
    log_info "Building uhabits-android..."
    build_apk
    build_instrumentation_apk
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
    rm -rfv uhabits-core/build
    rm -rfv uhabits-web/node_modules/upath/build
    rm -rfv uhabits-web/node_modules/core-js/build
    rm -rfv uhabits-web/build
    rm -rfv uhabits-core-legacy/build
    rm -rfv uhabits-server/build
    rm -rfv uhabits-android/build
    rm -rfv uhabits-android/uhabits-android/build
    rm -rfv uhabits-android/android-pickers/build
    rm -rfv uhabits-web/node_modules
}

main() {
    case "$1" in
        build)
            shift; parse_opts $*
            ktlint
            build_core
            build_android
            ;;

        medium-tests)
            shift; parse_opts $*
            for attempt in {1..3}; do
                (run_tests medium) && exit 0
            done
            exit 1
            ;;

        large-tests)
            shift; parse_opts $*
            stop_avd
            remove_avd
            for api in 28; do
                create_avd $api
                run_avd
                run_tests large
                stop_avd
                remove_avd
            done
            ;;

        clean)
            remove_build_dir
            ;;

        *)
    cat <<END
Usage: $0 <command> [options]
Builds and tests Loop Habit Tracker

Commands:
    build             Build the app
    clean             Remove all build directories
    large-tests       Run large-sized tests on connected device
    medium-tests      Run medium-sized tests on connected device

Options:
    -r  --release       Build and test release version, instead of debug
END
            exit 1
            ;;
    esac
}

main $*
    
