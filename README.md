# Loop Habit Tracker

Loop is a simple Android app that helps you create and maintain good habits,
allowing you to achieve your long-term goals. Detailed graphs and statistics
show you how your habits improved over time. It is completely ad-free and open
source.

<a href="https://play.google.com/store/apps/details?id=org.isoron.uhabits&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-AC-global-none-all-co-pr-py-PartBadges-Oct1515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge-border.png" width="200px"/></a>

## Features

* **Simple, beautiful and modern interface.** Loop has a minimalistic interface
  that is easy to use and follows the material design guidelines.

* **Habit score.** In addition to showing your current streak, Loop has an
  advanced algorithm for calculating the strength of your habits. Every
  repetition makes your habit stronger, and every missed day makes it weaker. A
  few missed days after a long streak, however, will not completely destroy
  your entire progress.

* **Detailed graphs and statistics.** Clearly see how your habits improved over
  time with beautiful and detailed graphs. Scroll back to see the complete
  history of your habits.

* **Flexible schedules.** Supports both daily habits and habits with more
  complex schedules, such as 3 times every week; one time every other week; or
  every other day.

* **Reminders.** Create an individual reminder for each habit, at a chosen hour
  of the day. Easily check, dismiss or snooze your habit directly from the
  notification, without opening the app.

* **Optimized for smartwatches.** Reminders can be checked, snoozed or
  dismissed directly from your Android Wear watch.

* **Completely ad-free and open source.** There are absolutely no
  advertisements, annoying notifications or intrusive permissions in this app,
  and there will never be. The complete source code is available under the
  GPLv3.

## Screenshots

[![Main screen][screen1th]][screen1]
[![Edit habit][screen2th]][screen2]
[![Habit strength][screen3th]][screen3]
[![Habit history and streaks][screen4th]][screen4]
[![Widgets][screen5th]][screen5]

## Contributing

Loop is an open source project developed entirely by volunteers. If you would
like to contribute to the project, you are very welcome. There are many ways to
contribute, even if you are not a software developer.

* **Report bugs, suggest features.** The easiest way to contribute is to simply
  use the app and let us know if you find any problems or have any suggestions
  to improve it. You can either use the link inside the app, or open an issue
  at GitHub.

* **Translate the app into your own language.** If you are not a native English
  speaker, and would like to see the app translated into your own language,
  please join our [open translation project at POEditor][poedit].

* **Write some code.** If you are an Android developer, you are very welcome to
  contribute with code.  If you already have an idea in mind, you can either
  implement it and then send a pull request, or open an issue first, to discuss
  your idea before you start your work. If you are out of ideas, have a look at
  the open issues.  Tasks that are easier to implement have the green label
  *small-task*.  This repository uses the [git-flow branching model][gitflow].
  Please, submit pull requests against the dev branch. This is the branch that
  will eventually become the next version of the app.

## Installing

The easiest way to install Loop is through the [Google Play Store][playstore].
You may also download and install the APK from the [releases page][releases];
note, however, that the app will not be updated automatically.  To build this
app from the source code, you will need to install the [Android SDK
Manager][sdk], then run the following commands.

    git clone https://github.com/iSoron/uhabits.git
    cd uhabits
    git submodule update --init
    ./gradlew assembleDebug

The generated APK will be located in `app/build/outputs/apk/`. It can be
installed on the device through [adb][adb]:

    adb install app/build/outputs/apk/app-debug.apk


## License

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program.  If not, see <http://www.gnu.org/licenses/>.

[screen1]: screenshots/original/uhabits1.png
[screen2]: screenshots/original/uhabits2.png
[screen3]: screenshots/original/uhabits3.png
[screen4]: screenshots/original/uhabits4.png
[screen5]: screenshots/original/uhabits5.png
[screen1th]: screenshots/thumbs/uhabits1.png
[screen2th]: screenshots/thumbs/uhabits2.png
[screen3th]: screenshots/thumbs/uhabits3.png
[screen4th]: screenshots/thumbs/uhabits4.png
[screen5th]: screenshots/thumbs/uhabits5.png
[poedit]: https://poeditor.com/join/project/8DWX5pfjS0
[gitflow]: http://nvie.com/posts/a-successful-git-branching-model/
[sdk]: https://developer.android.com/sdk/installing/index.html?pkg=studio
[playstore]: https://play.google.com/store/apps/details?id=org.isoron.uhabits
[releases]: https://github.com/iSoron/uhabits/releases
[adb]: https://developer.android.com/tools/help/adb.html
