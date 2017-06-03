#!/bin/bash
find uhabits-android/build/outputs/failed/test-screenshots -name '*.expected*' -delete
rsync -av uhabits-android/build/outputs/failed/test-screenshots/ uhabits-android/src/androidTest/assets/
