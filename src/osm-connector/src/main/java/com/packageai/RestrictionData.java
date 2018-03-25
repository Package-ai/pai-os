/**
 * Copyright (c) 2018 Package.ai. All Rights Reserved.
 */
package com.packageai;

/**
 *
 */
public class RestrictionData {

	boolean negativeRestriction;
	long from;
	long to;
	long via;
	boolean viaIsWay;

	public RestrictionData(boolean negativeRestriction, long from, long to, long via, boolean viaIsWay) {
		this.negativeRestriction = negativeRestriction;
		this.from = from;
		this.to = to;
		this.via = via;
		this.viaIsWay = viaIsWay;
	}


	public boolean isNegativeRestriction() {
		return negativeRestriction;
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