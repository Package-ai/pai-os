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

	private Map<Long, HighwayData> ways;
	private Map<Long, NodeData> nodes;
	private Map<Long, List<RestrictionData>> restrictions;

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
		this.ways = new HashMap<>();
		this.nodes = new HashMap<>();
		this.restrictions = new HashMap<>();
	}

	@Override
	public void process(EntityContainer entityContainer) {
		Entity entity = entityContainer.getEntity();
		EntityType type = entity.getType();
		switch (type) {
			case Way:
				Way way = (Way) entity;
				Map<String, String> wayTagValueMap = new TagCollectionImpl(way.getTags()).buildMap();
				boolean isHighway = wayTagValueMap.containsKey("highway");
				if (!isHighway) {
					break;
				}
				String highwayType = wayTagValueMap.get("highway");
				if (!WAY_TYPES.contains(highwayType)) {
					break;
				}
				double maxspeed = -1;
				if (wayTagValueMap.containsKey("maxspeed")) {
					String maxSpeedString = wayTagValueMap.get("maxspeed").split(" ")[0];
					try {
						maxspeed = Integer.parseInt(maxSpeedString);
					} catch (Exception e) {
					}
				}
				boolean isOneWay = wayTagValueMap.containsKey("oneway") && (wayTagValueMap.get("oneway").equalsIgnoreCase("true") || wayTagValueMap.get("oneway").equalsIgnoreCase("yes"));
				List<WayNode> wayNodes = way.getWayNodes();
				List<Long> nodeIds = wayNodes.stream().map(node -> node.getNodeId()).collect(Collectors.toList());
				boolean hasName = wayTagValueMap.containsKey("name");
				String name = null;
				if (hasName){
					name = wayTagValueMap.get("name");
				}

				HighwayData highwayData = new HighwayData(maxspeed, isOneWay, highwayType, nodeIds, name);
				ways.put(way.getId(), highwayData);
				break;
			case Node:
				Node node = (Node) entity;
				long id = node.getId();
				NodeData nodeData = new NodeData(node.getLatitude(), node.getLongitude());
				nodes.put(id, nodeData);
				break;
			case Relation:
				Relation relation = (Relation) entity;
				Map<String, String> relationTagValueMap = new TagCollectionImpl(relation.getTags()).buildMap();
				if (!relationTagValueMap.containsKey("restriction")){
					break;
				}
				String restrictionTypeName = relationTagValueMap.get("restriction");
				RestrictionType restrictionType = RestrictionType.getByOsmName(restrictionTypeName);

				List<RelationMember> members = relation.getMembers();
				long from = -1;
				long to = -1;
				long via = -1;
				boolean viaIsWay = false;
				int checksum = 0;
				for (RelationMember member : members){
					String memberRole = member.getMemberRole();
					if (memberRole.equals("from")){
						from = member.getMemberId();
						checksum++;
					}
					else if (memberRole.equals("to")){
						to = member.getMemberId();
						checksum++;
					}
					else if (memberRole.equals("via")){
						viaIsWay = member.getMemberType().equals(EntityType.Way);
						via = member.getMemberId();
						checksum++;
					}
				}

				if (checksum == 3){
					List<RestrictionData> restrictionData = restrictions.get(from);
					if (restrictionData == null){
						restrictionData = new ArrayList<>();
						restrictions.put(from, restrictionData);
					}
					restrictionData.add(new RestrictionData(restrictionType, from, to, via, viaIsWay));
				}
		}
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
		return new OsmParsedData(sink.getWays(), sink.getNodes(), sink.getRestrictions());
	}

	public static void main(String[] args) throws FileNotFoundException {
		OsmParsedData read = read(new File("./src/test/sydney.osm.gz"));
		Map<Long, NodeData> nodes = read.getNodes();
		Map<Long, HighwayData> ways = read.getWays();
		Map<Long, List<RestrictionData>> restrictions = read.getRestrictions();
		int t = 0;
	}

}
