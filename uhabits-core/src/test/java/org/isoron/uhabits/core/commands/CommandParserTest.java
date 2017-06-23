/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.core.commands;

import android.support.annotation.*;

import org.hamcrest.*;
import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.json.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;

public class CommandParserTest extends BaseUnitTest
{
    @NonNull
    private CommandParser parser;

    private Habit habit;

    private List<Habit> selected;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        parser = new CommandParser(habitList, modelFactory);
        habit = fixtures.createShortHabit();
        selected = Collections.singletonList(habit);
        habitList.add(habit);
    }

    @Test
    public void testDecodeArchiveCommand() throws JSONException
    {
        ArchiveHabitsCommand original, decoded;
        original = new ArchiveHabitsCommand(habitList, selected);
        decoded = (ArchiveHabitsCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.selected, equalTo(original.selected));
    }

    @Test
    public void testDecodeChangeColorCommand() throws JSONException
    {
        ChangeHabitColorCommand original, decoded;
        original = new ChangeHabitColorCommand(habitList, selected, 20);
        decoded = (ChangeHabitColorCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.newColor, equalTo(original.newColor));
        MatcherAssert.assertThat(decoded.selected, equalTo(original.selected));
    }

    @Test
    public void testDecodeCreateHabitCommand() throws JSONException
    {
        Habit model = modelFactory.buildHabit();
        model.setName("JSON");

        CreateHabitCommand original, decoded;
        original = new CreateHabitCommand(modelFactory, habitList, model);
        original.execute();

        decoded = (CreateHabitCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.savedId, equalTo(original.savedId));
        MatcherAssert.assertThat(decoded.model.getData(), equalTo(model
            .getData()));
    }

    @Test
    public void testDecodeCreateRepCommand() throws JSONException
    {
        CreateRepetitionCommand original, decoded;
        original = new CreateRepetitionCommand(habit, 1000, 5);
        decoded = (CreateRepetitionCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.timestamp, equalTo(original
            .timestamp));
        MatcherAssert.assertThat(decoded.value, equalTo(original.value));
        MatcherAssert.assertThat(decoded.habit, equalTo(original.habit));
    }

    @Test
    public void testDecodeDeleteCommand() throws JSONException
    {
        DeleteHabitsCommand original, decoded;
        original = new DeleteHabitsCommand(habitList, selected);
        decoded = (DeleteHabitsCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.selected, equalTo(original.selected));
    }

    @Test
    public void testDecodeEditHabitCommand() throws JSONException
    {
        Habit modified = modelFactory.buildHabit();
        modified.setName("Edited JSON");
        modified.setColor(2);

        EditHabitCommand original, decoded;
        original = new EditHabitCommand(modelFactory, habitList, habit, modified);
        original.execute();

        decoded = (EditHabitCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.savedId, equalTo(original.savedId));
        MatcherAssert.assertThat(decoded.modified.getData(), equalTo(modified
            .getData()));
    }

    @Test
    public void testDecodeToggleCommand() throws JSONException
    {
        ToggleRepetitionCommand original, decoded;
        original = new ToggleRepetitionCommand(habitList, habit, 1000);
        decoded = (ToggleRepetitionCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.timestamp, equalTo(original
            .timestamp));
        MatcherAssert.assertThat(decoded.habit, equalTo(original.habit));
    }

    @Test
    public void testDecodeUnarchiveCommand() throws JSONException
    {
        UnarchiveHabitsCommand original, decoded;
        original = new UnarchiveHabitsCommand(habitList, selected);
        decoded = (UnarchiveHabitsCommand) parser.parse(original.toJson());

        MatcherAssert.assertThat(decoded.getId(), equalTo(original.getId()));
        MatcherAssert.assertThat(decoded.selected, equalTo(original.selected));
    }
}