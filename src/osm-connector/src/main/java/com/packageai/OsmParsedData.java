/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class OsmParsedData {

	private final Map<Long, WayData> ways;
	private final Map<Long, NodeData> nodes;
	private final List<RestrictionData> restrictions;
	private final long lastModified;

	public OsmParsedData(Map<Long, WayData> ways, Map<Long, NodeData> nodes, List<RestrictionData> restrictions, long lastModified) {
		this.ways = ways;
		this.nodes = nodes;
		this.restrictions = restrictions;
		this.lastModified = lastModified;
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

	public long getLastModified() {
		return lastModified;
	}

	public static OsmParsedData fromFile(File file) throws IOException {
		return OsmDataSink.read(file, null, file.lastModified());
	}

	public static OsmParsedData fromFile(File file, File polygonFile) throws IOException {
		return OsmDataSink.read(file, polygonFile, Math.max(file.lastModified(), polygonFile.lastModified()));
	}

	public static OsmParsedData fromFile(InputStream file, InputStream polygonFile, long lastModified) throws IOException {
		return OsmDataSink.read(file, polygonFile, lastModified);
	}
}
