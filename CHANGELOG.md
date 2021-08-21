# Changelog

## [2.0.3] - 2021-08-21
### Fixed
- Improve automatic checkmarks for monthly habits (@iSoron, #947)
- Fix small theme issues (@iSoron)
- Fix ANR on some Samsung phones (@iSoron, #962)
- Fix dates before the year 2000 (@iSoron, #967)
- Fix notification adding checkmarks to the wrong day (@hiqua, #969)
- Fix crashes in widgets (@hiqua, @iSoron, #907, #966, #965)
- Fix crash when moving habits (@hiqua, #968)

## [2.0.2] - 2021-05-23

### Changed
- Make checkmark widget resizable

### Fixed
- Fix crash caused by numerical habits with zero target (@iSoron, #903)
- Fix small issues with font size (@iSoron)
- Allow fractional target values (@sumanabhi, #911)
- Fix IllegalStateException in androidx.customview.view (@iSoron, #906)
- Fix crash when selecting habit frequency in some languages (@iSoron, #926)
- Fix IllegalArgumentException in RingView (@iSoron, #904)

## [2.0.1] - 2021-05-09

### Added
- Make midnight delay optional and disabled by default (@hiqua)
- Add arrows to sort menu (@iSoron) 

### Removed
- Temporarily remove experimental device sync functionality. This feature will be re-added in
  Loop 2.1.

### Changed
- Make implicit checkmarks easier to read (@iSoron)
- Update and improve list of translators (@hiqua, @iSoron)

### Fixed
- Disable transparency for stacked widgets (@hiqua)
- Fix various color issues on the dark theme (@hiqua, @iSoron)
- Fix "customize notifications" on older devices (@hiqua)
- Fix snooze button in notifications when device is locked (@hiqua)
- Fix a crash when deleting habits (@engineering4good)
- Fix checkmark widget not rendering properly on some Samsung phones (@iSoron)

### Refactoring & Testing
- Finish conversion of the entire project to Kotlin (@hiqua, @iSoron, @MarKco)
- Automatically run large tests on GitHub Actions (@iSoron)
- Remove unused v21 resources (@hiqua)

## [2.0.0-alpha] - 2020-11-29

### Added
- Track numeric habits (@iSoron, @namnl)
- Skip days without breaking streak (@KristianTashkov)
- Sort habits by status (@hiqua)
- Sort habits in reverse order (@iSoron)
- Add notes to habits (@recheej)
- Improve readibility of charts (@chennemann)
- Delay new day until 3am (@KristianTashkov)
- Export backups daily (@iSoron)

### Removed
- Drop support to devices older than Android 6.0 (API 23)

### Fixed
- Reset chart offset when switching scale (@alxmjo)
- Don't show reminders from archived habits (@KristianTashkov)
- Lapses on non-daily habits decrease the score too much (@iSoron)
- Update widgets at midnight (@KristianTashkov)

### Refactoring
- Convert files to Kotlin (@olegivo)

## [1.8.12] - 2021-01-30

- Fix bug that caused incorrect check marks to show after scrolling (#713)
- Fix issue preventing widgets from updating at midnight (#680)

## [1.8.11] - 2020-12-29

- Fix theme issues on Xiaomi phones

## [1.8.10] - 2020-11-26

- Update translations

## [1.8.9] - 2020-11-18

- Manage exceptions when activities don't exist to handle intents (#181)
- MemoryHabitList: Inherit parent's order (#598)
- Remove notification groups; revert to default system behavior
- Remove SyncManager and Internet permission

## [1.8.8] - 2020-06-21

- Make small changes to the habit scheduling algorithm, so that "1 time every x days" habits work more predictably.
- Fix crash when saving habit

## [1.8.0] - 2020-01-01

- New bar chart showing number of repetitions performed in each week, month, quarter or year.
- Improved calculation of streaks for non-daily habits: performing habits on irregular weekdays will no longer break your streak.
- Many more colors to choose from (now 20 in total).
- Ability to customize how transparent the widgets are on your home screen.
- Ability to customize the first day of the week.
- Yes/No buttons on notifications, instead of just "Check".
- Automatic dark theme according to phone settings (Android 10).
- Smaller APK and backup files.
- Many other internal code changes improving performance and stability.

## [1.7.11] - 2019-08-10

- Fix bug that produced corrupted CSV files in some countries

## [1.7.10] - 2019-06-15

- Fix bug that prevented some devices from showing notifications.
- Update targetSdk to Android Pie (API level 28)

## [1.7.8] - 2018-04-21

- Add support for adaptive icons (Oreo)
- Add support for notification channels (Oreo)
- Update translations

## [1.7.7] - 2017-09-30

- Fix bug that caused reminders to show repeatedly on DST changes

## [1.7.6] - 2017-07-18

- Fix bug that caused widgets not to render sometimes
- Fix other minor bugs
- Update translations

## [1.7.3] - 2017-05-30

- Improve performance of 'sort by score'
- Other minor bug fixes

## [1.7.2] - 2017-05-27

- Fix crash at startup

## [1.7.1] - 2017-05-21

- Fix crash (BadParcelableException)
- Fix layout for RTL languages such as Arabic
- Automatically detect and reject invalid database files
- Add Hebrew translation

## [1.7.0] - 2017-03-31

- Sort habits automatically
- Allow swiping the header to see previous days
- Import backups directly from Google Drive or Dropbox
- Refresh data automatically at midnight
- Other minor bug fixes and enhancements

## [1.6.2] - 2016-10-13

- Fix crash on Android 4.1

## [1.6.1] - 2016-10-10

- Fix a crash at startup when database is corrupted

## [1.6.0] - 2016-10-10

- Add option to make notifications sticky
- Add option to hide completed habits
- Display total number of repetitions for each habit
- Pebble integration: check/snooze habits from the watch
- Tasker/Locale integration: allow third-party apps to add checkmarks
- Export an unified CSV file, with checkmarks for all the habits
- Increase width of name column according to screen size
- Stop showing reminders for archived habits
- Add Danish, Dutch, Greek, Hindi and Portuguese (PT) translations
- Other minor fixes and enhancements

## [1.5.6] - 2016-06-19

- Fix bug that prevented checkmark widget from working

## [1.5.5] - 2016-06-19

- Fix bug that prevented check button on notification to work sometimes
- Fix bug that caused back button to apparently erase some checkmarks
- Complete French translation
- Add Croatian and Slovenian translations

## [1.5.4] - 2016-05-29

- Fix crash upon opening settings screen in some phones
- Fix missing folders in CSV archive
- Add Serbian translation

## [1.5.3] - 2016-05-22

- Complete Arabic and Czech translations
- Fix crash at startup
- Fix checkmark widget on custom launchers

## [1.5.2] - 2016-05-19

- Fix missing attachment on bug reports
- Fix bug that prevents some widgets from rendering
- Complete Japanese translation

## [1.5.1] - 2016-05-17

- Fix build on F-Droid

## [1.5.0] - 2016-05-15

- Add night mode, with AMOLED support
- Backport material design to older devices
- Display more information on statistics screen
- Display score on main screen and checkmark widget
- Make widgets react immediately to touch
- Reschedule reminders after reboot
- Pick first day of the week according to country
- Add option to reverse order of days on main screen
- Add option to change notification sounds
- Add Catalan, Indonesian, Turkish, Ukrainian translations
- Switch between Simplified/Traditional Chinese according to country

## [1.4.1] - 2016-04-09

- Show error message on widgets, instead of crashing
- Complete French translation
- Minor fixes to other translations

## [1.4.0] - 2016-04-07

- Ability to import data from third-party apps
- Ability to save and restore full database backup
- Show more information on streak chart
- Simplify interface for creating habits
- Add link to Frequently Asked Questions (FAQ)
- Reduce app loading time and lag on widgets
- Generate bug reports on crash and from settings screen
- Disable vibration according to phone settings
- Add Czech translation
- Fix wrong month names for some languages

## [1.3.3] - 2016-03-20

- Add Spanish and Korean translations
- Make small corrections to other translations
- Fix incorrect date in history calendar

## [1.3.2] - 2016-03-18

- Add Arabic, Italian, Polish, Russian and Swedish translations
- Minor fixes to German and French translations
- Minor bug fixes

## [1.3.1] - 2016-03-15

- Fixes crash on devices with large screen, such as the Nexus 10
- Fixes crash when clicking widgets and reminders of deleted habits
- Other minor bug fixes

## [1.3.0] - 2016-03-12

- New frequency plot: view total repetitions per day of week
- New history editor: put checkmarks in the past
- Add German, French and Japanese translations
- Add about screen, with credits to all contributors
- Fix small bug that prevented habit from being reordered
- Fix small bug caused by rotating the device

## [1.2.0] - 2016-03-04

- Ability to export habit data as CSV
- Widgets (checkmark, history, score and streaks)
- More natural scrolling on data views (fling)
- Minor UI improvements on pre-Lollipop devices
- Fix crash on Samsung Galaxy TabS 8.4
- Other minor bug fixes

## [1.1.1] - 2016-02-24

- Show reminder only on chosen days of the week
- Rearrange habits by long-pressing then dragging
- Select and modify multiple habits simultaneously
- 12/24 hour format according to phone preferences
- Permanently delete habits
- Usage hints during startup
- Translation to Brazilian Portuguese and Chinese
- Other minor fixes

## [1.0.0] - 2016-02-19

- Initial release
