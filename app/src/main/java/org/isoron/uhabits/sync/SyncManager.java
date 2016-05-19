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

package org.isoron.uhabits.sync;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.isoron.uhabits.BaseActivity;
import org.isoron.uhabits.BuildConfig;
import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.commands.CommandParser;
import org.isoron.uhabits.helpers.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SyncManager
{
    public static final String EVENT_AUTH = "auth";
    public static final String EVENT_AUTH_OK = "authOK";
    public static final String EVENT_EXECUTE_COMMAND = "execute";
    public static final String EVENT_POST_COMMAND = "post";
    public static final String EVENT_FETCH = "fetch";
    public static final String EVENT_FETCH_OK = "fetchOK";

    public static final String SYNC_SERVER_URL = "https://sync.loophabits.org:4000";

    private static String GROUP_KEY;
    private static String CLIENT_ID;
    private final SharedPreferences prefs;

    @NonNull
    private Socket socket;
    private BaseActivity activity;
    private LinkedList<Event> pendingConfirmation;
    private List<Event> pendingEmit;
    private boolean readyToEmit = false;

    public SyncManager(final BaseActivity activity)
    {
        this.activity = activity;
        pendingConfirmation = new LinkedList<>();
        pendingEmit = Event.getAll();

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        GROUP_KEY = prefs.getString("syncKey", DatabaseHelper.getRandomId());
        CLIENT_ID = DatabaseHelper.getRandomId();

        try
        {
            IO.setDefaultSSLContext(getCACertSSLContext());

            socket = IO.socket(SYNC_SERVER_URL);

            logSocketEvent(socket, Socket.EVENT_CONNECT, "Connected");
            logSocketEvent(socket, Socket.EVENT_CONNECT_TIMEOUT, "Connect timeout");
            logSocketEvent(socket, Socket.EVENT_CONNECTING, "Connecting...");
            logSocketEvent(socket, Socket.EVENT_CONNECT_ERROR, "Connect error");
            logSocketEvent(socket, Socket.EVENT_DISCONNECT, "Disconnected");
            logSocketEvent(socket, Socket.EVENT_RECONNECT, "Reconnected");
            logSocketEvent(socket, Socket.EVENT_RECONNECT_ATTEMPT, "Reconnecting...");
            logSocketEvent(socket, Socket.EVENT_RECONNECT_ERROR, "Reconnect error");
            logSocketEvent(socket, Socket.EVENT_RECONNECT_FAILED, "Reconnect failed");
            logSocketEvent(socket, Socket.EVENT_DISCONNECT, "Disconnected");
            logSocketEvent(socket, Socket.EVENT_PING, "Ping");
            logSocketEvent(socket, Socket.EVENT_PONG, "Pong");

            socket.on(Socket.EVENT_CONNECT, new OnConnectListener());
            socket.on(Socket.EVENT_DISCONNECT, new OnDisconnectListener());
            socket.on(EVENT_EXECUTE_COMMAND, new OnExecuteCommandListener());
            socket.on(EVENT_AUTH_OK, new OnAuthOKListener());
            socket.on(EVENT_FETCH_OK, new OnFetchOKListener());

            socket.connect();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private SSLContext getCACertSSLContext()
    {
        try
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = activity.getAssets().open("cacert.pem");
            Certificate ca = cf.generateCertificate(caInput);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("ca", ca);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);

            return ctx;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void logSocketEvent(Socket socket, String event, final String msg)
    {
        socket.on(event, new Emitter.Listener()
        {
            @Override
            public void call(Object... args)
            {
                Log.i("SyncManager", msg);
            }
        });
    }

    public void postCommand(Command command)
    {
        JSONObject msg = command.toJSON();
        if(msg == null) return;

        Long now = new Date().getTime();
        Event e = new Event(command.getId(), now, msg.toString());
        e.save();

        Log.i("SyncManager", "Adding to outbox: " + msg.toString());

        pendingEmit.add(e);
        if(readyToEmit) emitPending();
    }

    private void emitPending()
    {
        try
        {
            for (Event e : pendingEmit)
            {
                Log.i("SyncManager", "Emitting: " + e.message);
                socket.emit(EVENT_POST_COMMAND, new JSONObject(e.message));
                pendingConfirmation.add(e);
            }

            pendingEmit.clear();
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void close()
    {
        socket.off();
        socket.close();
    }

    private class OnConnectListener implements Emitter.Listener
    {
        @Override
        public void call(Object... args)
        {
            Log.i("SyncManager", "Sending auth message");
            socket.emit(EVENT_AUTH, buildAuthMessage());
        }

        private JSONObject buildAuthMessage()
        {
            try
            {
                JSONObject json = new JSONObject();
                json.put("groupKey", GROUP_KEY);
                json.put("clientId", CLIENT_ID);
                json.put("version", BuildConfig.VERSION_NAME);
                return json;
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private class OnExecuteCommandListener implements Emitter.Listener
    {
        @Override
        public void call(Object... args)
        {
            try
            {
                Log.d("SyncManager", String.format("Received command: %s", args[0].toString()));
                JSONObject root = new JSONObject(args[0].toString());
                updateLastSync(root.getLong("timestamp"));
                executeCommand(root);
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }

        private void executeCommand(JSONObject root) throws JSONException
        {
            Command received = CommandParser.fromJSON(root);
            if(received == null) throw new RuntimeException("received is null");

            for(Event e : pendingConfirmation)
            {
                if(e.serverId.equals(received.getId()))
                {
                    Log.i("SyncManager", "Pending command confirmed");
                    pendingConfirmation.remove(e);
                    e.delete();
                    return;
                }
            }

            Log.d("SyncManager", "Executing received command");
            activity.executeCommand(received, null, false);
        }
    }

    private class OnAuthOKListener implements Emitter.Listener
    {
        @Override
        public void call(Object... args)
        {
            Log.i("SyncManager", "Auth OK");
            Log.i("SyncManager", "Requesting commands since last sync");

            Long lastSync = prefs.getLong("lastSync", 0);
            socket.emit(EVENT_FETCH, buildFetchMessage(lastSync));
        }

        private JSONObject buildFetchMessage(Long lastSync)
        {
            try
            {
                JSONObject json = new JSONObject();
                json.put("since", lastSync);
                return json;
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private class OnFetchOKListener implements Emitter.Listener
    {
        @Override
        public void call(Object... args)
        {
            try
            {
                Log.i("SyncManager", "Fetch OK");

                JSONObject json = new JSONObject((String) args[0]);
                updateLastSync(json.getLong("timestamp"));

                emitPending();
                readyToEmit = true;
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private class OnDisconnectListener implements Emitter.Listener
    {
        @Override
        public void call(Object... args)
        {
            readyToEmit = false;
        }
    }

    private void updateLastSync(Long timestamp)
    {
        prefs.edit().putLong("lastSync", timestamp).apply();
    }
}
