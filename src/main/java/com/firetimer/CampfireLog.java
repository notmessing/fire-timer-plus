package com.firetimer;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum CampfireLog
{
	LOGS(ItemID.LOGS, 3),
	ACHEY_TREE_LOGS(ItemID.ACHEY_TREE_LOGS, 3),
	OAK_LOGS(ItemID.OAK_LOGS, 10),
	WILLOW_LOGS(ItemID.WILLOW_LOGS, 17),
	TEAK_LOGS(ItemID.TEAK_LOGS, 19),
	ARCTIC_PINE_LOGS(ItemID.ARCTIC_PINE_LOGS, 22),
	MAPLE_LOGS(ItemID.MAPLE_LOGS, 24),
	MAHOGANY_LOGS(ItemID.MAHOGANY_LOGS, 26),
	YEW_LOGS(ItemID.YEW_LOGS, 31),
	BLISTERWOOD_LOGS(ItemID.BLISTERWOOD_LOGS, 32),
	MAGIC_LOGS(ItemID.MAGIC_LOGS, 38),
	REDWOOD_LOGS(ItemID.REDWOOD_LOGS, 45);

	private final int itemId;
	private final int ticksAdded;

	CampfireLog(int itemId, int ticksAdded)
	{
		this.itemId = itemId;
		this.ticksAdded = ticksAdded;
	}

	public static CampfireLog fromItemId(int itemId)
	{
		for (CampfireLog log : values())
		{
			if (log.itemId == itemId)
			{
				return log;
			}
		}
		return null;
	}
}
