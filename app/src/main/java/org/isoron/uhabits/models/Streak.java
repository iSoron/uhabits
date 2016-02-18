package org.isoron.uhabits.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

public class Streak extends Model
{
    @Column(name = "habit")
    public Habit habit;

    @Column(name = "start")
    public Long start;

    @Column(name = "end")
    public Long end;

    @Column(name = "length")
    public Long length;
}
