#!/bin/bash
input=$1
locale_name=$2

cat <<END
// --------------------------------------------------------------------------
//                   THIS FILE WAS AUTOMATICALLY GENERATED
// 
// Please do not submit pull request to modify it. Corrections to translations
// may be submitted at https://translate.loophabits.org/
// --------------------------------------------------------------------------

package org.isoron.uhabits.i18n

END

prefix="override "
if [ "$locale_name" == "" ]; then
        prefix="open "
        echo "open class Strings() {"
else
        echo "class Strings$locale_name : Strings() {"
fi

grep "<string name" "$1" | \
        grep -v translatable | \
        sed 's/&amp;/\&/g' | \
        sed 's/^.*name="\([^"]*\)">\([^<]*\)<.*/    '"$prefix"'val \1 = "\2"/'

echo "}"
