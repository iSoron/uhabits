# Build the project

This pages describes how to download and build the app from the source code. All instructions were tested on **Ubuntu Linux 18.04 LTS** and may need to be modified for other operating systems. If you are having trouble building the project, please do not hesitate to open a new issue.

## Build from the command line

**Step 1: Install basic packages**

To build the application, some basic packages are required. The package `git` is required to download the source code, while `openjdk-8-jdk-headless` is required for compiling Java and Kotlin files.

```bash
sudo apt-get update
sudo apt-get install -y git openjdk-8-jdk-headless
```

**IMPORTANT:** Newer JDK versions have not been tested and may not work correctly.


**Step 2: Install Android SDK tools**

The Android SDK tools contains many necessary tools for developing and debugging Android applications. It can be obtained as part of Android Studio, but, for simple command line usage, it can also be downloaded individually.

1. Download the file `sdk-tools-linux-4333796.zip` (or a newer version) from https://developer.android.com/studio/#downloads, and extract it somewhere. In this guide, we assume that it was extracted to `/opt/android-sdk/tools`; that is, the script `/opt/android-sdk/tools/bin/sdkmanager` should exist.

2. Append the following lines to `~/.profile`, so that other tools can locate your Android SDK installation. It is necessary to restart your terminal for these changes to take effect.
```
export PATH="$PATH:/opt/android-sdk/tools/bin"
export PATH="$PATH:/opt/android-sdk/platform-tools"
export ANDROID_HOME="/opt/android-sdk"
```

3. Accept all Android SDK licenses, by running
```bash
yes | sdkmanager --licenses
```

**Step 3: Download the source code**

To create a complete copy of the source code repository, navigate to your home directory and run:
```bash
git clone https://github.com/iSoron/uhabits.git
```
The repository will be downloaded to the directory `uhabits`.

If you are planning to submit pull request, it is recommended that you use the URL `git@github.com:iSoron/uhabits.git` instead of the one above. For this, it may be necessary to [generate and configure your SSH keys](https://help.github.com/en/github/authenticating-to-github/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent).

**Step 4: Compile the source code**

1. Navigate to the directory `uhabits/android`
2. Run `./gradlew assembleDebug --stacktrace`

If the compilation is successful, a debug APK will be generated somewhere inside the folder `uhabits/android/uhabits-android/build/`. Currently, the full path is the following, but it may change in the future:

    ./uhabits/android/uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk

The APK can be installed using the tool `adb`, which should have been automatically installed at `/opt/android-sdk/platform-tools/adb` during compilation of the project.