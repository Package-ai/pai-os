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

	private final Map<Long, HighwayData> ways;
	private final Map<Long, NodeData> nodes;
	private Map<Long, List<RestrictionData>> restrictions;

	public OsmParsedData(Map<Long, HighwayData> ways, Map<Long, NodeData> nodes, Map<Long, List<RestrictionData>> restrictions) {
		this.ways = ways;
		this.nodes = nodes;
		this.restrictions = restrictions;
	}

	public Map<Long, HighwayData> getWays() {
		return ways;
	}

	public Map<Long, NodeData> getNodes() {
		return nodes;
	}

	public Map<Long, List<RestrictionData>> getRestrictions() {
		return restrictions;
	}

	public static OsmParsedData fromFile(File file) throws FileNotFoundException {
		return OsmDataSink.read(file);
	}
}
