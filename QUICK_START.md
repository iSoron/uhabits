# Quick Setup Checklist - Loop Habit Tracker

## ⚡ 5-Minute Quick Start

### Have You Already Got?
- [ ] Windows 10/11
- [ ] Command line (Git Bash or PowerShell)
- [ ] Internet connection (SDK is ~5-10 GB)

### Install These in Order

#### Step 1: Java 17 (5 min)
```bash
# Verify if already installed:
java -version

# Should show: openjdk 17.x.x

# If not installed, install via scoop (if you have it):
scoop install openjdk17
```

#### Step 2: Android SDK (15 min - mostly download)

**Option A - Using Android Studio (Easiest, Recommended)**
```
1. Go to: https://developer.android.com/studio
2. Download and run installer
3. Go through installation wizard
4. Android Studio will automatically download SDK
```

**Option B - Command Line**
```bash
# Download from https://developer.android.com/studio#downloads
# Extract to: C:\Users\w11-d\AppData\Local\Android\Sdk

# Then run:
sdkmanager --licenses
sdkmanager "platforms;android-36"
sdkmanager "platforms;android-28"
sdkmanager "build-tools;36.0.0"
```

#### Step 3: Set Environment Variable (2 min)
```bash
# Windows 11:
# 1. Press: Win+X → System Settings
# 2. Search: "Environment Variables"
# 3. Click: "Edit the system environment variables"
# 4. Click: "Environment Variables..." button
# 5. Under "User variables" → Click "New"
#    - Variable name: ANDROID_HOME
#    - Variable value: C:\Users\w11-d\AppData\Local\Android\Sdk
# 6. Click OK → OK → Restart your terminal

# Verify it works:
echo %ANDROID_HOME%
```

#### Step 4: Update local.properties (1 min)
```bash
# Edit: C:\Users\w11-d\src\gh\uhabits\local.properties

# Make sure it has:
org.gradle.java.home=C:/Users/w11-d/scoop/apps/openjdk17/current
sdk.dir=C:/Users/w11-d/AppData/Local/Android/Sdk
```

#### Step 5: Verify Setup (1 min)
```bash
# Run the verification script:
cd C:\Users\w11-d\src\gh\uhabits
./verify-setup.sh

# Should show ✅ for all items
```

---

## ✅ Build & Test Commands

Once setup is complete, you can run:

### Build the App
```bash
cd C:\Users\w11-d\src\gh\uhabits
./gradlew :uhabits-android:assembleDebug

# Output: uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk
```

### Run Unit Tests (No emulator needed)
```bash
./gradlew :uhabits-android:testDebugUnitTest

# Runs 13 tests for our new AggregateScoreCalculator
# Should show: ✅ 13 passed
```

### Run All Tests
```bash
./gradlew test

# Runs all unit and core module tests
```

### Install & Run on Device
```bash
# Connect USB device or start emulator, then:
./gradlew :uhabits-android:installDebug
adb shell am start -n org.isoron.uhabits/.activities.habits.list.ListHabitsActivity
```

---

## 🆘 Troubleshooting

### Build Fails: "SDK location not found"
→ Add `sdk.dir=...` to `local.properties`

### Build Fails: "Cannot find Java 17"
→ Run: `export JAVA_HOME=C:\Users\w11-d\scoop\apps\openjdk17\current`

### Build Fails: "ANDROID_HOME not set"
→ Set environment variable and restart terminal

### Tests Won't Run: "Cannot find TestRunner"
→ You're missing Android SDK - go back to Step 2

### Gradle Takes Forever
→ First build is slow (downloads dependencies). Subsequent builds are faster.

---

## 📊 Expected Timeline

| Task | Time | Status |
|------|------|--------|
| Java 17 | 5 min | ✅ Done |
| Android SDK | 20 min | ❌ TODO |
| Environment setup | 3 min | ❌ TODO |
| First build | 10 min | ❌ TODO |
| Run tests | 2 min | ❌ TODO |
| **Total** | **~40 min** | |

---

## 📚 More Information

- Full setup guide: [ANDROID_SETUP.md](ANDROID_SETUP.md)
- Original build guide: [docs/BUILD.md](docs/BUILD.md)
- Test guide: [docs/TEST.md](docs/TEST.md)
- Development guidelines: [docs/GUIDELINES.md](docs/GUIDELINES.md)

---

## ✨ You're All Set When...

```bash
$ ./verify-setup.sh
# All checks show ✅

$ ./gradlew :uhabits-android:testDebugUnitTest
# Output: "13 passed"

$ ./gradlew :uhabits-android:assembleDebug
# Output: "APK created successfully"
```

**Congratulations! You're ready to develop, test, and build Loop Habit Tracker! 🎉**
