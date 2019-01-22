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

package org.isoron.uhabits.core.database;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class Tokenizer {

    private final InputStream mStream;

    private boolean           mIsNext;
    private int               mCurrent;

    public Tokenizer(final InputStream in) {
        this.mStream = in;
    }

    public boolean hasNext() throws IOException {

        if (!this.mIsNext) {
            this.mIsNext = true;
            this.mCurrent = this.mStream.read();
        }
        return this.mCurrent != -1;
    }

    public int next() throws IOException {

        if (!this.mIsNext) {
            this.mCurrent = this.mStream.read();
        }
        this.mIsNext = false;
        return this.mCurrent;
    }

    public boolean skip(final String s) throws IOException {

        if (s == null || s.length() == 0) {
            return false;
        }

        if (s.charAt(0) != this.mCurrent) {
            return false;
        }

        final int len = s.length();
        this.mStream.mark(len - 1);

        for (int n = 1; n < len; n++) {
            final int value = this.mStream.read();

            if (value != s.charAt(n)) {
                this.mStream.reset();
                return false;
            }
        }
        return true;
    }
}


public class SQLParser {

    public final static int STATE_NONE          = 0;
    public final static int STATE_STRING        = 1;
    public final static int STATE_COMMENT       = 2;
    public final static int STATE_COMMENT_BLOCK = 3;

    public static List<String> parse(final InputStream stream) throws IOException {

        final BufferedInputStream buffer = new BufferedInputStream(stream);
        final List<String> commands = new ArrayList<String>();
        final StringBuffer sb = new StringBuffer();

        try {
            final Tokenizer tokenizer = new Tokenizer(buffer);
            int state = STATE_NONE;

            while (tokenizer.hasNext()) {
                final char c = (char) tokenizer.next();

                if (state == STATE_COMMENT_BLOCK) {
                    if (tokenizer.skip("*/")) {
                        state = STATE_NONE;
                    }
                    continue;

                } else if (state == STATE_COMMENT) {
                    if (isNewLine(c)) {
                        state = STATE_NONE;
                    }
                    continue;

                } else if (state == STATE_NONE && tokenizer.skip("/*")) {
                    state = STATE_COMMENT_BLOCK;
                    continue;

                } else if (state == STATE_NONE && tokenizer.skip("--")) {
                    state = STATE_COMMENT;
                    continue;

                } else if (state == STATE_NONE && c == ';') {
                    final String command = sb.toString().trim();
                    commands.add(command);
                    sb.setLength(0);
                    continue;

                } else if (state == STATE_NONE && c == '\'') {
                    state = STATE_STRING;

                } else if (state == STATE_STRING && c == '\'') {
                    state = STATE_NONE;

                }

                if (state == STATE_NONE || state == STATE_STRING) {
                    if (state == STATE_NONE && isWhitespace(c)) {
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                            sb.append(' ');
                        }
                    } else {
                        sb.append(c);
                    }
                }
            }

        } finally {
            buffer.close();
        }

        if (sb.length() > 0) {
            commands.add(sb.toString().trim());
        }

        return commands;
    }

    private static boolean isNewLine(final char c) {
        return c == '\r' || c == '\n';
    }

    private static boolean isWhitespace(final char c) {
        return c == '\r' || c == '\n' || c == '\t' || c == ' ';
    }
}