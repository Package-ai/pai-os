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
	private boolean highwayTrafficSignal;
	private boolean crosswalkTrafficSignal;

	public NodeData(double latitude, double longitude, boolean highwayTrafficSignal, boolean crosswalkTrafficSignal) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.highwayTrafficSignal = highwayTrafficSignal;
		this.crosswalkTrafficSignal = crosswalkTrafficSignal;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public boolean isHighwayTrafficSignal() {
		return highwayTrafficSignal;
	}

	public boolean isCrosswalkTrafficSignal() {
		return crosswalkTrafficSignal;
	}
}
