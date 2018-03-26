/**
 * Copyright (c) 2018 Package.ai. All Rights Reserved.
 */
package com.packageai;

/**
 *
 */
public class RestrictionData {

	RestrictionType restrictionType;
	long from;
	long to;
	long via;
	boolean viaIsWay;

	public RestrictionData(RestrictionType restrictionType, long from, long to, long via, boolean viaIsWay) {
		this.restrictionType = restrictionType;
		this.from = from;
		this.to = to;
		this.via = via;
		this.viaIsWay = viaIsWay;
	}


	public RestrictionType getRestrictionType() {
		return restrictionType;
	}

	public long getFrom() {
		return from;
	}

	public long getTo() {
		return to;
	}

	public long getVia() {
		return via;
	}

	public boolean isViaIsWay() {
		return viaIsWay;
	}
}