package com.firetimer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.ObjectID;

@Getter
public enum FireType
{
	REGULAR_FIRE(setOf(ObjectID.FIRE_26185), 200, 100, false),

	// Forester's Campfire (Forestry update). The six IDs cover the Regular,
	// Red, Green, Blue, White, and Purple log-color variants. Per the OSRS
	// Wiki the lifetime caps at 300 ticks (~3 min) regardless of refuel;
	// log-type inference and refuel tracking are layered in later phases.
	CAMPFIRE(setOf(49927, 49928, 49929, 49930, 49931, 49932), 300, 100, true);

	private final Set<Integer> objectIds;
	private final int maxTicks;
	private final int lowWarningTicks;
	private final boolean canRefuel;

	FireType(Set<Integer> objectIds, int maxTicks, int lowWarningTicks, boolean canRefuel)
	{
		this.objectIds = objectIds;
		this.maxTicks = maxTicks;
		this.lowWarningTicks = lowWarningTicks;
		this.canRefuel = canRefuel;
	}

	public static FireType fromObjectId(int objectId)
	{
		for (FireType type : values())
		{
			if (type.objectIds.contains(objectId))
			{
				return type;
			}
		}
		return null;
	}

	private static Set<Integer> setOf(int... ids)
	{
		Set<Integer> set = new HashSet<>();
		for (int id : ids)
		{
			set.add(id);
		}
		return Collections.unmodifiableSet(set);
	}
}
