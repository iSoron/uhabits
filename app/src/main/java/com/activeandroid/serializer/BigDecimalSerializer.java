package com.activeandroid.serializer;

import java.math.BigDecimal;

public final class BigDecimalSerializer extends TypeSerializer {
	public Class<?> getDeserializedType() {
		return BigDecimal.class;
	}

	public Class<?> getSerializedType() {
		return String.class;
	}

	public String serialize(Object data) {
		if (data == null) {
			return null;
		}

		return ((BigDecimal) data).toString();
	}

	public BigDecimal deserialize(Object data) {
		if (data == null) {
			return null;
		}

		return new BigDecimal((String) data);
	}
}