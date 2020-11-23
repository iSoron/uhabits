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

import org.isoron.uhabits.*
import org.junit.*
import java.io.*

class EncryptionExtTest : BaseAndroidTest() {
    @Test
    fun test_encrypt_decrypt_string() {
        val original = "Hello world!"
        val key = generateEncryptionKey()
        val encrypted = original.encrypt(key)
        val decrypted = encrypted.decrypt(key)
        assertEquals("Hello world!", decrypted)
    }

    @Test
    fun test_encrypt_decrypt_file() {
        val original = File.createTempFile("file", ".txt")
        val writer = PrintWriter(original.outputStream())
        writer.println("hello world")
        writer.println("encryption test")
        writer.close()

        val key = generateEncryptionKey()
        val encrypted = original.encryptToString(key)

        val decrypted = File.createTempFile("file", ".txt")
        encrypted.decryptToFile(key, decrypted)
        assertEquals("hello world\nencryption test\n", decrypted.readText())
    }
}
