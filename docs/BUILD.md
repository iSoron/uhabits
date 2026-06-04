# Build the project

This pages describes how to download and build the app from the source code. If you are having trouble building the project, please do not hesitate to open a new issue.

## Contents

* [Build using Android Studio](#build-using-android-studio)
* [Build from the command line](#build-from-the-command-line)

## Build using Android Studio

### Step 1: Install git

The package `git` is required for downloading the source code of the app and submitting changes GitHub. Please see [the git book](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) for further instructions. If you are planning to submit pull requests in the future, it is recommended to [generate and configure your SSH keys](https://help.github.com/en/github/authenticating-to-github/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent).

### Step 2: Download and install Android Studio

Although Android Studio can be downloaded [from their official website](https://developer.android.com/studio/), a much better option is to install it through [JetBrains Toolbox](https://www.jetbrains.com/toolbox-app/). This tool, developed by the same developers of Android Studio, allows you to easily upgrade and downgrade the IDE, or switch between stable, beta and canary versions. After downloading and installing JetBrains Toolbox, simply click the install button near Android Studio to install the newest stable version of IDE. Beta and canary versions have not been tested and may not work correctly.

After installation, launch Android Studio. If this is the first time you launch it, you will need to go through a wizard to setup the IDE. The default options should work fine. The wizard will download all additional components necessary for development, including the emulator, so it may take a while.

### Step 3: Download the source code

To create a complete copy of the source code repository, open the terminal (Linux/macOS) or Git Bash (Windows), navigate to the desired folder, then run:
```bash
git clone https://github.com/iSoron/uhabits.git
```
The repository will be downloaded to the directory `uhabits`.

### Step 4: Open and run the project on Android Studio

1. Launch Android Studio and select "Open an existing Android Studio project".
2. When the IDE asks you for the project location, select `uhabits` and click "Ok".
3. Android Studio will spend some time indexing the project. When this is complete, click the toolbar icon "Sync Project with Gradle File", located near the right corner of the top toolbar.
4. The operation will likely fail several times due to missing Android SDK components. Each time it fails, click the link "Install missing platforms", "Install build tools", etc, and try again.
5. To test the application, create a virtual Android device using the menu "Tools" and "AVD Manager". The default options should work fine, but feel free to customize the device.
6. Click the menu "Run" and "uhabits-android". The application should launch.


## Build from the command line

The following instructions were tested on **Ubuntu Linux 22.04 LTS** and should also work on 20.04 LTS, 24.04 LTS, and recent macOS releases. They may need to be modified for other operating systems.

### Step 1: Install basic packages

To build the application, some basic packages are required. The package `git` is required to download the source code, and a JDK is required for compiling Java and Kotlin files. The project is configured with `jvmToolchain(17)` in `uhabits-android/build.gradle.kts` and `uhabits-core/build.gradle.kts`, so **JDK 17 is required**. Older JDKs (such as JDK 8 or JDK 11) are no longer supported by the current Android Gradle Plugin and will fail to compile.

On Debian/Ubuntu, install OpenJDK 17:

```bash
sudo apt-get update
sudo apt-get install -y git openjdk-17-jdk-headless
```

On macOS (using Homebrew):

```bash
brew install git openjdk@17
```

If you have multiple JDKs installed, point Gradle to the correct one by exporting `JAVA_HOME` before running any `./gradlew` command, for example:

```bash
export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(which javac)")")")"
```

Gradle will normally download the required toolchain automatically if it cannot find a matching JDK, but exporting `JAVA_HOME` speeds up the first build and avoids network surprises on CI.


### Step 2: Install Android SDK tools

The Android SDK tools contain many of the necessary tools for developing and debugging Android applications. They can be obtained as part of Android Studio, but, for simple command line usage, it is recommended to install the **command-line tools** directly, since that is what `build.sh` and the CI pipeline expect (`$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager`).

1. Download the latest `commandlinetools-linux-*.zip` (or `commandlinetools-mac-*.zip` on macOS) from <https://developer.android.com/studio#command-line-tools-only>, and extract it to `/opt/android-sdk/cmdline-tools/latest` so that the script `/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager` exists. (Earlier standalone "SDK tools" archives from 2018 are deprecated and no longer recommended.)

2. Append the following lines to `~/.profile` (Linux) or `~/.zshrc` / `~/.bash_profile` (macOS), so that other tools can locate your Android SDK installation. It is necessary to restart your terminal for these changes to take effect.
```bash
export ANDROID_HOME="/opt/android-sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
export PATH="$PATH:$ANDROID_HOME/platform-tools"
```

3. Accept all Android SDK licenses by running:
```bash
yes | sdkmanager --licenses
```

### Step 3: Download the source code

To create a complete copy of the source code repository, navigate to your home directory and run:
```bash
git clone https://github.com/iSoron/uhabits.git
```
The repository will be downloaded to the directory `uhabits`.

### Step 4: Compile the source code

1. Navigate to the directory `uhabits`
2. Run `./gradlew assembleDebug --stacktrace`

If the compilation is successful, a debug APK will be generated somewhere inside the folder `uhabits-android/build/`. Currently, the full path is `./uhabits-android/build/outputs/apk/debug/uhabits-android-debug.apk`, but it may change in the future.

The APK can be installed using the tool `adb`, which should have been automatically installed at `/opt/android-sdk/platform-tools/adb` during compilation of the project.
