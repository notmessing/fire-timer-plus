package com.firetimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("firetimer")
public interface FireTimerConfig extends Config
{
	@ConfigItem(
		keyName = "circle",
		name = "Tile Timer",
		description = "Use a visual timer drawn over the fire rather than a clock-style countdown."
	)
	default Boolean circle()
	{
		return false;
	}
}
