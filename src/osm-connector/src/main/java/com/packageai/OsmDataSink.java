/**
 * Copyright (c) 2017 Package.ai. All Rights Reserved.
 */
package com.packageai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.openstreetmap.osmosis.areafilter.AreaFilterPluginLoader;
import org.openstreetmap.osmosis.areafilter.v0_6.AreaFilter;
import org.openstreetmap.osmosis.areafilter.v0_6.AreaFilterTaskManagerFactory;
import org.openstreetmap.osmosis.areafilter.v0_6.PolygonFilter;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.FastXmlReader;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.*;
import java.util.List;

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

	static OsmParsedData read(InputStream osmFile, InputStream polygonFile, long lastModified) throws IOException {
		OsmDataSink sink = new OsmDataSink();

		RunnableSource reader = new AyalReader(osmFile);

		if (polygonFile != null){

			GeoJsonObject object = new ObjectMapper().readValue(polygonFile, GeoJsonObject.class);
			GeoJsonObject geometry = ((FeatureCollection) object).getFeatures().get(0).getGeometry();
			if (geometry instanceof Polygon) {
				Polygon polygon = (Polygon) geometry;
				List<LngLatAlt> exteriorRing = polygon.getExteriorRing();

				Path2D.Double path = new Path2D.Double();
				path.moveTo(exteriorRing.get(0).getLongitude(), exteriorRing.get(0).getLatitude());
				for (int i = 1; i < exteriorRing.size(); i++){
					LngLatAlt lngLatAlt = exteriorRing.get(i);
					path.lineTo(lngLatAlt.getLongitude(), lngLatAlt.getLatitude());
				}
				Area area = new Area(path);
				AreaFilter areaFilter = new AreaFilterImpl(IdTrackerType.Dynamic, area, true, false, false, false);
				areaFilter.setSink(sink);

				reader.setSink(areaFilter);
			}
			else{
				reader.setSink(sink);
			}
		}
		else{
			reader.setSink(sink);
		}

		reader.run();
		OsmParsedData osmParsedData = new OsmParsedData(sink.getWays(), sink.getNodes(), sink.getRestrictions(), lastModified);
		return osmParsedData;
	}

	static OsmParsedData read(File file, File polygonFile, long lastModified) throws Exception {
		return read (new FileInputStream(file), polygonFile != null ? new FileInputStream(polygonFile) : null, lastModified);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(System.currentTimeMillis());
		OsmParsedData read = read(new File("./src/test/sydney.osm.pbf"), new File("./src/test/sydney.geojson"), new File("./src/test/sydney.osm.pbf").lastModified());
		System.out.println(System.currentTimeMillis());

		Map<Long, NodeData> nodes = read.getNodes();
	}

}
