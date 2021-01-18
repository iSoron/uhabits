/*
 * Copyright (C) 2014 Markus Pfeiffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.isoron.uhabits.core.database

import java.io.BufferedInputStream
import java.io.InputStream
import java.util.ArrayList

internal class Tokenizer(
    private val mStream: InputStream,
) {
    private var mIsNext = false
    private var mCurrent = 0

    operator fun hasNext(): Boolean {
        if (!mIsNext) {
            mIsNext = true
            mCurrent = mStream.read()
        }
        return mCurrent != -1
    }

    operator fun next(): Int {
        if (!mIsNext) {
            mCurrent = mStream.read()
        }
        mIsNext = false
        return mCurrent
    }

    fun skip(s: String?): Boolean {
        if (s == null || s.isEmpty()) {
            return false
        }
        if (s[0].toInt() != mCurrent) {
            return false
        }
        val len = s.length
        mStream.mark(len - 1)
        for (n in 1 until len) {
            val value = mStream.read()
            if (value != s[n].toInt()) {
                mStream.reset()
                return false
            }
        }
        return true
    }
}

object SQLParser {
    private const val STATE_NONE = 0
    private const val STATE_STRING = 1
    private const val STATE_COMMENT = 2
    private const val STATE_COMMENT_BLOCK = 3

    fun parse(stream: InputStream): List<String> {
        val buffer = BufferedInputStream(stream)
        val commands: MutableList<String> = ArrayList()
        val sb = StringBuffer()
        buffer.use { buffer ->
            val tokenizer = Tokenizer(buffer)
            var state = STATE_NONE
            while (tokenizer.hasNext()) {
                val c = tokenizer.next().toChar()
                if (state == STATE_COMMENT_BLOCK) {
                    if (tokenizer.skip("*/")) {
                        state = STATE_NONE
                    }
                    continue
                } else if (state == STATE_COMMENT) {
                    if (isNewLine(c)) {
                        state = STATE_NONE
                    }
                    continue
                } else if (state == STATE_NONE && tokenizer.skip("/*")) {
                    state = STATE_COMMENT_BLOCK
                    continue
                } else if (state == STATE_NONE && tokenizer.skip("--")) {
                    state = STATE_COMMENT
                    continue
                } else if (state == STATE_NONE && c == ';') {
                    val command = sb.toString().trim { it <= ' ' }
                    commands.add(command)
                    sb.setLength(0)
                    continue
                } else if (state == STATE_NONE && c == '\'') {
                    state = STATE_STRING
                } else if (state == STATE_STRING && c == '\'') {
                    state = STATE_NONE
                }
                if (state == STATE_NONE || state == STATE_STRING) {
                    if (state == STATE_NONE && isWhitespace(c)) {
                        if (sb.isNotEmpty() && sb[sb.length - 1] != ' ') {
                            sb.append(' ')
                        }
                    } else {
                        sb.append(c)
                    }
                }
            }
        }
        if (sb.isNotEmpty()) {
            commands.add(sb.toString().trim { it <= ' ' })
        }
        return commands
    }

    private fun isNewLine(c: Char): Boolean {
        return c == '\r' || c == '\n'
    }

    private fun isWhitespace(c: Char): Boolean {
        return c == '\r' || c == '\n' || c == '\t' || c == ' '
    }
}
