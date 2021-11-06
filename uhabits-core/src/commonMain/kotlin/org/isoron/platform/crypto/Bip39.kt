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

class Bip39(private val wordlist: List<String>, private val crypto: Crypto) {
    private fun computeChecksum(entropy: List<Boolean>): List<Boolean> {
        val sha256 = crypto.sha256()
        var byte = 0
        entropy.forEachIndexed { i, bit ->
            byte = byte shl 1
            if (bit) byte += 1
            if (i.rem(8) == 7) {
                sha256.update(byte.toByte())
                byte = 0
            }
        }
        return sha256.finalize().toBits().subList(0, entropy.size / 32)
    }

    fun encode(entropy: ByteArray): List<String> {
        val entropyBits = entropy.toBits()
        val msg = entropyBits + computeChecksum(entropyBits)
        var wordIndex = 0
        val mnemonic = mutableListOf<String>()
        msg.forEachIndexed { i, bit ->
            wordIndex = wordIndex shl 1
            if (bit) wordIndex += 1
            if (i.rem(11) == 10) {
                mnemonic.add(wordlist[wordIndex])
                wordIndex = 0
            }
        }
        return mnemonic
    }

    fun decode(mnemonic: List<String>): ByteArray {
        val bits = mutableListOf<Boolean>()
        mnemonic.forEach { word ->
            val wordBits = mutableListOf<Boolean>()
            var wordIndex = wordlist.binarySearch(word)
            if (wordIndex < 0) throw InvalidWordException(word)
            for (it in 0..10) {
                wordBits.add(wordIndex.rem(2) == 1)
                wordIndex = wordIndex shr 1
            }
            bits.addAll(wordBits.reversed())
        }
        if (bits.size.rem(33) != 0) throw InvalidMnemonicLength()
        val checksumSize = bits.size / 33
        val checksum = bits.subList(bits.size - checksumSize, bits.size)
        val entropy = bits.subList(0, bits.size - checksumSize)
        if (computeChecksum(entropy) != checksum) throw InvalidChecksumException()
        return byteArray(entropy)
    }
}

class InvalidChecksumException : Exception()
class InvalidWordException(word: String) : Exception(word)
class InvalidMnemonicLength : Exception()
