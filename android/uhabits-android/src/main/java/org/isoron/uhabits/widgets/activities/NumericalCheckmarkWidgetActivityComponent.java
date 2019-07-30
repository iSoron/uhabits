package org.isoron.uhabits.widgets.activities;

import android.content.Context;

import org.isoron.androidbase.AppContext;
import org.isoron.androidbase.AppContextModule;
import org.isoron.uhabits.HabitsModule;
import org.isoron.uhabits.core.AppScope;
import org.isoron.uhabits.core.commands.CommandRunner;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.preferences.Preferences;
import org.isoron.uhabits.core.tasks.TaskRunner;
import org.isoron.uhabits.core.ui.widgets.NumericalCheckmarkWidgetBehavior;
import org.isoron.uhabits.tasks.AndroidTaskRunner;

import dagger.Component;

@AppScope
@Component(modules = {
        AppContextModule.class,
        HabitsModule.class,
        AndroidTaskRunner.class,
})
public interface NumericalCheckmarkWidgetActivityComponent {
    NumericalCheckmarkWidgetBehavior getNumericalCheckmarkWidgetBehavior();

    CommandRunner getCommandRunner();

    @AppContext
    Context getContext();

    HabitList getHabitList();

    Preferences getPreferences();

    TaskRunner getTaskRunner();
}
