package org.isoron.uhabits.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Checkmarks")
public class Checkmark extends Model
{

    public static final int UNCHECKED = 0;
    public static final int CHECKED_IMPLICITLY = 1;
    public static final int CHECKED_EXPLICITLY = 2;

    @Column(name = "habit")
    public Habit habit;

    @Column(name = "timestamp")
    public Long timestamp;

    /**
     * Indicates whether there is a checkmark at the given timestamp or not, and whether the
     * checkmark is explicit or implicit. An explicit checkmark indicates that there is a
     * repetition at that day. An implicit checkmark indicates that there is no repetition at that
     * day, but a repetition was not needed, due to the frequency of the habit.
     */
    @Column(name = "value")
    public Integer value;
}
