#!/bin/bash
find app/build/outputs/failed/test-screenshots -name '*.expected*' -delete
rsync -av app/build/outputs/failed/test-screenshots/ app/src/androidTest/assets/
