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

package org.isoron.uhabits.models;

import java.util.*;

/**
 * A ModelObservable allows objects to subscribe themselves to it and receive
 * notifications whenever the model is changed.
 */
public class ModelObservable
{
    private List<Listener> listeners;

    /**
     * Creates a new ModelObservable with no listeners.
     */
    public ModelObservable()
    {
        super();
        listeners = new LinkedList<>();
    }

    /**
     * Adds the given listener to the observable.
     *
     * @param l the listener to be added.
     */
    public void addListener(Listener l)
    {
        listeners.add(l);
    }

    /**
     * Notifies every listener that the model has changed.
     * <p>
     * Only models should call this method.
     */
    public void notifyListeners()
    {
        for (Listener l : listeners) l.onModelChange();
    }

    /**
     * Removes the given listener.
     * <p>
     * The listener will no longer be notified when the model changes. If the
     * given listener is not subscribed to this observable, does nothing.
     *
     * @param l the listener to be removed
     */
    public void removeListener(Listener l)
    {
        listeners.remove(l);
    }

    /**
     * Interface implemented by objects that want to be notified when the model
     * changes.
     */
    public interface Listener
    {
        /**
         * Called whenever the model associated to this observable has been
         * modified.
         */
        void onModelChange();
    }
}
