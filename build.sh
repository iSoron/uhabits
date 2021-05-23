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

cd "$(dirname "$0")" || exit

ADB="${ANDROID_HOME}/platform-tools/adb"
ANDROID_OUTPUTS_DIR="uhabits-android/build/outputs"
AVDMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/avdmanager"
AVD_PREFIX="uhabitsTest"
EMULATOR="${ANDROID_HOME}/tools/emulator"
GRADLE="./gradlew --stacktrace --quiet"
PACKAGE_NAME=org.isoron.uhabits
SDKMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager"
VERSION=$(grep versionName uhabits-android/build.gradle.kts | sed -e 's/.*"\([^"]*\)".*/\1/g')

if [ -z $VERSION ]; then
    echo "Could not parse app version from: uhabits-android/build.gradle.kts"
    exit 1
fi

if [ ! -f "${ANDROID_HOME}/platform-tools/adb" ]; then
    echo "Error: ANDROID_HOME is not set correctly; ${ANDROID_HOME}/platform-tools/adb not found"
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

core_build() {
    log_info "Building uhabits-core..."
    $GRADLE ktlintCheck || fail
    $GRADLE :uhabits-core:build || fail
}

# Android
# -----------------------------------------------------------------------------

# shellcheck disable=SC2016
android_test() {
    API=$1
    AVDNAME=${AVD_PREFIX}${API}

    (
        flock 10
        log_info "Stopping Android emulator..."
        while [[ -n $(pgrep -f ${AVDNAME}) ]]; do
            pkill -9 -f ${AVDNAME}
        done

        log_info "Removing existing Android virtual device..."
        $AVDMANAGER delete avd --name $AVDNAME

        log_info "Creating new Android virtual device (API $API)..."
        (echo "y" | $SDKMANAGER --install "system-images;android-$API;default;x86_64") || return 1
        $AVDMANAGER create avd \
                --name $AVDNAME \
                --package "system-images;android-$API;default;x86_64" \
                --device "Nexus 4" || return 1

        flock -u 10
    ) 10>/tmp/uhabitsTest.lock

    log_info "Launching emulator..."
    $EMULATOR -avd $AVDNAME -port 6${API}0 1>/dev/null 2>&1 &

    log_info "Waiting for emulator to boot..."
    export ADB="$ADB -s emulator-6${API}0"
    $ADB wait-for-device shell 'while [[ -z "$(getprop sys.boot_completed)" ]]; do echo Waiting...; sleep 1; done; input keyevent 82' || return 1
    $ADB root || return 1
    sleep 5

    log_info "Disabling animations..."
    $ADB shell settings put global window_animation_scale 0 || return 1
    $ADB shell settings put global transition_animation_scale 0 || return 1
    $ADB shell settings put global animator_duration_scale 0 || return 1

    log_info "Acquiring wake lock..."
    $ADB shell 'echo android-test > /sys/power/wake_lock' || return 1

    if [ -n "$RELEASE" ]; then
        log_info "Installing release APK..."
        $ADB install -r ${ANDROID_OUTPUTS_DIR}/apk/release/uhabits-android-release.apk || return 1
    else
        log_info "Installing debug APK..."
        $ADB install -t -r ${ANDROID_OUTPUTS_DIR}/apk/debug/uhabits-android-debug.apk || return 1
    fi
    log_info "Installing test APK..."
    $ADB install -r ${ANDROID_OUTPUTS_DIR}/apk/androidTest/debug/uhabits-android-debug-androidTest.apk || return 1

    for size in medium large; do
        log_info "Running $size instrumented tests..."
        OUT_INSTRUMENT=${ANDROID_OUTPUTS_DIR}/instrument-${API}.txt
        OUT_LOGCAT=${ANDROID_OUTPUTS_DIR}/logcat-${API}.txt
        $ADB shell am instrument \
            -r -e coverage true -e size $size \
            -w ${PACKAGE_NAME}.test/androidx.test.runner.AndroidJUnitRunner \
            | tee $OUT_INSTRUMENT
        if grep "\(INSTRUMENTATION_STATUS_CODE.*-1\|FAILURES\|ABORTED\|onError\|Error type\|crashed\)" $OUT_INSTRUMENT; then
            log_error "Some $size instrumented tests failed."
            log_error "Saving logcat: $OUT_LOGCAT..."
            $ADB logcat -d > $OUT_LOGCAT
            log_error "Fetching test screenshots..."
            $ADB pull /sdcard/Android/data/${PACKAGE_NAME}/files/test-screenshots ${ANDROID_OUTPUTS_DIR}/
            $ADB shell rm -r /sdcard/Android/data/${PACKAGE_NAME}/files/test-screenshots/
            return 1
        fi
        log_info "$size tests passed."
    done

    return 0
}

android_test_parallel() {
    for API in $*; do
        (
            LOG=build/android-test-$API.log
            log_info "API $API: Running tests..."
            if android_test $API 1>$LOG 2>&1; then
                log_info "API $API: Passed"
            else
                log_error "API $API: Failed. See $LOG for more details."
            fi
            pkill -9 -f ${AVD_PREFIX}${API}
        )&
    done
    wait
}

android_build() {
    log_info "Building uhabits-android..."

    if [ -n "$RELEASE" ]; then
        log_info "Reading secret..."
        # shellcheck disable=SC1091
        source .secret/env || fail
    fi

    log_info "Removing old APKs..."
    rm -vf uhabits-android/build/*.apk

    if [ -n "$RELEASE" ]; then
        log_info "Building release APK..."
        $GRADLE updateTranslators
        $GRADLE :uhabits-android:assembleRelease
        cp -v \
            uhabits-android/build/outputs/apk/release/uhabits-android-release.apk \
            uhabits-android/build/loop-"$VERSION"-release.apk
    fi

    log_info "Building debug APK..."
    $GRADLE :uhabits-android:assembleDebug --stacktrace || fail
    cp -v \
        uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk \
        uhabits-android/build/loop-"$VERSION"-debug.apk

    log_info "Building instrumentation APK..."
    if [ -n "$RELEASE" ]; then
        $GRADLE :uhabits-android:assembleAndroidTest  \
            -Pandroid.injected.signing.store.file="$LOOP_KEY_STORE" \
            -Pandroid.injected.signing.store.password="$LOOP_STORE_PASSWORD" \
            -Pandroid.injected.signing.key.alias="$LOOP_KEY_ALIAS" \
            -Pandroid.injected.signing.key.password="$LOOP_KEY_PASSWORD" || fail
    else
        $GRADLE assembleAndroidTest || fail
    fi

    return 0
}

android_accept_images() {
    find ${ANDROID_OUTPUTS_DIR}/test-screenshots -name '*.expected*' -delete
    rsync -av ${ANDROID_OUTPUTS_DIR}/test-screenshots/ uhabits-android/src/androidTest/assets/
}

# General
# -----------------------------------------------------------------------------

_parse_opts() {
    if ! OPTS="$(getopt -o r --long release -n 'build.sh' -- "$@")" ; then
      exit 1;
    fi
    eval set -- "$OPTS"

    while true; do
        case "$1" in
            -r | --release ) RELEASE=1; shift ;;
            * ) break ;;
        esac
    done
}

_print_usage() {
    cat <<END
CI/CD script for Loop Habit Tracker.

Usage:
    build.sh build [options]
    build.sh android-tests <API> [options]
    build.sh android-tests-parallel <API> <API>... [options]
    build.sh android-accept-images [options]

Commands:
    build                   Build the app and run small tests
    android-tests           Run medium and large Android tests on an emulator
    android-tests-parallel  Tests multiple API levels simultaneously
    android-accept-images   Copy fetched images to corresponding assets folder

Options:
    -r  --release       Build and test release version, instead of debug
END
}

clean() {
    rm -rfv uhabits-android/.gradle
    rm -rfv uhabits-android/android-pickers/build
    rm -rfv uhabits-android/build
    rm -rfv uhabits-android/uhabits-android/build
    rm -rfv uhabits-core-legacy/.gradle
    rm -rfv uhabits-core-legacy/build
    rm -rfv uhabits-core/.gradle
    rm -rfv uhabits-core/build
    rm -rfv uhabits-server/.gradle
    rm -rfv uhabits-server/build
    rm -rfv uhabits-web/build
    rm -rfv uhabits-web/node_modules
    rm -rfv uhabits-web/node_modules/core-js/build
    rm -rfv uhabits-web/node_modules/upath/build
    rm -rfv .gradle
}

main() {
    case "$1" in
        build)
            shift; _parse_opts "$@"
            clean
            core_build
            android_build
            ;;
        android-tests)
            shift; _parse_opts "$@"
            if [ -z $1 ]; then
                _print_usage
                exit 1
            fi
            for attempt in {1..5}; do
                log_info "Running Android tests (attempt $attempt)..."
                android_test $1 && return 0
            done
            log_error "Maximum number of attempts reached. Failing."
            return 1
            ;;
        android-tests-parallel)
            shift; _parse_opts "$@"
            android_test_parallel $*
            ;;
        android-accept-images)
            android_accept_images
            ;;
        *)
            _print_usage
            exit 1
            ;;
    esac
}

main "$@"
