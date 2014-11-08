package org.isoron.uhabits.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Score")
public class Score extends Model
{
	@Column(name = "habit")
	public Habit habit;
	
	@Column(name = "timestamp")
	public Long timestamp;
	
	@Column(name = "score")
	public Integer score;
}
