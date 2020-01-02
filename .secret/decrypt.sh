#!/bin/sh
cd "$(dirname "$0")"
if [ -z "$GPG_PASSWORD" ]; then
	echo Env variable GPG_PASSWORD must be defined
	exit 1
fi
for file in gcp-key.json keystore.jks gradle.properties env; do
	gpg \
		--quiet \
		--batch \
		--yes \
		--decrypt \
		--passphrase="$GPG_PASSWORD" \
		--output $file \
		$file.gpg
done
