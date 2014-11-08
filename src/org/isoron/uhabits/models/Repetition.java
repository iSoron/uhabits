package org.isoron.uhabits.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Repetitions")
public class Repetition extends Model {

	@Column(name = "habit")
	public Habit habit;
	
	@Column(name = "timestamp")
	public Long timestamp;
}
