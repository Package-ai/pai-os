/**
 * Copyright (c) 2022 Package.ai. All Rights Reserved.
 */
package com.packageai;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

/**
 *
 */
public class GeoFilterException extends OsmosisRuntimeException {


	/**
	 * Constructs a new exception.
	 */
	public GeoFilterException() {
		super();
	}


	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public GeoFilterException(String message) {
		super(message);
	}


	/**
	 * Constructs a new exception with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public GeoFilterException(Throwable cause) {
		super(cause);
	}


	/**
	 * Constructs a new exception with the specified detail message and
	 * cause.
	 *
	 * @param message the detail message.
	 * @param cause the cause.
	 */
	public GeoFilterException(String message, Throwable cause) {
		super(message, cause);
	}

}
