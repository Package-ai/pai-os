/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.FastXmlReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *
 */
public class OsmDataSink implements Sink {

	private Map<Long, WayData> ways;
	private Map<Long, NodeData> nodes;
	private List<RestrictionData> restrictions;

	public static final Map<String, Short> WAY_TYPES = new HashMap<>();
	static{
		WAY_TYPES.put("motorway", (short)0);
		WAY_TYPES.put("trunk", (short)1);
		WAY_TYPES.put("primary", (short)2);
		WAY_TYPES.put("secondary", (short)3);
		WAY_TYPES.put("tertiary", (short)4);
		WAY_TYPES.put("unclassified", (short)5);
		WAY_TYPES.put("residential", (short)6);
		//WAY_TYPES.put("service", (short)7);
		WAY_TYPES.put("motorway_link", (short)8);
		WAY_TYPES.put("trunk_link", (short)9);
		WAY_TYPES.put("primary_link", (short)10);
		WAY_TYPES.put("secondary_link", (short)11);
		WAY_TYPES.put("tertiary_link", (short)12);
		WAY_TYPES.put("living_street", (short)13);
		WAY_TYPES.put("road", (short)14);
	}

	public static final Map<Short, Short> WAY_TYPE_HIERARCHY = new HashMap<>();
	static{
		WAY_TYPE_HIERARCHY.put((short)0, (short)0);
		WAY_TYPE_HIERARCHY.put((short)1, (short)1);
		WAY_TYPE_HIERARCHY.put((short)2, (short)2);
		WAY_TYPE_HIERARCHY.put((short)3, (short)3);
		WAY_TYPE_HIERARCHY.put((short)4, (short)4);
		WAY_TYPE_HIERARCHY.put((short)5, (short)5);
		WAY_TYPE_HIERARCHY.put((short)6, (short)5);
		//WAY_TYPE_HIERARCHY.put((short)7, (short)5);
		WAY_TYPE_HIERARCHY.put((short)8, (short)0);
		WAY_TYPE_HIERARCHY.put((short)9, (short)1);
		WAY_TYPE_HIERARCHY.put((short)10, (short)2);
		WAY_TYPE_HIERARCHY.put((short)11, (short)3);
		WAY_TYPE_HIERARCHY.put((short)12, (short)4);
		WAY_TYPE_HIERARCHY.put((short)13, (short)5);
		WAY_TYPE_HIERARCHY.put((short)14, (short)5);
	}

	OsmDataSink(){
		this.ways = new UnifiedMap<>();
		this.nodes = new UnifiedMap<>();
		this.restrictions = new ArrayList<>();
	}

	@Override
	public void process(EntityContainer entityContainer) {
		Entity entity = entityContainer.getEntity();
		EntityType type = entity.getType();
		switch (type) {
			case Way:
				Way way = (Way) entity;
				Map<String, String> wayTagValueMap = new TagCollectionImpl(way.getTags()).buildMap();
				if (!wayTagValueMap.containsKey("highway")) {
					break;
				}
				Short wayType = WAY_TYPES.get(wayTagValueMap.get("highway"));
				if (wayType == null) {
					break;
				}
				double maxspeed = -1;
				if (wayTagValueMap.containsKey("maxspeed")) {
					String[] split = wayTagValueMap.get("maxspeed").split(" ");
					try {
						maxspeed = Integer.parseInt(split[0]);
					} catch (Exception e) {
					}
					if (split.length > 1){
						if (split[1].equalsIgnoreCase("mph")){//is mph, we convert to kmh
							maxspeed *= 1.609344;
						}
					}
				}

				boolean isOneWay = wayTagValueMap.containsKey("oneway") && (wayTagValueMap.get("oneway").equalsIgnoreCase("true") || wayTagValueMap.get("oneway").equalsIgnoreCase("yes"));

				long[] nodeIds = new long[way.getWayNodes().size()];
				for (int i = 0; i < way.getWayNodes().size(); i++){
					nodeIds[i] = way.getWayNodes().get(i).getNodeId();
				}

				String name = null;
				if (wayTagValueMap.containsKey("name")){
					name = wayTagValueMap.get("name");
				}

				boolean hgv = true;
				if (wayTagValueMap.containsKey("hgv")){
					if (wayTagValueMap.get("hgv").equalsIgnoreCase("no") || wayTagValueMap.get("hgv").equalsIgnoreCase("false")){
						hgv =false;
					}
				}

				ways.put(way.getId(), new WayData(maxspeed, isOneWay, wayType, nodeIds, name, hgv));
				break;
			case Node:
				Node node = (Node) entity;
				Map<String, String> nodeTagValueMap = new TagCollectionImpl(node.getTags()).buildMap();

				String crossingValue = nodeTagValueMap.get("crossing");
				boolean crosswalkTrafficSignal =  (crossingValue != null && crossingValue.equals("traffic_signals"));

				String highwayValue = nodeTagValueMap.get("highway");
				boolean highwayTrafficSignal =  (highwayValue != null && highwayValue.equals("traffic_signals"));

				NodeData nodeData = new NodeData(node.getLatitude(), node.getLongitude(), highwayTrafficSignal, crosswalkTrafficSignal);
				nodes.put(node.getId(), nodeData);
				break;
			case Relation:
				Relation relation = (Relation) entity;
				Map<String, String> relationTagValueMap = new TagCollectionImpl(relation.getTags()).buildMap();
				if (!relationTagValueMap.containsKey("restriction")){
					break;
				}
				boolean negativeRestriction = false;
				if (relationTagValueMap.get("restriction").startsWith("no")){
					negativeRestriction = true;
				}
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
					restrictions.add(new RestrictionData(negativeRestriction, from, to, via, viaIsWay));
				}
				break;
		}
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
			reader = new AyalReader(
					new FileInputStream(file));
		} else {
			reader = new FastXmlReader(file, false, compression);
		}

		reader.setSink(sink);
		reader.run();
		OsmParsedData osmParsedData = new OsmParsedData(sink.getWays(), sink.getNodes(), sink.getRestrictions());
		sink = null;//for GC
		reader = null;//for CG
		return osmParsedData;
	}

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println(System.currentTimeMillis());
		OsmParsedData read = read(new File("./src/test/sydney.osm"));
		System.out.println(System.currentTimeMillis());

		Map<Long, NodeData> nodes = read.getNodes();
	}

}
