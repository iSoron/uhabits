/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.colorpicker;

import java.util.Comparator;

import android.graphics.Color;

/**
 * A color comparator which compares based on hue, saturation, and value.
 */
public class HsvColorComparator implements Comparator<Integer> {

    @Override
    public int compare(Integer lhs, Integer rhs) {
        float[] hsv = new float[3];
        Color.colorToHSV(lhs, hsv);
        float hue1 = hsv[0];
        float sat1 = hsv[1];
        float val1 = hsv[2];

        float[] hsv2 = new float[3];
        Color.colorToHSV(rhs, hsv2);
        float hue2 = hsv2[0];
        float sat2 = hsv2[1];
        float val2 = hsv2[2];

        if (hue1 < hue2) {
            return 1;
        } else if (hue1 > hue2) {
            return -1;
        } else {
            if (sat1 < sat2) {
                return 1;
            } else if (sat1 > sat2) {
                return -1;
            } else {
                if (val1 < val2) {
                    return 1;
                } else if (val1 > val2) {
                    return -1;
                }
            }
        }
        return 0;
    }
}
