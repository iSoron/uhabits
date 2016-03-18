package org.isoron.helpers;

import com.activeandroid.ActiveAndroid;

public class ActiveAndroidHelper
{
    public interface Command
    {
        void execute();
    }

    public static void executeAsTransaction(Command command)
    {
        ActiveAndroid.beginTransaction();
        try
        {
            command.execute();
            ActiveAndroid.setTransactionSuccessful();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }
}
