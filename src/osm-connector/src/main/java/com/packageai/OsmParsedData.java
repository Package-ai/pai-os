/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class OsmParsedData {

	private final Map<Long, WayData> ways;
	private final Map<Long, NodeData> nodes;
	private List<RestrictionData> restrictions;

	public OsmParsedData(Map<Long, WayData> ways, Map<Long, NodeData> nodes, List<RestrictionData> restrictions) {
		this.ways = ways;
		this.nodes = nodes;
		this.restrictions = restrictions;
	}

	public Map<Long, WayData> getWays() {
		return ways;
	}

	public Map<Long, NodeData> getNodes() {
		return nodes;
	}

	public List<RestrictionData> getRestrictions() {
		return restrictions;
	}

	public static OsmParsedData fromFile(File file) throws FileNotFoundException {
		return OsmDataSink.read(file);
	}
}
