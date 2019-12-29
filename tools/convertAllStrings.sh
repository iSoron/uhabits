#!/bin/bash

INPUT_DIR=../android/uhabits-android/src/main/res/
OUTPUT_DIR=../core/src/commonMain/kotlin/org/isoron/uhabits/i18n/

convert() {
    ./androidStringsToKt.sh $INPUT_DIR/$1/strings.xml "$2" > $OUTPUT_DIR/Strings$2.kt
}

#convert values ""
convert values-ar Arabic
convert values-bg Bulgarian
convert values-ca Catalan
convert values-cs Czech
convert values-da Danish
convert values-de German
convert values-el Greek
convert values-eo Esperanto
convert values-es Spanish
convert values-eu Basque
convert values-fa Persian
convert values-fi Finnish
convert values-fr French
convert values-hi Hindi
convert values-hr Croatian
convert values-hu Hungarian
convert values-in Indonesian
convert values-it Italian
convert values-iw Hebrew
convert values-ja Japanese
convert values-ko Korean
convert values-nl Dutch
convert values-no-rNO Norwegian
convert values-pl Polish
convert values-pt-rBR PortugueseBR
convert values-pt-rPT PortuguesePT
convert values-ro Romanian
convert values-ru Russian
convert values-sl Slovak
convert values-sr Serbian
convert values-sv Swedish
convert values-tr Turkish
convert values-uk Ukrainian
convert values-vi Vietnamese
convert values-zh-rCN ChineseCN
convert values-zh-rTW ChineseTW
