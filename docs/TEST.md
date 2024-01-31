# Testing the project

Loop Habit Tracker has a fairly large number of automated tests to reduce the chance of bugs being silently introduced in our code base. The tests are divided into three categories:

- **Unit tests:** These tests run very quickly on the developer's computer, inside a JVM, and do not need an Android emulator or device. They typically test the correctness of core functions of the application, such as the computation of scores and streaks.
- **Instrumented tests:** These tests require an Android emulator or device. _Medium_ instrumented tests are still quite fast to run, since only individual classes are tested. The app itself does not need to be launched. Examples include _view tests_, which render our custom views on the device and compare them against prerendered images. _Large_ instrumented tests launch the application on an Android emulator and interact with it by touching the screen, much like a regular user.

## Running unit tests

Unit tests can be launched by running `./gradlew test` or by right-clicking a particular class/method in Android Studio and selecting "Run testMethod()" or "Run ClassTest". An alternative way is to use `build.sh`, the script used by our continuous integration server. By running `./build.sh build`, the script will automatically build and run all small tests.

## Running instrumented tests

To run medium tests, it is recommended to use the `build.sh` script.

1. Run `./build.sh android-setup API` to create the emulator, where `API` is the desired API level.
2. Run `./build.sh android-tests API` to run the tests on a single API.
3. Run `./build.sh android-tests-parallel API API...` to run the tests on multiple APIs in parallel.

Note that instrumented tests are designed to run on a clean install, inside an emulator. They will not work on actual devices. All tests are also designed for a particular screen size, namely the Nexus 4 configuration (4.7" 768x1280 xhdpi), and a particular locale, namely English (US). Furthermore:

- No additional apps should be installed on the device;
- The homescreen must look exactly like it was when the emulator was originally created, with no additional icons or widgets;
- All animations must be manually disabled.

If there are failing view tests (that is, if some custom views do not render exactly like the prerendered images we have), then both the actual and expected images will be automatically downloaded from the device to the folder `uhabits-android/build/outputs`. After verifying the differences, if you feel that the actual images are actually fine and should replace the prerendered ones, then run `./build.sh android-accept-images`.
