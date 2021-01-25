/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.models

import java.util.LinkedList
import javax.annotation.concurrent.ThreadSafe

/**
 * A ModelObservable allows objects to subscribe themselves to it and receive
 * notifications whenever the model is changed.
 */
@ThreadSafe
class ModelObservable {
    private val listeners: MutableList<Listener>

    /**
     * Adds the given listener to the observable.
     *
     * @param l the listener to be added.
     */
    @Synchronized
    fun addListener(l: Listener) {
        listeners.add(l)
    }

    /**
     * Notifies every listener that the model has changed.
     *
     *
     * Only models should call this method.
     */
    @Synchronized
    fun notifyListeners() {
        for (l in listeners) l.onModelChange()
    }

    /**
     * Removes the given listener.
     *
     *
     * The listener will no longer be notified when the model changes. If the
     * given listener is not subscribed to this observable, does nothing.
     *
     * @param l the listener to be removed
     */
    @Synchronized
    fun removeListener(l: Listener) {
        listeners.remove(l)
    }

    /**
     * Interface implemented by objects that want to be notified when the model
     * changes.
     */
    fun interface Listener {
        /**
         * Called whenever the model associated to this observable has been
         * modified.
         */
        fun onModelChange()
    }

    /**
     * Creates a new ModelObservable with no listeners.
     */
    init {
        listeners = LinkedList()
    }
}
