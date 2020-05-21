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
package org.isoron.androidbase

import android.content.Context
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class SSLContextProvider @Inject constructor(@param:AppContext private val context: Context) {
    fun getCACertSSLContext(): SSLContext =
            try {
                val ca = CertificateFactory.getInstance("X.509")
                        .let { cf ->
                            context.assets.open("cacert.pem")
                                    .use { caInput ->
                                        cf.generateCertificate(caInput)
                                    }
                        }
                val ks = KeyStore.getInstance(KeyStore.getDefaultType())
                        .apply {
                            load(null, null)
                            setCertificateEntry("ca", ca)
                        }
                val tmf =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                                .apply { init(ks) }
                SSLContext.getInstance("TLS")
                        .apply { init(null, tmf.trustManagers, null) }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
}