/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.utils

import android.util.*
import java.io.*
import java.nio.*
import java.nio.charset.StandardCharsets.*
import javax.crypto.*
import javax.crypto.spec.*

fun ByteArray.encrypt(key: String): ByteArray {
    val keySpec = SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec)
    val encrypted = cipher.doFinal(this)
    return ByteBuffer
            .allocate(16 + encrypted.size)
            .put(cipher.iv)
            .put(encrypted)
            .array()
}

fun ByteArray.decrypt(key: String): ByteArray {
    val buffer = ByteBuffer.wrap(this)
    val iv = ByteArray(16)
    buffer.get(iv)
    val encrypted = ByteArray(buffer.remaining())
    buffer.get(encrypted)
    val keySpec = SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
    return cipher.doFinal(encrypted)
}

fun String.encrypt(key: String): String {
    return Base64.encodeToString(this.toByteArray().encrypt(key), Base64.DEFAULT)
}

fun String.decrypt(key: String): String {
    return String(Base64.decode(this, Base64.DEFAULT).decrypt(key), UTF_8)
}

fun String.decryptToFile(key: String, output: File)
{
    val outputStream = FileOutputStream(output)
    output.writeBytes(Base64.decode(this, Base64.DEFAULT).decrypt(key))
    outputStream.close()
}

fun File.encryptToString(key: String): String {
    val bytes = ByteArray(this.length().toInt())
    val inputStream = FileInputStream(this)
    inputStream.read(bytes)
    inputStream.close()
    return Base64.encodeToString(bytes.encrypt(key), Base64.DEFAULT)
}

fun generateEncryptionKey(): String {
    return try {
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key = keygen.generateKey()
        Base64.encodeToString(key.encoded, Base64.DEFAULT).trim()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}