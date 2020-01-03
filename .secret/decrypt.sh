#!/bin/sh
cd "$(dirname "$0")"
if [ -z "$GPG_PASSWORD" ]; then
	echo Env variable GPG_PASSWORD must be defined
	exit 1
fi
gpg \
	--quiet \
	--batch \
	--yes \
	--decrypt \
	--passphrase="$GPG_PASSWORD" \
	--output secret.tar.gz \
	secret
tar -xzf secret.tar.gz
rm secret.tar.gz
