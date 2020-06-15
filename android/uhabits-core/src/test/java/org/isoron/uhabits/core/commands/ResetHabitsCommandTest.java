package org.isoron.uhabits.core.commands;

        import org.isoron.uhabits.core.*;
        import org.isoron.uhabits.core.models.*;
        import org.junit.*;
        import org.junit.rules.*;

        import java.util.*;

        import static org.hamcrest.CoreMatchers.equalTo;
        import static org.hamcrest.MatcherAssert.assertThat;

public class ResetHabitsCommandTest extends BaseUnitTest
{
    private ResetHabitsCommand command;
    private ResetHabitsCommand command2;
    private HabitList habitList2;


    private LinkedList<Habit> selected;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        selected = new LinkedList<>();

        Long unixTime1 = 5000000L;
        Long unixTimeExtra = 8000000L;

        // Habits that should be reset
        for (int i = 0; i < 3; i++) {
            Habit habit = fixtures.createShortHabit();
            habitList.add(habit);
            selected.add(habit);
            RepetitionList repetitionList = habit.getRepetitions();
            Timestamp ts = new Timestamp(unixTime1);
            unixTime1 += 5000;
            Repetition rep = new Repetition(ts, 1000);
            repetitionList.add(rep);
            repetitionList.toggle(ts);
        }

        // Extra habit that should not be reset
        Habit extraHabit = fixtures.createShortHabit();
        extraHabit.setName("extra");
        habitList.add(extraHabit);
        RepetitionList repetitionList = extraHabit.getRepetitions();
        Timestamp ts = new Timestamp(unixTimeExtra);
        Repetition rep = new Repetition(ts, 1000);
        repetitionList.add(rep);
        repetitionList.toggle(ts);

        command = new ResetHabitsCommand(habitList, selected);
    }



    @Test
    public void testExecute()
    {
        for (Habit h : habitList) {
            RepetitionList repetitionList = h.getRepetitions();
            assertThat(repetitionList.getTotalCount(), equalTo(6L));
        }

        command.execute();

        for (Habit h : habitList) {
            RepetitionList repetitionList = h.getRepetitions();

            if (h.getName() != "extra")
                assertThat(repetitionList.getTotalCount(), equalTo(0L));
            else if (h.getName() == "extra")
                assertThat(repetitionList.getTotalCount(), equalTo(6L));
        }
    }

    @Test
    public void testExecuteResetAll()
    {
        Long unixTime1 = 5000000L;

        // Habits that should be reset
        for (int i = 0; i < 5; i++) {
            Habit habit = fixtures.createShortHabit();
            habitList.add(habit);
            selected.add(habit);
            RepetitionList repetitionList = habit.getRepetitions();
            Timestamp ts = new Timestamp(unixTime1);
            unixTime1 += 5000;
            Repetition rep = new Repetition(ts, 1000);
            repetitionList.add(rep);
            repetitionList.toggle(ts);
        }
        command = new ResetHabitsCommand(habitList);

        for (Habit h : habitList) {
            RepetitionList repetitionList = h.getRepetitions();
            assertThat(repetitionList.getTotalCount(), equalTo(6L));
        }

        command.execute();

        for (Habit h : habitList) {
            RepetitionList repetitionList = h.getRepetitions();
            assertThat(repetitionList.getTotalCount(), equalTo(0L));
        }
    }

    @Test
    public void testUndo()
    {
        thrown.expect(UnsupportedOperationException.class);
        command.undo();
    }

    @Test
    public void testRecord()
    {
        ResetHabitsCommand.Record rec = (ResetHabitsCommand.Record) command.toRecord();
        ResetHabitsCommand other = rec.toCommand(habitList);
        assertThat(other.getId(), equalTo(command.getId()));
        assertThat(other.selected, equalTo(command.selected));
    }
}
