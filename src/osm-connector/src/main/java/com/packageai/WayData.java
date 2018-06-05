/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import java.util.List;

/**
 *
 */
public class WayData {

	private int speedLimit;
	private boolean oneWay;
	private String highwayType;
	private List<Long> wayNodeIds;
	private String name;

	public WayData(double speedLimit, boolean oneWay, String highwayType, List<Long> wayNodeIds, String name) {
		this.speedLimit = (int)speedLimit;
		this.oneWay = oneWay;
		this.highwayType = highwayType;
		this.wayNodeIds = wayNodeIds;
		this.name = name;
	}

	public int getSpeedLimit() {
		return speedLimit;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public String getWayType() {
		return highwayType;
	}

	public List<Long> getWayNodeIds() {
		return wayNodeIds;
	}

	public String getName(){
		return name;
	}

}
