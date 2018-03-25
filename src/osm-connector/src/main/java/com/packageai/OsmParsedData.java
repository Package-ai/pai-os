/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 *
 */
public class OsmParsedData {

	private final List<HighwayData> ways;
	private final List<NodeData> nodes;
	private List<RestrictionData> restrictions;

	public OsmParsedData(List<HighwayData> ways, List<NodeData> nodes, List<RestrictionData> restrictions) {
		this.ways = ways;
		this.nodes = nodes;
		this.restrictions = restrictions;
	}

	public List<HighwayData> getWays() {
		return ways;
	}

	public List<NodeData> getNodes() {
		return nodes;
	}

	public List<RestrictionData> getRestrictions() {
		return restrictions;
	}

	public static OsmParsedData fromFile(File file) throws FileNotFoundException {
		return OsmDataSink.read(file);
	}
}
