# Changelog

### 2.0.0 (TBD)

* **New Features:**
  * Track numerical habits (@iSoron, @namnl)
  * Skip days without breaking streak (@KristianTashkov)
  * Sort habits by status (@hiqua)
  * Sort habits in reverse order (@iSoron)
  * Add notes to habits (@recheej)
  * Improve readibility of charts (@chennemann)
  * Delay new day until 3am (@KristianTashkov)
  * Export backups daily (@iSoron)
* **Bug fixes:**
  * Reset chart offset when switching scale (@alxmjo)
  * Don't show reminders from archived habits (@KristianTashkov)
  * Lapses on non-daily habits decrease the score too much (@iSoron)
  * Update widgets at midnight (@KristianTashkov)
* **Refactoring:**
  * Convert files to Kotlin (@olegivo)

### 1.8.12 (Jan 30, 2021)

* Fix bug that caused incorrect check marks to show after scrolling (#713)
* Fix issue preventing widgets from updating at midnight (#680)

### 1.8.11 (Dec 29, 2020)

* Fix theme issues on Xiaomi phones

### 1.8.10 (Nov 26, 2020)

* Update translations

### 1.8.9 (Nov 18, 2020)

* Manage exceptions when activities don't exist to handle intents (#181)
* MemoryHabitList: Inherit parent's order (#598)
* Remove notification groups; revert to default system behavior
* Remove SyncManager and Internet permission

### 1.8.8 (June 21, 2020)

* Make small changes to the habit scheduling algorithm, so that "1 time every x days" habits work more predictably.
* Fix crash when saving habit

### 1.8.0 (Jan 1, 2020)

* New bar chart showing number of repetitions performed in each week, month, quarter or year.
* Improved calculation of streaks for non-daily habits: performing habits on irregular weekdays will no longer break your streak.
* Many more colors to choose from (now 20 in total).
* Ability to customize how transparent the widgets are on your home screen.
* Ability to customize the first day of the week.
* Yes/No buttons on notifications, instead of just "Check".
* Automatic dark theme according to phone settings (Android 10).
* Smaller APK and backup files.
* Many other internal code changes improving performance and stability.

### 1.7.11 (Aug 10, 2019)

* Fix bug that produced corrupted CSV files in some countries

### 1.7.10 (June 15, 2019)

* Fix bug that prevented some devices from showing notifications.
* Update targetSdk to Android Pie (API level 28)

### 1.7.8 (April 21, 2018)

* Add support for adaptive icons (Oreo)
* Add support for notification channels (Oreo)
* Update translations

### 1.7.7 (September 30, 2017)

* Fix bug that caused reminders to show repeatedly on DST changes

### 1.7.6 (July 18, 2017)

* Fix bug that caused widgets not to render sometimes
* Fix other minor bugs
* Update translations

### 1.7.3 (May 30, 2017)

* Improve performance of 'sort by score'
* Other minor bug fixes

### 1.7.2 (May 27, 2017)

* Fix crash at startup

### 1.7.1 (May 21, 2017)

* Fix crash (BadParcelableException)
* Fix layout for RTL languages such as Arabic
* Automatically detect and reject invalid database files
* Add Hebrew translation

### 1.7.0 (Mar 31, 2017)

* Sort habits automatically
* Allow swiping the header to see previous days
* Import backups directly from Google Drive or Dropbox
* Refresh data automatically at midnight
* Other minor bug fixes and enhancements

### 1.6.2 (Oct 13, 2016)

* Fix crash on Android 4.1

### 1.6.1 (Oct 10, 2016)

* Fix a crash at startup when database is corrupted

### 1.6.0 (Oct 10, 2016)

* Add option to make notifications sticky
* Add option to hide completed habits
* Display total number of repetitions for each habit
* Pebble integration: check/snooze habits from the watch
* Tasker/Locale integration: allow third-party apps to add checkmarks
* Export an unified CSV file, with checkmarks for all the habits
* Increase width of name column according to screen size
* Stop showing reminders for archived habits
* Add Danish, Dutch, Greek, Hindi and Portuguese (PT) translations
* Other minor fixes and enhancements

### 1.5.6 (Jun 19, 2016)

* Fix bug that prevented checkmark widget from working

### 1.5.5 (Jun 19, 2016)

* Fix bug that prevented check button on notification to work sometimes
* Fix bug that caused back button to apparently erase some checkmarks
* Complete French translation
* Add Croatian and Slovenian translations

### 1.5.4 (May 29, 2016)

* Fix crash upon opening settings screen in some phones
* Fix missing folders in CSV archive
* Add Serbian translation

### 1.5.3 (May 22, 2016)

* Complete Arabic and Czech translations
* Fix crash at startup
* Fix checkmark widget on custom launchers

### 1.5.2 (May 19, 2016)

* Fix missing attachment on bug reports
* Fix bug that prevents some widgets from rendering
* Complete Japanese translation

### 1.5.1 (May 17, 2016)

* Fix build on F-Droid

### 1.5.0 (May 15, 2016)

* Add night mode, with AMOLED support
* Backport material design to older devices
* Display more information on statistics screen
* Display score on main screen and checkmark widget
* Make widgets react immediately to touch
* Reschedule reminders after reboot
* Pick first day of the week according to country
* Add option to reverse order of days on main screen
* Add option to change notification sounds
* Add Catalan, Indonesian, Turkish, Ukrainian translations
* Switch between Simplified/Traditional Chinese according to country

### 1.4.1 (April 9, 2016)

* Show error message on widgets, instead of crashing
* Complete French translation
* Minor fixes to other translations

### 1.4.0 (April 7, 2016)

* Ability to import data from third-party apps
* Ability to save and restore full database backup
* Show more information on streak chart
* Simplify interface for creating habits
* Add link to Frequently Asked Questions (FAQ)
* Reduce app loading time and lag on widgets
* Generate bug reports on crash and from settings screen
* Disable vibration according to phone settings
* Add Czech translation
* Fix wrong month names for some languages

### 1.3.3 (March 20, 2016)

* Add Spanish and Korean translations
* Make small corrections to other translations
* Fix incorrect date in history calendar

### 1.3.2 (March 18, 2016)

* Add Arabic, Italian, Polish, Russian and Swedish translations
* Minor fixes to German and French translations
* Minor bug fixes

### 1.3.1 (March 15, 2016)

* Fixes crash on devices with large screen, such as the Nexus 10
* Fixes crash when clicking widgets and reminders of deleted habits
* Other minor bug fixes

### 1.3.0 (March 12, 2016)

* New frequency plot: view total repetitions per day of week
* New history editor: put checkmarks in the past
* Add German, French and Japanese translations
* Add about screen, with credits to all contributors
* Fix small bug that prevented habit from being reordered
* Fix small bug caused by rotating the device

### 1.2.0 (March 4, 2016)

* Ability to export habit data as CSV
* Widgets (checkmark, history, score and streaks)
* More natural scrolling on data views (fling)
* Minor UI improvements on pre-Lollipop devices
* Fix crash on Samsung Galaxy TabS 8.4
* Other minor bug fixes

### 1.1.1 (February 24, 2016)

* Show reminder only on chosen days of the week
* Rearrange habits by long-pressing then dragging
* Select and modify multiple habits simultaneously
* 12/24 hour format according to phone preferences
* Permanently delete habits
* Usage hints during startup
* Translation to Brazilian Portuguese and Chinese
* Other minor fixes

### 1.0.0 (February 19, 2016)

* Initial release
