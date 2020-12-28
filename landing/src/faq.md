## Frequently Asked Questions

### Creating and tracking habits

#### How can I track bad habits I want to break?

Loop can currently only supports tracking positive habits. Instead of tracking bad habits, an alternative is to rephrase your habit in a positive way. For example, instead of having the bad habit "Did you smoke today?" you could have the good habit "Did you have a smoke-free day today?".  Another example, instead of "Did you eat junk food today?" you could have "Did you eat healthy today?".

#### How can I specify that a habit should be performed on certain days of the week?

You don't need to specify this. The app will figure it out by itself. You just
need to specify how many times would you like to repeat the habit in a certain
interval of time. For example, if you want to repeat a certain habit every
Monday and Friday, simply specify that you want to repeat the habit 2 times per
week, and then start practicing your habit. If, during your first week, you
indeed perform your habit on Monday and Friday, the app will automatically add
a checkmark on the following Saturday and Sunday. As long as you keep your
routine, the checkmarks should appear automatically on the correct days.

#### When I am creating a new habit, what am I supposed to write on the "question" field?

The question field is a place where you can specify more clearly the
requirements for the habit. For example, while the title of your habit might
say "Meditate", the question might ask "Did you meditate for at least 10
minutes this morning?". This question appears on the reminders.

#### How can I put checkmarks in past days?

Open the statistics page for a certain habit, scroll down to the "Calendar"
section and press the button "Edit" located under the calendar. Then, tap on
the days to check or uncheck. You can also scroll sideways to change days
further back in time.

#### How can I see the statistics page for a certain habit?

Just tap on the name of the habit.

#### How can I reorder the habits?

First, press-and-hold the name of a habit for a second. Then, when it becomes
selected, just drag it to the desired place, without lifting your finger.

#### How can I disable the vibration every time I put a checkmark?

Loop follows your phone's settings to decide whether it should vibrate on touch
or not. To disable the vibration, open your phone's settings, tap "Sounds"
and disable "Vibrate on touch".

#### Why are some checkmarks gray?

The color gray indicates that the checkmark was automatically added by the
app, and not manually by the user. This happens with non-daily habits. For
example, if you have a habit that should be repeated only one time every week,
and you perform it on a certain Monday, the app will automatically add gray
checkmarks on Tuesday, Wednesday an so on.

#### How can I delete a habit?

On the main screen, press-and-hold the name of the habit for a second. When it
becomes selected, tap the overflow icon on the menu bar (it looks like three
vertical dots), then select delete.

#### How is the score calculated?

To compute the score of a habit, the app uses a statistical method known as
exponential smoothing. Basically, it computes a weighted average that takes
into consideration every repetition of the habit, from the very first day you
started your habit until today. Recent repetitions, however, are considered
more important than older ones, and have a larger impact on the score. This
method has many nice features, including:

1. Every repetition counts, even way back in the past. This is not true for
   other simpler formulas, such as counting how many times have you performed
   the habit in the last week/month/year and then dividing by the number of
   days in that interval.
2. If you have a poor habit score, then a few repetitions can bring your score
   up very quickly. As your score improves, however, the reward for each
   repetition gets smaller and smaller, so you have to keep at it if you want
   to see further increases.
3. If you have a high score for a very long time and you take a short break,
   it's quite easy to restore your score back to what it was. If you start
   taking frequent breaks, however, then your score will suffer.

#### How long will it take to reach the highest score?

If you perform a daily habit perfectly, the score reaches 80% after one month,
96% after two months and 99% after three months. For non-daily habits, it takes
longer to reach the same percentages. If your habit is repeated every other
day, for example, then it takes two months to reach 80%, and if you habit is
weekly, then it takes seven months.

### Importing and exporting data

#### If I uninstall the app, will I lose all my data?

Yes, uninstalling the app will delete all your data! All files inside the
folder `Android » data » org.isoron.uhabits` on the SD card will also be deleted.
This is the way Android was designed, and there is nothing Loop can do to
prevent it.

#### If my phone gets damaged or factory reset, will I lose all my data?

Loop supports Android's native backup service: if you have backups activated in
your phone settings (Settings » Backup & Restore » Back up my data), it should
periodically save a copy of your data in the cloud automatically. The data will
be restored in case you factory-reset your phone or activate a new device.
**Note however that uninstalling the app will delete all your data, in addition
to all the backups stored in the cloud**. Furthermore, this backup system can be unreliable at
times. Therefore, if you want to make sure that your data is safely backed up,
it is still recommended that you use a specialized backup utility, such as
Titanium Backup. Alternatively, you can export a full backup of your data
manually. For more details, see the answer for the next question.

#### How can I export a full backup of my data?

Select the option "Export full backup" on the settings screen. A file will be
generated, and you will be asked to choose an app to share it to. If you select
an email application, you will be able to send the file as an attachment to
yourself. If you select Google Drive, Dropbox or similar apps, you will be able
to upload it. A copy of the generated file is also automatically saved on the
SD card, inside the folder `Android » data » org.isoron.uhabits » files`. Be aware that
this folder will be deleted when the app is uninstalled. If you would like to
keep the backups on the SD card after the app is uninstalled, you should move
them to another folder.

#### How can I restore a full backup?


First, go to the settings screen and tap "Import data". A file browser should appear. Tap the menu icon (the one with three vertical lines) and select the app where your backup is stored, such as Google Drive.

If your backup file is located in your SD card, after tapping "Import data", tap the icon with three dots on the top right corner of the screen and select "Show internal storage". Then, tap the menu icon (the one with three vertical lines) and select your SD card.

#### How can I export my data as CSV?

Open the settings screen, scroll down to the section "Database" and select the
option "Export as CSV". The data will be exported as a ZIP archive containing
individual comma-separated files for each habit, which can be opened with any
modern spreadsheet software, such as Microsoft Excel or OpenOffice Calc.

### Troubleshooting

#### I have installed the iOS version of the app, but it refuses to import my backup file.

There is currently no iOS version of Loop Habit Tracker. You have probably installed an unauthorized copycat version of our app. Please contact us at <dev@loophabits.org> and let us know the application name, so that we can contact Apple to remove the copycat app from the App Store. Note that copycat versions of Loop are typically not open source and there is no guarantee that they will keep your data private.

#### When I add a checkmark, the app deletes my habit!

You have probably accidentally enabled the "Hide completed habits" option. To disable it, open the main screen, tap the filter icon (the one with three horizontal lines), then tap "Hide completed".

#### Why are the reminders not working?

There are many reasons why the reminders may not appear:

* Loop will only show you a reminder if the task has not been already completed
  that day. For example, if your reminder is scheduled to show up at 8am, but
  you already added a checkmark manually (on the main app) at 7am, then the
  reminder will not appear.  The same is true for non-daily habits. If you have
  a weekly habit, and you complete it on a certain Monday, the reminder will
  only appear on the following Monday.
* When you create a habit, you can also choose what days are the reminder
  allowed to appear. If you create a weekly habit, but set the reminders to
  appear only on the weekends, for example, then they will never appear Monday
  to Friday, regardless of you having completed the habit during the weekend or
  not.
* If you are sure that the reminders should appear, but they don't, please,
  make sure that your phone has no task killers or RAM boosters installed.
  These apps are known to disable Loop's reminders. If your phone has an
  aggressive power saving mode, such as Samsung's Ultra Power Saving mode or
  Sony's Stamina Mode, please make sure that Loop is whitelisted.

#### I can't find Loop's widgets on the list. How can I add them?

* Some phones don't display the name of the app the widgets belong to.  In that
  case, the widgets will be called simply "Checkmark", "Frequency", "Habit
  Strength", "History" and "Streaks".
* If you still can't find the widgets, make sure that Loop is not installed on
  the SD card. Moving apps to the SD card disables many features, including
  widgets.

### Other questions

#### How can I donate to the project?

Loop currently does not accept any monetary donations. There are many other
ways you can contribute to the project, however, even if you are not a software
developer, such as providing feedback, finding bugs, sharing the app with your
friends, rating and writing a review on Google Play Store, or translating the
app into your language. Please, [see our webpage at GitHub][contributing] for
more details.

#### My question was not answered in this FAQ. What should I do?

Please, feel free to contact the developer at <dev@loophabits.org>. 

[contributing]: https://github.com/iSoron/uhabits/
