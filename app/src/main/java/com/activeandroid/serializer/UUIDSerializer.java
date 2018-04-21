package com.activeandroid.serializer;

import java.util.UUID;

public final class UUIDSerializer extends TypeSerializer {
	public Class<?> getDeserializedType() {
		return UUID.class;
	}

	public Class<?> getSerializedType() {
		return String.class;
	}

	public String serialize(Object data) {
		if (data == null) {
			return null;
		}

		return ((UUID) data).toString();
	}

	public UUID deserialize(Object data) {
		if (data == null) {
			return null;
		}

		return UUID.fromString((String)data);
	}
}