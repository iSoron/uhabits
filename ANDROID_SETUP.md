# Complete Android Build & Test Setup Guide for Loop Habit Tracker

## Project Requirements Summary

**Current Environment Status:**
- ✅ Gradle: 8.11.1 (already installed)
- ✅ Java/JDK: 17 (installed, required)
- ❌ Android SDK: NOT installed (blocking full build)
- ❌ Android Platform Tools: NOT installed
- ❌ Android Emulator: NOT installed (needed for instrumented tests)

---

## What You Need to Install

### 1. **Java Development Kit (JDK) 17** ✅ ALREADY DONE
```
Your Setup:
  Location: C:\Users\w11-d\scoop\apps\openjdk17\current
  Version: OpenJDK 17.0.2-8
  Status: ✅ Working
```

**Note:** Do NOT use Java 21+ or Java 8. Java 17 is strictly required.
- Project builds with: `kotlinOptions.jvmTarget = 17`
- sourceCompatibility/targetCompatibility = Java 17

---

### 2. **Android SDK** ❌ REQUIRED (Missing)

**What it is:** Core Android development tools and libraries needed to compile Android apps

**Version Requirements:**
- compileSdk: 36 (Android 15)
- targetSdk: 36 (Android 15)
- minSdk: 28 (Android 9)

**Installation Options:**

#### **Option A: Using Android Studio (Recommended)**
```
1. Download Android Studio from: https://developer.android.com/studio
2. Run installer
3. During setup, Android Studio will automatically:
   - Install Android SDK
   - Install required platforms (API 36, 28)
   - Configure paths
4. Set ANDROID_HOME environment variable:
   - Windows: Environment Variables → ANDROID_HOME → C:\Users\USERNAME\AppData\Local\Android\Sdk
```

**Installation Location on Windows:**
```
C:\Users\w11-d\AppData\Local\Android\Sdk\
├── cmdline-tools/
├── platforms/           (needed: android-36, android-28)
├── platform-tools/      (adb, fastboot)
├── build-tools/         (needed)
└── emulator/            (optional, for testing)
```

#### **Option B: Command Line Only (Advanced)**
```bash
# 1. Download SDK tools from: https://developer.android.com/studio#downloads
#    Get: "Command line tools for Windows"

# 2. Extract to: C:\Users\w11-d\AppData\Local\Android\Sdk\cmdline-tools\latest

# 3. Accept licenses:
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

# 4. Install required platforms:
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-36"
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-28"
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "build-tools;36.0.0"

# 5. Set environment variable:
setx ANDROID_HOME "C:\Users\w11-d\AppData\Local\Android\Sdk"
```

---

### 3. **Android Platform Tools** (Included with SDK)

**What they are:** adb (Android Debug Bridge), fastboot, and other device/emulator tools

**Includes:**
- `adb` - Connect to devices/emulators
- `fastboot` - Flash images
- Located in: `%ANDROID_HOME%/platform-tools/`

**Verification:**
```bash
adb version    # Should show version info
```

---

### 4. **Android Emulator** (Needed for UI Tests) ❌ OPTIONAL

**For Instrumented Tests Only:**

If you plan to run: `./build.sh android-tests API`

**Installation:**
```bash
# Via Android Studio: Tools → Device Manager → Create Virtual Device
# Or command line:
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "emulator"
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "system-images;android-36;google_apis_playstore;x86_64"
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd -n "Pixel_4_API_36" \
  -k "system-images;android-36;google_apis_playstore;x86_64"
```

---

## Installation Steps for Complete Build

### **Step 1: Install Android SDK** (5-10 minutes)

**Windows with Android Studio:**
```
1. Download from: https://developer.android.com/studio
2. Run installer (android-studio-2024.x.x-windows.exe)
3. Follow setup wizard
4. Let it download Android SDK automatically
5. Note the installation path (usually C:\Users\USERNAME\AppData\Local\Android\Sdk)
```

### **Step 2: Set Environment Variables** (1 minute)

```
Windows 11/10:
1. Press: Win+X → System Settings
2. Search: "Environment Variables"
3. Click: "Edit the system environment variables"
4. New User Variable:
   Variable name: ANDROID_HOME
   Variable value: C:\Users\w11-d\AppData\Local\Android\Sdk
5. Click OK → OK → Restart terminal
```

**Verify:**
```bash
echo %ANDROID_HOME%    # Should print the path
```

### **Step 3: Update local.properties** (1 minute)

```
File: C:\Users\w11-d\src\gh\uhabits\local.properties

Current content:
  org.gradle.java.home=C:/Users/w11-d/scoop/apps/openjdk17/current

Add:
  sdk.dir=C:/Users/w11-d/AppData/Local/Android/Sdk
```

### **Step 4: Verify Installation** (1 minute)

```bash
cd C:\Users\w11-d\src\gh\uhabits

# Test Java
java -version              # Should show Java 17

# Test Gradle
./gradlew --version        # Should show Gradle 8.11.1

# Test Android SDK path
echo %ANDROID_HOME%        # Should show SDK path
ls %ANDROID_HOME%          # Should show platforms/, build-tools/, etc.
```

---

## Running the Build & Tests

### **Build Debug APK:**
```bash
cd C:\Users\w11-d\src\gh\uhabits
export JAVA_HOME=C:\Users\w11-d\scoop\apps\openjdk17\current
./gradlew :uhabits-android:assembleDebug
# Output: uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk
```

### **Run Unit Tests (No Emulator Needed):**
```bash
./gradlew :uhabits-android:testDebugUnitTest
# Will run our 13 AggregateScoreCalculatorTest tests
```

### **Run All Tests:**
```bash
./gradlew test
# Both unit tests and core module tests
```

### **Build & Run on Emulator (Requires emulator):**
```bash
# Using build.sh script (from docs/BUILD.md):
./build.sh android-setup 36          # Create API 36 emulator
./build.sh android-tests 36          # Run instrumented tests
```

---

## Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| "SDK location not found" | `local.properties` missing sdk.dir | Add `sdk.dir` to local.properties |
| "Cannot find Android SDK" | ANDROID_HOME not set | Set environment variable, restart terminal |
| "No platforms installed" | Only cmdline-tools downloaded | Run `sdkmanager` to install platforms |
| Java version mismatch | Java 21/25 instead of 17 | Set `JAVA_HOME` to Java 17 path |
| Gradle daemon issues | Cached Gradle state | Run `./gradlew --stop` then retry |

---

## Disk Space Requirements

- Android SDK: 5-10 GB
- Build artifacts: 2-3 GB
- Emulator image: 3-5 GB (if using)
- **Total: ~10-20 GB free space recommended**

---

## Final Verification

Once everything is installed, you should be able to run:

```bash
cd C:\Users\w11-d\src\gh\uhabits

# This should complete successfully:
./gradlew :uhabits-android:assembleDebug

# This should run 13 tests:
./gradlew :uhabits-android:testDebugUnitTest

# Success = these generate:
# ✅ APK at: uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk
# ✅ Test report at: uhabits-android/build/reports/tests/debug/
```

---

## Summary

**Minimal Setup (Just build APK):**
- Java 17 ✅
- Gradle 8.11.1 ✅
- Android SDK (need to install)
- ANDROID_HOME env var (need to set)

**Full Setup (Build + Unit Tests):**
- Above + configure local.properties

**Complete Setup (Build + All Tests + Emulator):**
- Above + Android Emulator + system images

**Estimated time to install:** 30-45 minutes (mostly download time)
