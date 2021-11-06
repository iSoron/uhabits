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

import kotlinx.coroutines.runBlocking
import org.isoron.platform.io.JavaFileOpener
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class Bip39Test {

    private lateinit var bip39: Bip39

    private val phrases = listOf(
        listOf(
            "gather",
            "capable",
            "since",
        ),
        listOf(
            "exit",
            "churn",
            "hazard",
            "garage",
            "hint",
            "great",
        ),
        listOf(
            "exile",
            "blouse",
            "athlete",
            "dinner",
            "chef",
            "home",
            "destroy",
            "disagree",
            "select",
            "eight",
            "slim",
            "talent",
        ),
    )

    private val entropies = listOf(
        byteArrayOfInts(0x60, 0x64, 0x3f, 0x24),
        byteArrayOfInts(0x4f, 0xe5, 0x19, 0xa7, 0xaf, 0xb6, 0xbc, 0xcc),
        byteArrayOfInts(
            0x4f,
            0xa3,
            0x04,
            0x38,
            0x9f,
            0x22,
            0x74,
            0xda,
            0x0f,
            0x09,
            0xf6,
            0xc3,
            0x48,
            0xdf,
            0x2f,
            0x6e,
        )
    )

    @Before
    fun setUp() = runBlocking {
        bip39 = Bip39(JavaFileOpener().openResourceFile("bip39/en_US.txt").lines(), JavaCrypto())
    }

    @Test
    fun test_encode_decode() {
        phrases.zip(entropies).forEach { (phrase, entropy) ->
            assertEquals(phrase, bip39.encode(entropy))
            assertEquals(entropy.toHexString(), bip39.decode(phrase).toHexString())
        }
    }

    @Test
    fun test_decode_invalid_checksum() {
        assertFailsWith<InvalidChecksumException> {
            bip39.decode(
                listOf(
                    "lawn",
                    "dirt",
                    "work",
                    "mountain",
                    "depth",
                    "loyal",
                    "citizen",
                    "theory",
                    "cram",
                    "trip",
                    "boil",
                    "about",
                )
            )
        }
    }

    @Test
    fun test_decode_invalid_word() {
        assertFailsWith<InvalidWordException> {
            bip39.decode(listOf("dirt", "bee", "work"))
        }
    }
}
