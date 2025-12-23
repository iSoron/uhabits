# Reminder Import Investigation

## User Question

> "on old app i had reminders for many habits, in this version, even though i had imported some data then appended/updated daily basis the reminders are gone, was it not stored/handled based on .db file?"

## Answer: Reminders ARE Imported from .db Files

### Database Schema

Reminders are stored in the `habits` table with these columns ([HabitRecord.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmMain\java\org\isoron\uhabits\core\models\sqlite\records\HabitRecord.kt#L52-L64)):

```kotlin
@field:Column(name = "reminder_hour")
var reminderHour: Int? = null  // 0-23

@field:Column(name = "reminder_min")
var reminderMin: Int? = null   // 0-59

@field:Column(name = "reminder_days")
var reminderDays: Int? = null  // Bitmask: Monday=1, Tuesday=2, etc.
```

### Import Process

The [LoopDBImporter](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmMain\java\org\isoron\uhabits\core\io\LoopDBImporter.kt) handles .db file imports:

```kotlin
fun copyTo(habit: Habit) {
    // ... other fields ...
    if (reminderHour != null && reminderMin != null) {
        habit.reminder = Reminder(
            reminderHour!!,
            reminderMin!!,
            WeekdayList(reminderDays!!)
        )
    }
}
```

**Verification from Tests:**
[ImportTest.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmTest\java\org\isoron\uhabits\core\io\ImportTest.kt#L144-L149) confirms reminders are imported:

```kotlin
@Test
fun testRewireDB() {
    importFromFile("rewire.db")
    val habit = habitList.getByPosition(2)
    assertThat(habit.hasReminder(), equalTo(true))
    val reminder = habit.reminder
    assertThat(reminder!!.hour, equalTo(8))
    assertThat(reminder.minute, equalTo(0))
    // ...
}
```

## Why Reminders Might Disappear

### Possible Causes

#### 1. Update vs Import Behavior

From [LoopDBImporter.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmMain\java\org\isoron\uhabits\core\io\LoopDBImporter.kt#L71-L93):

```kotlin
for (habitRecord in habitsRepository.findAll("order by position")) {
    var habit = habitList.getByUUID(habitRecord.uuid)
    
    if (habit == null) {
        // NEW HABIT: Full import including reminders
        habit = modelFactory.buildHabit()
        habitRecord.copyTo(habit)
        CreateHabitCommand(...).run()
    } else {
        // EXISTING HABIT: Update via EditHabitCommand
        val modified = modelFactory.buildHabit()
        habitRecord.copyTo(modified)
        EditHabitCommand(habitList, habit.id!!, modified).run()
    }
}
```

**Key Insight:**
- If habit already exists (matched by UUID), `EditHabitCommand` is used
- If `EditHabitCommand` doesn't properly preserve reminders, they could be lost
- **This is the most likely cause**: User imported once (reminders set), then imported again (reminders overwritten)

#### 2. Reminder Rescheduling Not Triggered

After import, reminders need to be scheduled with Android's AlarmManager. If [ReminderScheduler.scheduleAll()](c:\Users\w11-d\src\gh\uhabits\uhabits-core\src\jvmMain\java\org\isoron\uhabits\core\reminders\ReminderScheduler.kt) is not called after import, reminders won't trigger even though they're in the database.

#### 3. Permission Issues (Android 13+)

On Android 13+, apps need `POST_NOTIFICATIONS` permission. If denied, reminders won't show even if scheduled. From [ListHabitsActivity.kt](c:\Users\w11-d\src\gh\uhabits\uhabits-android\src\main\java\org\isoron\uhabits\activities\habits\list\ListHabitsActivity.kt#L67-L73):

```kotlin
registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
    if (isGranted) {
        scheduleReminders()
    } else {
        Log.i("ListHabitsActivity", "POST_NOTIFICATIONS denied")
    }
}
```

#### 4. App Update Migration

If user updated from very old version, database migrations might have issues. However, the current codebase shows reminders are part of schema from early versions.

## How to Verify Reminders in Database

### Check Database Directly

1. Export full backup (.db file) from Settings → Database → Export full backup
2. Open with SQLite browser/viewer
3. Query:
   ```sql
   SELECT name, reminder_hour, reminder_min, reminder_days 
   FROM habits 
   WHERE reminder_hour IS NOT NULL;
   ```
4. If query returns rows: reminders ARE in database
5. If empty: reminders were not imported or were cleared

### Check in App

1. Open habit details
2. Check "Reminder" section
3. If shows "No reminder": reminder is not set in database
4. If shows time: reminder exists but may not be scheduled

## Solution: How to Restore Reminders

### Method 1: Re-import Original Backup (Fresh Import)

1. Uninstall app (if safe to lose recent entries)
2. Reinstall
3. Import original .db file from old app
4. Grant notification permission when prompted
5. All reminders should be restored and scheduled

### Method 2: Manual Re-addition (If Recent Data Important)

1. For each habit:
   - Open habit details
   - Tap "Reminder"
   - Set reminder time and days
   - Save

### Method 3: Export-Modify-Import (Advanced)

1. Export current database
2. Open with SQLite editor
3. Update `reminder_hour`, `reminder_min`, `reminder_days` from old backup
4. Import modified database
5. Restart app

## Technical Fix Needed

### Root Cause

If reminders disappear after "append/update daily basis", the issue is likely in how the app handles:
1. Repeated imports of same habits (matched by UUID)
2. Reminder scheduling after habit edits

### Recommended Fix

**File**: `EditHabitCommand.kt` or import logic

Ensure that when updating existing habit from import:
```kotlin
// When importing habit that already exists:
if (modified.reminder != null && habit.reminder == null) {
    // Don't lose reminder if new import has it but current habit doesn't
    habit.reminder = modified.reminder
}

// After habit update, reschedule reminders:
reminderScheduler.schedule(habit)
```

**File**: `LoopDBImporter.kt`

After all habits imported:
```kotlin
override fun importHabitsFromFile(file: File) {
    // ... existing import logic ...
    
    // Schedule all reminders after import
    reminderScheduler.scheduleAll()
    
    db.close()
}
```

## Conclusion

**Reminders ARE stored and imported from .db files** — the schema and import code fully support them. The issue is likely:

1. **Most likely**: Repeated imports overwrite existing habits, and `EditHabitCommand` may not properly preserve reminders
2. **Second likely**: Reminders imported but not rescheduled with AlarmManager
3. **Less likely**: Notification permission denied on Android 13+

**User Action:**
- Check Settings → Notifications → uHabits → Notifications enabled
- Check individual habit settings to see if reminders exist in UI
- Try setting one reminder manually to test if scheduling works
- If manual reminders work but imported ones don't, file bug report with database export

**Developer Action (if confirmed bug):**
- Ensure `EditHabitCommand` preserves reminders when updating habits during import
- Call `reminderScheduler.scheduleAll()` after import completes
- Add logging to track reminder import and scheduling
