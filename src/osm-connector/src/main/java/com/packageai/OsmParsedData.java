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

	public OsmParsedData(List<HighwayData> ways, List<NodeData> nodes) {
		this.ways = ways;
		this.nodes = nodes;
	}

	public List<HighwayData> getWays() {
		return ways;
	}

	public List<NodeData> getNodes() {
		return nodes;
	}

	public static OsmParsedData fromFile(File file) throws FileNotFoundException {
		return OsmDataSink.read(file);
	}
}
