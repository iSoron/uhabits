package org.isoron.helpers;


public abstract class Command
{
    public abstract void execute();

    public abstract void undo();

    public Integer getExecuteStringId()
    {
        return null;
    }

    public Integer getUndoStringId()
    {
        return null;
    }
}
