package org.isoron.uhabits;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.isoron.uhabits.models.Habit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.isoron.uhabits.HabitMatchers.containsHabit;
import static org.isoron.uhabits.HabitMatchers.withName;
import static org.isoron.uhabits.HabitViewActions.toggleAllCheckmarks;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest
{
    @Rule
    public IntentsTestRule<MainActivity> activityRule = new IntentsTestRule<>(
            MainActivity.class);

    @Before
    public void skipTutorial()
    {
        try
        {
            for (int i = 0; i < 10; i++)
                onView(allOf(withClassName(endsWith("AppCompatImageButton")),
                        isDisplayed())).perform(click());
        }
        catch (NoMatchingViewException e)
        {
            // ignored
        }
    }

    public String addHabit()
    {
        return addHabit(false);
    }

    public String addHabit(boolean openDialogs)
    {
        String name = "New Habit " + new Random().nextInt(1000000);
        String description = "Did you perform your new habit today?";
        String num = "4";
        String den = "8";

        onView(withId(R.id.action_add))
                .perform(click());

        typeHabitData(name, description, num, den);

        if(openDialogs)
        {
            onView(withId(R.id.buttonPickColor))
                    .perform(click());
            pressBack();
            onView(withId(R.id.inputReminderTime))
                    .perform(click());
            onView(withText("Done"))
                    .perform(click());
            onView(withId(R.id.inputReminderDays))
                    .perform(click());
            onView(withText("OK"))
                    .perform(click());
        }

        onView(withId(R.id.buttonSave))
                .perform(click());

        onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                .onChildView(withId(R.id.label));

        return name;
    }

    private void typeHabitData(String name, String description, String num, String den)
    {
        onView(withId(R.id.input_name))
                .perform(replaceText(name));
        onView(withId(R.id.input_description))
                .perform(replaceText(description));
        onView(withId(R.id.input_freq_num))
                .perform(replaceText(num));
        onView(withId(R.id.input_freq_den))
                .perform(replaceText(den));
    }

    private void selectHabits(List<String> names)
    {
        boolean first = true;
        for(String name : names)
        {
            onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                    .onChildView(withId(R.id.label))
                    .perform(first ? longClick() : click());

            first = false;
        }
    }

    private void assertHabitsDontExist(List<String> names)
    {
        for(String name : names)
            onView(withId(R.id.listView))
                    .check(matches(not(containsHabit(withName(name)))));
    }

    private void assertHabitExists(String name)
    {
        List<String> names = new LinkedList<>();
        names.add(name);
        assertHabitsExist(names);
    }

    private void assertHabitsExist(List<String> names)
    {
        for(String name : names)
            onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                    .check(matches(isDisplayed()));
    }

    private void deleteHabit(String name)
    {
        LinkedList<String> names = new LinkedList<>();
        names.add(name);
        deleteHabits(names);
    }

    private void deleteHabits(List<String> names)
    {
        Context context = InstrumentationRegistry.getTargetContext();

        selectHabits(names);

        openActionBarOverflowOrOptionsMenu(context);

        onView(withText(R.string.delete))
                .perform(click());
        onView(withText("OK"))
                .perform(click());

        assertHabitsDontExist(names);
    }

    @Test
    public void testArchiveHabits()
    {
        List<String> names = new LinkedList<>();
        Context context = InstrumentationRegistry.getTargetContext();

        for(int i = 0; i < 3; i++)
            names.add(addHabit());

        selectHabits(names);
        onView(withContentDescription(R.string.archive))
                .perform(click());
        assertHabitsDontExist(names);

        openActionBarOverflowOrOptionsMenu(context);
        onView(withText(R.string.show_archived))
                .perform(click());

        assertHabitsExist(names);
        deleteHabits(names);
    }

    @Test
    public void testAddInvalidHabit()
    {
        typeHabitData("", "", "15", "7");
        onView(withId(R.id.buttonSave)).perform(click());
        onView(withId(R.id.input_name)).check(matches(isDisplayed()));
    }

    @Test
    public void testToggleCheckmarks()
    {
        String name = addHabit();

        onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                .onChildView(withId(R.id.llButtons))
                .perform(toggleAllCheckmarks());

        deleteHabit(name);
    }

    @Test
    public void testAddHabit()
    {
        String name = addHabit(true);

        onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                .onChildView(withId(R.id.label))
                .perform(click());

        onView(withId(R.id.punchcardView))
                .perform(scrollTo());
    }

    @Test
    public void testEditHabit()
    {
        String name = addHabit();

        onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                .onChildView(withId(R.id.label))
                .perform(longClick());

        onView(withContentDescription(R.string.edit))
                .perform(click());

        String modifiedName = "Modified " + new Random().nextInt(10000);
        typeHabitData(modifiedName, "", "1", "1");

        onView(withId(R.id.buttonSave))
                .perform(click());

        assertHabitExists(modifiedName);
        deleteHabit(modifiedName);
    }
}
