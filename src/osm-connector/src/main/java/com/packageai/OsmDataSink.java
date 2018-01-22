/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import com.google.common.collect.Sets;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class OsmDataSink implements Sink {

	private List<HighwayData> ways;
	private List<NodeData> nodes;

	public static final Set<String> WAY_TYPES = Sets.newHashSet("motorway",
			"trunk",
			"primary",
			"secondary",
			"tertiary",
			"unclassified",
			"residential",
			"service",
			"motorway_link",
			"trunk_link",
			"primary_link",
			"secondary_link",
			"tertiary_link",
			"living_street",
			"road");

	OsmDataSink(){
		this.ways = new ArrayList<>();
		this.nodes = new ArrayList<>();
	}

	@Override
	public void process(EntityContainer entityContainer) {
		Entity entity = entityContainer.getEntity();
		EntityType type = entity.getType();
		switch (type) {
			case Way:
				Way way = (Way) entity;
				Map<String, String> tagValueMap = new TagCollectionImpl(way.getTags()).buildMap();
				boolean isHighway = tagValueMap.containsKey("highway");
				if (!isHighway) {
					break;
				}
				String highwayType = tagValueMap.get("highway");
				if (!WAY_TYPES.contains(highwayType)) {
					break;
				}
				double maxspeed = -1;
				if (tagValueMap.containsKey("maxspeed")) {
					String maxSpeedString = tagValueMap.get("maxspeed").split(" ")[0];
					try {
						maxspeed = Integer.parseInt(maxSpeedString);
					} catch (Exception e) {
					}
				}
				boolean isOneWay = tagValueMap.containsKey("oneway") && (tagValueMap.get("oneway").equalsIgnoreCase("true") || tagValueMap.get("oneway").equalsIgnoreCase("yes"));
				List<WayNode> wayNodes = way.getWayNodes();
				List<Long> nodeIds = wayNodes.stream().map(node -> node.getNodeId()).collect(Collectors.toList());
				HighwayData highwayData = new HighwayData(maxspeed, isOneWay, highwayType, nodeIds);
				ways.add(highwayData);
				break;
			case Node:
				Node node = (Node) entity;
				long id = node.getId();
				NodeData nodeData = new NodeData(id, node.getLatitude(), node.getLongitude());
				nodes.add(nodeData);
		}
	}

	public List<HighwayData> getWays() {
		return ways;
	}

	public List<NodeData> getNodes() {
		return nodes;
	}

	@Override
	public void initialize(Map<String, Object> metaData) {

	}

	@Override
	public void complete() {

	}

	@Override
	public void close() {

	}

	static OsmParsedData read(File file) throws FileNotFoundException {

		OsmDataSink sink = new OsmDataSink();

		boolean pbf = false;
		CompressionMethod compression = CompressionMethod.None;

		if (file.getName().endsWith(".pbf")) {
			pbf = true;
		} else if (file.getName().endsWith(".gz")) {
			compression = CompressionMethod.GZip;
		} else if (file.getName().endsWith(".bz2")) {
			compression = CompressionMethod.BZip2;
		}

		RunnableSource reader;

		if (pbf) {
			reader = new crosby.binary.osmosis.OsmosisReader(
					new FileInputStream(file));
		} else {
			reader = new XmlReader(file, false, compression);
		}

		reader.setSink(sink);
		reader.run();
		return new OsmParsedData(sink.getWays(), sink.getNodes());
	}

}
