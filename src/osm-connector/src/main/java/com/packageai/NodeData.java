/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

/**
 *
 */
public class NodeData {

	private double latitude;
	private double longitude;

	public NodeData(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

}
