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
package org.isoron.uhabits.core.sync

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.io.PrintWriter
import java.util.Random

class EncryptionExtTest {

    @Test
    fun test_encode_decode() {
        val original = ByteArray(5000)
        Random().nextBytes(original)
        val encoded = original.encodeBase64()
        val decoded = encoded.decodeBase64()
        assertThat(decoded, equalTo(original))
    }

    @Test
    fun test_encrypt_decrypt_bytes() = runBlocking {
        val original = ByteArray(5000)
        Random().nextBytes(original)
        val key = EncryptionKey.generate()
        val encrypted = original.encrypt(key)
        val decrypted = encrypted.decrypt(key)
        assertThat(decrypted, equalTo(original))
    }

    @Test
    fun test_encrypt_decrypt_file() = runBlocking {
        val original = File.createTempFile("file", ".txt")
        val writer = PrintWriter(original.outputStream())
        writer.println("hello world")
        writer.println("encryption test")
        writer.close()
        assertThat(original.length(), equalTo(28L))

        val key = EncryptionKey.generate()
        val encrypted = original.encryptToString(key)
        assertThat(encrypted.length, greaterThan(10))

        val decrypted = File.createTempFile("file", ".txt")
        encrypted.decryptToFile(key, decrypted)
        assertThat(decrypted.length(), equalTo(28L))
        assertEquals("hello world\nencryption test\n", decrypted.readText())
    }
}
