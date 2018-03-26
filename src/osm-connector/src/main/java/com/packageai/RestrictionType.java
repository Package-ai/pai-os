/**
 * Copyright (c) 2018 Package.ai. All Rights Reserved.
 */

package com.packageai;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */

public enum RestrictionType {

	NO_RIGHT_TURN ("no_right_turn"),
	NO_LEFT_TURN ("no_left_turn"),
	NO_U_TURN ("no_u_turn"),
	NO_STRAIGHT_ON ("no_straight_on"),
	ONLY_RIGHT_TURN ("only_right_turn"),
	ONLY_LEFT_TURN ("only_left_turn"),
	ONLY_STRAIGHT_ON ("only_straight_on"),
	NO_ENTRY ("no_entry"),
	NO_EXIT ("no_exit");

	private static Map<String, RestrictionType> restrictionNameMap = new HashMap<>();

	static{
		Arrays.stream(RestrictionType.values()).forEach(restrictionType ->
						restrictionNameMap.put(restrictionType.osmName, restrictionType));
	}

	public static RestrictionType getByOsmName(String osmName){
		return restrictionNameMap.get(osmName);
	}

	private final String osmName;

	RestrictionType(String osmName) {
		this.osmName = osmName;
	}
}
