
package com.activeandroid.util;

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

import java.io.IOException;
import java.io.InputStream;


public class Tokenizer {

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
