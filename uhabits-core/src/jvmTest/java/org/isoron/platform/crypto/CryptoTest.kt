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

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CryptoTest {
    private val crypto = JavaCrypto()

    @Test
    fun test_sha256() {
        val sha256 = crypto.sha256()
        sha256.update(0x10.toByte())
        sha256.update(0x20.toByte())
        sha256.update(0x30.toByte())
        val digest = sha256.finalize()
        assertEquals(32, digest.size)
        assertEquals(0x8e.toByte(), digest[0])
        assertEquals(0x13.toByte(), digest[1])
        assertEquals(0x36.toByte(), digest[2])
        assertEquals(0xb9.toByte(), digest[31])
    }

    @Test
    fun test_hmacsha256() {
        val hmac = crypto.hmacSha256()
        hmac.init(byteArrayOfInts(0x01, 0x02, 0x03))
        hmac.update(0x40.toByte())
        hmac.update("AB")
        val checksum = hmac.finalize()
        assertEquals(32, checksum.size)
        assertEquals(0x6d.toByte(), checksum[0])
        assertEquals(0xc9.toByte(), checksum[1])
        assertEquals(0x05.toByte(), checksum[2])
        assertEquals(0xa1.toByte(), checksum[31])
    }

    @Test
    fun test_aes_gcm() {
        val msg = byteArrayOfInts(
            0x2f,
            0xdc,
            0xaa,
            0x41,
            0xfa,
            0xb8,
            0x5e,
            0xe8,
            0xa3,
            0x12,
            0x69,
            0x68,
            0x14,
            0x31,
            0xd8,
            0x59,
            0x74,
            0x29,
            0x2e,
            0xae,
            0xed,
            0x76,
            0x0a,
            0x56,
            0x46,
            0x90,
            0xb6,
            0xcb,
            0x9f,
            0x37,
            0xbe,
            0xae,
        )

        val key = Key(
            byteArrayOfInts(
                0xed,
                0xa8,
                0xc3,
                0xc6,
                0x44,
                0x1e,
                0xa1,
                0xd5,
                0x71,
                0x8c,
                0x71,
                0x45,
                0xbe,
                0x2d,
                0xf7,
                0xa4,
                0x81,
                0x2e,
                0x0a,
                0x0b,
                0xa8,
                0xe4,
                0x20,
                0x49,
                0x94,
                0x8a,
                0x71,
                0x1a,
                0x15,
                0xf5,
                0x29,
                0x78,
            )
        )

        val iv = byteArrayOfInts(
            0xa7,
            0xef,
            0xe1,
            0xba,
            0xdf,
            0x4f,
            0x85,
            0xca,
            0xc3,
            0x81,
            0xc1,
            0x93,
        )

        val expected = byteArrayOfInts(
            // iv
            0xa7,
            0xef,
            0xe1,
            0xba,
            0xdf,
            0x4f,
            0x85,
            0xca,
            0xc3,
            0x81,
            0xc1,
            0x93,
            // msg
            0x24,
            0xe7,
            0x26,
            0x9b,
            0xb8,
            0x59,
            0xf0,
            0xe0,
            0x4f,
            0xda,
            0xc0,
            0x85,
            0xc6,
            0x23,
            0x21,
            0x61,
            0x80,
            0x59,
            0xd6,
            0x18,
            0xee,
            0xa0,
            0xd8,
            0x00,
            0xe3,
            0xdf,
            0x6e,
            0xcf,
            0x89,
            0x82,
            0xfd,
            0x63,
            // verification tag
            0xe9,
            0xe9,
            0xac,
            0x92,
            0xdc,
            0xb1,
            0x7c,
            0x2d,
            0x9a,
            0x73,
            0xda,
            0x25,
            0x6d,
            0xda,
            0xc0,
            0x83,
        )
        val cipher = crypto.aesGcm(key)
        val actual = cipher.encrypt(msg, iv)
        assertEquals(actual.toHexString(), expected.toHexString())

        val recovered = cipher.decrypt(actual)
        assertEquals(msg.toHexString(), recovered.toHexString())
    }

    @Test
    fun test_rand() {
        val r1 = crypto.secureRandomBytes(8)
        val r2 = crypto.secureRandomBytes(8)
        assertEquals(8, r1.size)
        assertNotEquals(r1.toBits(), r2.toBits())
    }

    @Test
    fun test_derive_key() {
        val k1 = Key(byteArrayOfInts(0x01, 0x02, 0x03))
        val k2 = crypto.deriveKey(k1, "TEST")
        assertEquals(0x44.toByte(), k2.bytes[0])
        assertEquals(0xd3.toByte(), k2.bytes[31])
    }
}
