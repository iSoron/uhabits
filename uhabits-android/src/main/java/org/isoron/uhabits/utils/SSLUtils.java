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

package org.isoron.uhabits.utils;

import android.content.*;
import android.support.annotation.*;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;

import javax.net.ssl.*;

public abstract class SSLUtils
{
    public static SSLContext getCACertSSLContext(@NonNull Context context)
    {
        try
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getAssets().open("cacert.pem");
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

}
