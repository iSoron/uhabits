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

class Key(val bytes: ByteArray)

interface Crypto {
    fun sha256(): Sha256
    fun hmacSha256(): HmacSha256
    fun aesGcm(key: Key): AesGcm
    fun secureRandomBytes(numBytes: Int): ByteArray

    fun generateKey(): Key {
        return Key(secureRandomBytes(32))
    }

    fun deriveKey(master: Key, subkeyName: String): Key {
        val mac = hmacSha256()
        mac.init(master.bytes)
        mac.update(subkeyName)
        return Key(mac.finalize())
    }
}

interface Sha256 {
    fun update(byte: Byte)
    fun finalize(): ByteArray
}

interface HmacSha256 {
    fun init(key: ByteArray)
    fun update(byte: Byte)
    fun finalize(): ByteArray

    fun update(msg: String) {
        for (b in msg.encodeToByteArray()) {
            update(b)
        }
    }
}

interface AesGcm {
    fun encrypt(msg: ByteArray, iv: ByteArray): ByteArray
    fun decrypt(cipherText: ByteArray): ByteArray
}

fun Byte.toBits(): List<Boolean> = (7 downTo 0).map { (toInt() and (1 shl it)) != 0 }
fun ByteArray.toBits(): List<Boolean> = flatMap { it.toBits() }
fun byteArrayOfInts(vararg b: Int) = b.map { it.toByte() }.toByteArray()
fun byteArray(bits: List<Boolean>): ByteArray {
    var byte = 0
    val bytes = ByteArray(bits.size / 8)
    bits.forEachIndexed { i, b ->
        byte = byte shl 1
        if (b) byte += 1
        if (i.rem(8) == 7) {
            bytes[i / 8] = byte.toByte()
        }
    }
    return bytes
}
