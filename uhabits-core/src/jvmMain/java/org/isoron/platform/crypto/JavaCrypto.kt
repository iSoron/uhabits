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

package org.isoron.platform.crypto

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class JavaCrypto : Crypto {
    override fun sha256() = JavaSha256()
    override fun hmacSha256() = JavaHmacSha256()
    override fun aesGcm(key: Key) = JavaAesGcm(key)

    override fun secureRandomBytes(numBytes: Int): ByteArray {
        val sr = SecureRandom()
        val bytes = ByteArray(numBytes)
        sr.nextBytes(bytes)
        return bytes
    }
}

class JavaSha256 : Sha256 {
    private val md = MessageDigest.getInstance("SHA-256")
    override fun update(byte: Byte) = md.update(byte)
    override fun finalize(): ByteArray = md.digest()
}

class JavaHmacSha256 : HmacSha256 {
    private val mac = Mac.getInstance("HmacSHA256")
    override fun init(key: ByteArray) = mac.init(SecretKeySpec(key, "HmacSHA256"))
    override fun update(byte: Byte) = mac.update(byte)
    override fun finalize(): ByteArray = mac.doFinal()
}

class JavaAesGcm(val key: Key) : AesGcm {
    override fun encrypt(msg: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.bytes, "AES"), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(msg)
        return ByteBuffer
            .allocate(iv.size + encrypted.size)
            .put(iv)
            .put(encrypted)
            .array()
    }
    override fun decrypt(cipherText: ByteArray): ByteArray {
        val buffer = ByteBuffer.wrap(cipherText)
        val iv = ByteArray(12)
        buffer.get(iv)
        val encrypted = ByteArray(buffer.remaining())
        buffer.get(encrypted)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.bytes, "AES"), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }
}

fun ByteArray.toHexString(): String {
    val sb = StringBuilder()
    for (b in this) sb.append(String.format("%02x", b))
    return sb.toString()
}
