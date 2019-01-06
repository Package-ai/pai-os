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
	private short highwayType;
	private long[] wayNodeIds;
	private String name;
	private boolean hgv;//Heavy Goods Vehicle. true of allowed, false if restricted

	public WayData(double speedLimit, boolean oneWay, short highwayType, long[] wayNodeIds, String name, boolean hgv) {
		this.speedLimit = (int)speedLimit;
		this.oneWay = oneWay;
		this.highwayType = highwayType;
		this.wayNodeIds = wayNodeIds;
		this.name = name;
		this.hgv = hgv;
	}

	public int getSpeedLimit() {
		return speedLimit;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public short getWayType() {
		return highwayType;
	}

	public long[] getWayNodeIds() {
		return wayNodeIds;
	}

	public String getName(){
		return name;
	}

	public boolean getHgv(){
		return hgv;
	}

}
