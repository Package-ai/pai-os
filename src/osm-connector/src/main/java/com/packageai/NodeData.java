/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

/**
 *
 */
public class NodeData {

	private Long nodeId;
	private double latitude;
	private double longitude;

	public NodeData(Long nodeId, double latitude, double longitude) {
		this.nodeId = nodeId;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

}
