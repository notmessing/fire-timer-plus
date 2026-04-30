package com.firetimer;

import lombok.Getter;

@Getter
public enum CampfireLog
{
	// firemakingXpDeltas = the set of integer XP deltas a single log of this
	// type can produce in StatChanged. OSRS tracks XP internally as
	// (actual * 10), so logs with fractional XP (e.g. mahogany 157.5,
	// yew 202.5, magic 303.8, ironwood 220.5) appear as alternating
	// integer deltas as the fractional accumulator rolls over.
	//
	// ticksAdded = burn-time gain when added to a Forester's Campfire.
	// Camphor / Ironwood / Rosewood ticks are linearly extrapolated from
	// the known-level pattern (~0.5 ticks per Firemaking level) -- the
	// OSRS Wiki does not list confirmed values for these three yet.
	LOGS(new int[]{40}, 3),
	OAK_LOGS(new int[]{60}, 10),
	WILLOW_LOGS(new int[]{90}, 17),
	TEAK_LOGS(new int[]{105}, 19),
	ARCTIC_PINE_LOGS(new int[]{125}, 22),
	MAPLE_LOGS(new int[]{135}, 24),
	MAHOGANY_LOGS(new int[]{157, 158}, 26),
	YEW_LOGS(new int[]{202, 203}, 31),
	BLISTERWOOD_LOGS(new int[]{96}, 32),
	CAMPHOR_LOGS(new int[]{180}, 33),                // ticksAdded estimated
	MAGIC_LOGS(new int[]{303, 304}, 38),
	IRONWOOD_LOGS(new int[]{220, 221}, 40),          // ticksAdded estimated
	REDWOOD_LOGS(new int[]{350}, 45),
	ROSEWOOD_LOGS(new int[]{268}, 46);               // ticksAdded estimated

	// A freshly-lit Forester's Campfire burns for
	// (CAMPFIRE_BASE_TICKS + log.ticksAdded) ticks. Refuels add only the
	// log's ticksAdded on top of the current remaining time, capped at
	// FireType.CAMPFIRE.getMaxTicks() (300).
	public static final int CAMPFIRE_BASE_TICKS = 99;

	private final int[] firemakingXpDeltas;
	private final int ticksAdded;

	CampfireLog(int[] firemakingXpDeltas, int ticksAdded)
	{
		this.firemakingXpDeltas = firemakingXpDeltas;
		this.ticksAdded = ticksAdded;
	}

	public static CampfireLog fromFiremakingXpDelta(int xpDelta)
	{
		for (CampfireLog log : values())
		{
			for (int v : log.firemakingXpDeltas)
			{
				if (v == xpDelta)
				{
					return log;
				}
			}
		}
		return null;
	}
}
