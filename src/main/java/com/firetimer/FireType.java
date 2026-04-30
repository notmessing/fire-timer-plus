package com.firetimer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.ObjectID;

@Getter
public enum FireType
{
	REGULAR_FIRE(setOf(ObjectID.FIRE_26185), 200, 100, false);

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
