package org.isoron.uhabits.models;

import java.util.Date;
import java.util.List;

import org.isoron.helpers.Command;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.R;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.SQLiteUtils;

@Table(name = "Habits")
public class Habit extends Model
{

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                           Fields                                          *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public static final int colors[] = { Color.parseColor("#900000"),
			Color.parseColor("#c54100"), Color.parseColor("#c0ab00"),
			Color.parseColor("#8db600"), Color.parseColor("#117209"),
			Color.parseColor("#06965b"), Color.parseColor("#069a95"),
			Color.parseColor("#114896"), Color.parseColor("#501394"),
			Color.parseColor("#872086"), Color.parseColor("#c31764"),
			Color.parseColor("#000000"), Color.parseColor("#aaaaaa") };

	@Column(name = "name")
	public String name;

	@Column(name = "description")
	public String description;

	@Column(name = "freq_num")
	public Integer freq_num;

	@Column(name = "freq_den")
	public Integer freq_den;

	@Column(name = "color")
	public Integer color;

	@Column(name = "position")
	public Integer position;

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                          Commands                                         *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public static class CreateCommand extends Command
	{
		private Habit model;
		private Long savedId;

		public CreateCommand(Habit model)
		{
			this.model = model;
		}

		@Override
		public void execute()
		{
			Habit savedHabit = new Habit(model);
			if(savedId == null)
			{
				savedHabit.save();
				savedId = savedHabit.getId();
			}
			else
			{
				savedHabit.save(savedId);
			}
		}

		@Override
		public void undo()
		{
			Habit.get(savedId).delete();
		}

		@Override
		public Integer getExecuteStringId()
		{
			return R.string.toast_habit_created;
		}

		@Override
		public Integer getUndoStringId()
		{
			return R.string.toast_habit_deleted;
		}

	}

	public class EditCommand extends Command
	{
		private Habit original;
		private Habit modified;
		private long savedId;
		private boolean hasIntervalChanged;

		public EditCommand(Habit modified)
		{
			this.savedId = getId();
			this.modified = new Habit(modified);
			this.original = new Habit(Habit.this);

			hasIntervalChanged = (this.original.freq_den != this.modified.freq_den
					|| this.original.freq_num != this.modified.freq_num);
		}

		public void execute()
		{
			Habit habit = Habit.get(savedId);
			habit.copyAttributes(modified);
			habit.save();
			if(hasIntervalChanged)
				habit.deleteScoresNewerThan(0);
		}

		public void undo()
		{
			Habit habit = Habit.get(savedId);
			habit.copyAttributes(original);
			habit.save();
			if(hasIntervalChanged)
				habit.deleteScoresNewerThan(0);
		}

		public Integer getExecuteStringId()
		{
			return R.string.toast_habit_changed;
		}

		public Integer getUndoStringId()
		{
			return R.string.toast_habit_changed_back;
		}
	}

	public class ToggleRepetitionCommand extends Command
	{
		private Long offset;

		public ToggleRepetitionCommand(long offset)
		{
			this.offset = offset;
		}

		@Override
		public void execute()
		{
			toggleRepetition(offset);
		}

		@Override
		public void undo()
		{
			execute();
		}
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                         Accessors                                         *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public Habit(Habit model)
	{
		copyAttributes(model);
	}

	public void copyAttributes(Habit model)
	{
		this.name = model.name;
		this.description = model.description;
		this.freq_num = model.freq_num;
		this.freq_den = model.freq_den;
		this.color = model.color;
		this.position = model.position;
	}

	public Habit()
	{
		this.color = colors[11];
		this.position = Habit.getCount();
	}

	public static Habit get(Long id)
	{
		return Habit.load(Habit.class, id);
	}

	public void save(Long id)
	{
		save();
		Habit.updateId(getId(), id);
	}

	@SuppressLint("DefaultLocale")
	public static void updateId(long oldId, long newId)
	{
		SQLiteUtils.execSql(String.format(
				"update Habits set Id = %d where Id = %d", newId, oldId));
	}

	protected static From select()
	{
		return new Select().from(Habit.class).orderBy("position");
	}

	public static int getCount()
	{
		return select().count();
	}

	public static Habit getByPosition(int position)
	{
		return select().offset(position).executeSingle();
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                        Repetitions                                        *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	protected From selectReps()
	{
		return new Select().from(Repetition.class).where("habit = ?", getId())
				.orderBy("timestamp");
	}

	protected From selectRepsFromTo(long timeFrom, long timeTo)
	{
		return selectReps().and("timestamp >= ?", timeFrom).and(
				"timestamp <= ?", timeTo);
	}

	public boolean hasRep(long timestamp)
	{
		int count = selectReps().where("timestamp = ?", timestamp).count();
		return (count > 0);
	}

	public void deleteReps(long timestamp)
	{
		new Delete().from(Repetition.class).where("habit = ?", getId())
				.and("timestamp = ?", timestamp).execute();
	}

	public int[] getReps(long timeFrom, long timeTo)
	{
		long timeFromExtended = timeFrom - (long)(freq_den) * DateHelper.millisecondsInOneDay;
		List<Repetition> reps = selectRepsFromTo(timeFromExtended, timeTo).execute();

		int nDaysExtended = (int) ((timeTo - timeFromExtended) / DateHelper.millisecondsInOneDay);
		int checkExtended[] = new int[nDaysExtended + 1];
		
		int nDays = (int) ((timeTo - timeFrom) / DateHelper.millisecondsInOneDay);

		// mark explicit checks
		for (Repetition rep : reps)
		{
			int offset = (int) ((rep.timestamp - timeFrom) / DateHelper.millisecondsInOneDay);
			checkExtended[nDays - offset] = 2;
		}
		
		// marks implicit checks
		for(int i=0; i<nDays; i++)
		{
			int counter = 0;
			
			for(int j=0; j<freq_den; j++)
				if(checkExtended[i+j] == 2) counter++;
			
			if(counter >= freq_num)
				checkExtended[i] = Math.max(checkExtended[i], 1);
		}

		int check[] = new int[nDays + 1];
		for(int i=0; i<nDays+1; i++)
			check[i] = checkExtended[i];
		
		return check;
	}

	public Repetition getOldestRep()
	{
		return (Repetition) selectReps().limit(1).executeSingle();
	}

	public void toggleRepetition(long timestamp)
	{
		if(hasRep(timestamp))
		{
			deleteReps(timestamp);
		}
		else
		{
			Repetition rep = new Repetition();
			rep.habit = this;
			rep.timestamp = timestamp;
			rep.save();
		}

		deleteScoresNewerThan(timestamp);
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                        Scoring                                            *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public Score getNewestScore()
	{
		return new Select().from(Score.class).where("habit = ?", getId())
				.orderBy("timestamp desc").limit(1).executeSingle();
	}

	public void deleteScoresNewerThan(long timestamp)
	{
		new Delete().from(Score.class).where("habit = ?", getId())
				.and("timestamp >= ?", timestamp).execute();
	}

	public Integer getScore()
	{
		int beginningScore;
		long beginningTime;

		long today = DateHelper.getStartOfDay(DateHelper.getLocalTime());
		long day = DateHelper.millisecondsInOneDay;

		double freq = ((double) freq_num) / freq_den;
		double multiplier = Math.pow(0.5, 1.0 / (14.0 / freq - 1));

		Score newestScore = getNewestScore();
		if(newestScore == null)
		{
			Repetition oldestRep = getOldestRep();
			if(oldestRep == null)
				return 0;
			beginningTime = oldestRep.timestamp;
			beginningScore = 0;
		}
		else
		{
			beginningTime = newestScore.timestamp + day;
			beginningScore = newestScore.score;
		}

		long nDays = (today - beginningTime) / day;
		if(nDays < 0)
			return newestScore.score;

		int reps[] = getReps(beginningTime, today);

		int lastScore = beginningScore;
		for (int i = 0; i < reps.length; i++)
		{
			Score s = new Score();
			s.habit = this;
			s.timestamp = beginningTime + day * i;
			s.score = (int) (lastScore * multiplier);
			if(reps[reps.length-i-1] == 2) {
				s.score += 1000000;
				s.score = Math.min(s.score, 19259500);
			}
			s.save();

			lastScore = s.score;
		}

		return lastScore;
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                          Ordering                                         *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public static void reorder(int from, int to)
	{
		if(from == to)
			return;

		Habit h = Habit.getByPosition(from);
		if(to < from)
			new Update(Habit.class).set("position = position + 1")
					.where("position >= ? and position < ?", to, from)
					.execute();
		else
			new Update(Habit.class).set("position = position - 1")
					.where("position > ? and position <= ?", from, to)
					.execute();

		h.position = to;
		h.save();
	}

	public static void rebuildOrder()
	{
		List<Habit> habits = select().execute();
		int i = 0;
		for (Habit h : habits)
		{
			h.position = i++;
			h.save();
		}
	}

	public static void roundTimestamps()
	{
		List<Repetition> reps = new Select().from(Repetition.class).execute();
		for (Repetition r : reps)
		{
			r.timestamp = DateHelper.getStartOfDay(r.timestamp);
			r.save();
		}
	}
}
