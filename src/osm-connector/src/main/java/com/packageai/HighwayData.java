/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import java.util.List;

/**
 *
 */
public class HighwayData {

	private double speedLimit;
	private boolean oneWay;
	private String highwayType;
	private List<Long> wayNodeIds;

	public HighwayData(double speedLimit, boolean oneWay, String highwayType, List<Long> wayNodeIds) {
		this.speedLimit = speedLimit;
		this.oneWay = oneWay;
		this.highwayType = highwayType;
		this.wayNodeIds = wayNodeIds;
	}

	public double getSpeedLimit() {
		return speedLimit;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public String getHighwayType() {
		return highwayType;
	}

	public List<Long> getWayNodeIds() {
		return wayNodeIds;
	}

}
