# Testing the project

Loop Habit Tracker has a fairly large number of automated tests to reduce the chance of bugs being silently introduced in our code base. The tests are divided into three categories:

* **Small tests:** These tests run very quickly on the developer's computer, inside a JVM, and do not need an Android emulator or device. They typically test the correctness of core functions of the application, such as the computation of scores and streaks.
* **Medium tests:** These tests require an Android emulator or device, but they are still quite fast to run, since only individual classes are tested. The app itself does not need to be launched. Examples include *view tests*, which render our custom views on the device and compare them against prerendered images.
* **Large tests:** These are end-to-end tests, which launch the application on an Android emulator and interact with it by touching the screen, much like a regular user.

## Running small tests

Small tests can be launched by running `./gradlew test` or by right-clicking a particular class/method in Android Studio and selecting "Run testMethod()" or "Run ClassTest". An alternative way is to use `build.sh`, the script used by our continuous integration server. By running `./build.sh build`, the script will automatically build and run all small tests.

## Running medium tests

To run medium tests, it is recommended to use the `build.sh` script:

    ./build.sh build
    ./build.sh medium-tests


For this script to succeed, make sure that an emulator is currently running, or that a device (with developer mode activated) is connected via USB.

**WARNING!** This script will uninstall the app prior to testing it, and therefore delete all user data!


If there are failing view tests (that is, if some custom views do not render exactly like the prerendered images we have), then the script `./build.sh fetch-images` can be used to download both the actual and the expected images from the device. The images will be downloaded from the device into the folder `tmp/`. After verifying the differences, if you feel that the actual images are actually fine and should replace the prerendered ones, then run `./build.sh accept-images`.

## Running large tests

Large tests are significantly more complicated to run. In particular, they require:

* An Android emulator; they will **not** work on actual devices;
* A vanilla x86 AOSP image; they will **not** work with Google API images;
* A particular screen size, namely the Nexus 4 configuration on Android Studio (4.7 768x1280 xhdpi);
* A particular locale, namely English (US).

Furthermore:

* No additional apps should be installed on the device;
* The homescreen must look exactly like it was when the emulator was originally created, with no additional icons or widgets;
* Developer mode must be activated, and all animations must be manually disabled.

Only the following Android versions are supported by our test suite:

* Android 7.0 (API 24)
* Android 7.1.1 (API 25)
* Android 8.0 (API 26)
* Android 8.1 (API 27)
* Android 9.0 (API 28)
* Android 10.0 (API 29)

After creating an emulator and configuring it exactly as described above, launch it, wait for it to finish booting up, then run `./build.sh large-tests`. As mentioned before, this script will uninstall the app before testing it, and therefore will delete all the user data.
