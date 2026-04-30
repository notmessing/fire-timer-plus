package com.firetimer;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("firetimer")
public interface FireTimerConfig extends Config
{
	enum DisplayUnit
	{
		TICKS("Ticks"),
		MM_SS("Time (m:ss)");

		private final String label;

		DisplayUnit(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	@ConfigItem(
			position = 1,
			keyName = "displayUnit",
			name = "Display unit",
			description = "Show the timer as raw game ticks or as m:ss"
	)
	default DisplayUnit displayUnit()
	{
		return DisplayUnit.TICKS;
	}

	@ConfigItem(
			position = 2,
			keyName = "allowNegative",
			name = "Allow negative",
			description = "When on, refuelable-fire countdowns continue past 0 (e.g. -1, -2) instead of clamping. A signal that someone refueled a campfire after our estimate ran out."
	)
	default boolean allowNegative()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			position = 3,
			keyName = "normalTimerColor",
			name = "Normal timer color",
			description = "Configures the color of the timer"
	)
	default Color normalTimerColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "lowTimerColor",
			name = "Low timer color",
			description = "Configures the color of the timer when remaining time is low"
	)
	default Color lowTimerColor()
	{
		return Color.RED;
	}
}
