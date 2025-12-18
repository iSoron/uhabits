#!/bin/bash
# Quick Setup Verification Script for Loop Habit Tracker Build Environment
# Run this to verify all required components are installed

echo "====== Loop Habit Tracker - Build Environment Verification ======"
echo ""

# Check Java
echo "1️⃣  Java JDK 17"
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | grep -oP 'version "\K[^"]+')
    echo "   ✅ Found: $java_version"
    if [[ $java_version == 17* ]]; then
        echo "   ✅ Correct version (17)"
    else
        echo "   ❌ WRONG VERSION - Need Java 17, got $java_version"
    fi
else
    echo "   ❌ NOT FOUND - Install Java 17 from https://openjdk.java.net/"
fi
echo ""

# Check Gradle
echo "2️⃣  Gradle"
if command -v gradle &> /dev/null || [ -f "gradlew" ]; then
    gradle_version=$(./gradlew --version 2>&1 | grep "Gradle" | head -1)
    echo "   ✅ Found: $gradle_version"
else
    echo "   ❌ NOT FOUND"
fi
echo ""

# Check Android SDK
echo "3️⃣  Android SDK"
if [ -z "$ANDROID_HOME" ]; then
    echo "   ❌ ANDROID_HOME not set"
    echo "      Set it with: export ANDROID_HOME=/path/to/android/sdk"
else
    echo "   📍 ANDROID_HOME: $ANDROID_HOME"
    if [ -d "$ANDROID_HOME" ]; then
        echo "   ✅ Directory exists"

        # Check platforms
        if [ -d "$ANDROID_HOME/platforms" ]; then
            echo "   ✅ platforms/ found"
            if [ -d "$ANDROID_HOME/platforms/android-36" ]; then
                echo "      ✅ android-36 installed"
            else
                echo "      ❌ android-36 NOT installed - RUN: sdkmanager \"platforms;android-36\""
            fi
            if [ -d "$ANDROID_HOME/platforms/android-28" ]; then
                echo "      ✅ android-28 installed"
            else
                echo "      ❌ android-28 NOT installed - RUN: sdkmanager \"platforms;android-28\""
            fi
        else
            echo "   ❌ platforms/ NOT found"
        fi

        # Check build-tools
        if [ -d "$ANDROID_HOME/build-tools" ]; then
            echo "   ✅ build-tools/ found"
        else
            echo "   ❌ build-tools/ NOT found"
        fi

        # Check platform-tools
        if [ -d "$ANDROID_HOME/platform-tools" ]; then
            echo "   ✅ platform-tools/ found (adb, fastboot)"
        else
            echo "   ❌ platform-tools/ NOT found"
        fi
    else
        echo "   ❌ Directory does not exist: $ANDROID_HOME"
    fi
fi
echo ""

# Check local.properties
echo "4️⃣  local.properties"
if [ -f "local.properties" ]; then
    echo "   ✅ File exists"
    if grep -q "org.gradle.java.home" local.properties; then
        echo "   ✅ org.gradle.java.home set"
    else
        echo "   ⚠️  org.gradle.java.home not set"
    fi
    if grep -q "sdk.dir" local.properties; then
        echo "   ✅ sdk.dir set"
    else
        echo "   ❌ sdk.dir NOT set - Add: sdk.dir=/path/to/android/sdk"
    fi
else
    echo "   ❌ local.properties NOT found"
fi
echo ""

echo "====== Summary ======"
echo ""
echo "✅ Ready to build if:"
echo "   - Java 17 is installed"
echo "   - ANDROID_HOME is set"
echo "   - Android SDK 36 and 28 are installed"
echo "   - local.properties has sdk.dir"
echo ""
echo "📖 Full setup guide: ANDROID_SETUP.md"
echo ""
