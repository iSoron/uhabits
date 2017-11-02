/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.reminders;

import android.content.*;
import android.support.annotation.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.reminders.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xmlpull.v1.*;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

public class CustomRemindersSaverXml implements CustomReminders.Saver
{
    private static final String FILENAME_NORMAL = "custom_reminders.xml";
    private static final String FILENAME_DEBUG = "custom_reminders_debug.xml";
    private static final String TAG = "CustomRemindersSaverXml";

    private Context context;

    private String filename()
    {
        return HabitsApplication.Companion.isTestMode() ? FILENAME_DEBUG : FILENAME_NORMAL;
    }

    public CustomRemindersSaverXml(@NonNull Context context)
    {
        this.context = context;
    }

    @Override
    public void save(Map<Long, Long> map)
    {
        StringWriter writer = new StringWriter();
        try
        {
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput( writer );
            serializer.startDocument("UTF-8", true);
            serializer.startTag( "","reminders" );
            for( Map.Entry< Long, Long > entry : map.entrySet())
            {
                serializer.startTag( "", "reminder" );
                serializer.attribute( "", "habitId", entry.getKey().toString());
                serializer.attribute( "", "time", entry.getValue().toString());
                serializer.endTag( "", "reminder" );
            }
            serializer.endTag( "", "reminders" );
            serializer.endDocument();
        } catch (IOException e)
        {
            Log.e( TAG, "Error writing XML for custom reminders" );
            context.getFileStreamPath( filename()).delete();
            return;
        }
        try
        {
            FileOutputStream out = context.openFileOutput( filename(), Context.MODE_PRIVATE);
            out.write( writer.toString().getBytes());
            out.close();
        }
        catch (IOException e)
        {
            Log.e( TAG, "Error creating XML file for custom reminders" );
            context.getFileStreamPath( filename()).delete();
        }
    }

    @Override
    public Map<Long, Long> load()
    {
        Map< Long, Long > map = new HashMap< Long, Long >();
        try
        {
            InputStream in = context.openFileInput( filename());
            if( in != null )
            {
                try
                {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.parse( in );
                    NodeList nodes = doc.getElementsByTagName( "reminder" );
                    for( int i = 0; i < nodes.getLength(); ++i )
                    {
                        Element element = (Element) nodes.item(i);
                        Long habitId = Long.parseLong(element.getAttribute("habitId"));
                        Long time = Long.parseLong(element.getAttribute("time"));
                        map.put(habitId, time);
                    }
                }
                catch (SAXException | ParserConfigurationException e)
                {
                    Log.e( TAG, "Error reading custom reminders XML");
                }
                in.close();
            }
        }
        catch (IOException e)
        {
            Log.d( TAG, "Custom reminders XML file not found");
        }
        return map;
    }
}
