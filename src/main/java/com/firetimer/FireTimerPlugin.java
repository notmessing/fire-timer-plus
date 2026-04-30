package com.firetimer;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Fire Timer Plus",
	description = "Adds an in-game timer over player-made fires and Forester's Campfires.",
	tags = {"firemaking", "woodcutting", "forestry", "timer", "campfire", "overlay"}
)
public class FireTimerPlugin extends Plugin
{
	// How long after a firemaking XP drop we'll still attribute a fresh
	// campfire spawn to that drop (handles the case where StatChanged
	// fires before the GameObjectSpawned for the new campfire).
	private static final int RECENT_LOG_TIMEOUT_TICKS = 2;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private FireTimerOverlay fireTimerOverlay;

	@Getter(AccessLevel.PACKAGE)
	private long lastTrueTickUpdate;

	@Getter(AccessLevel.PACKAGE)
	private Map<Long, FireTimeLocation> fireIds;

	private int previousFiremakingXp;
	private CampfireLog lastConsumedLog;
	private long lastConsumedLogTick;

	@Override
	protected void startUp() throws Exception
	{
		this.fireIds = new HashMap<>();
		this.previousFiremakingXp = -1;
		this.lastConsumedLog = null;
		this.lastConsumedLogTick = -1;
		this.overlayManager.add(this.fireTimerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.fireIds.clear();
		this.previousFiremakingXp = -1;
		this.lastConsumedLog = null;
		this.lastConsumedLogTick = -1;
		this.overlayManager.remove(this.fireTimerOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
				event.getGameState() == GameState.HOPPING)
		{
			this.fireIds.clear();
			this.previousFiremakingXp = -1;
			this.lastConsumedLog = null;
			this.lastConsumedLogTick = -1;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.FIREMAKING)
		{
			return;
		}

		int currentXp = event.getXp();
		if (this.previousFiremakingXp < 0)
		{
			this.previousFiremakingXp = currentXp;
			return;
		}

		int delta = currentXp - this.previousFiremakingXp;
		this.previousFiremakingXp = currentXp;
		if (delta <= 0)
		{
			return;
		}

		CampfireLog log = CampfireLog.fromFiremakingXpDelta(delta);
		if (log == null)
		{
			return;
		}

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}
		WorldPoint playerLoc = player.getWorldLocation();
		if (playerLoc == null)
		{
			return;
		}

		FireTimeLocation campfire = findAdjacentRefuelableFire(playerLoc);
		if (campfire == null)
		{
			// No campfire adjacent yet -- this might be the lighting log
			// for a campfire that's about to spawn this tick. Hold the
			// log briefly; onGameObjectSpawned will pick it up.
			this.lastConsumedLog = log;
			this.lastConsumedLogTick = this.lastTrueTickUpdate;
			return;
		}

		applyLogToExistingCampfire(campfire, log);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned objectSpawned) {
		GameObject obj = objectSpawned.getGameObject();
		FireType fireType = FireType.fromObjectId(obj.getId());
		if (fireType == null) {
			return;
		}

		Integer maxOverride = null;
		if (fireType.isCanRefuel()
				&& this.lastConsumedLog != null
				&& (this.lastTrueTickUpdate - this.lastConsumedLogTick) <= RECENT_LOG_TIMEOUT_TICKS)
		{
			// XP drop already fired before this spawn -- this is our initial
			// light: 99 base + log.ticksAdded.
			maxOverride = Math.min(
					fireType.getMaxTicks(),
					CampfireLog.CAMPFIRE_BASE_TICKS + this.lastConsumedLog.getTicksAdded()
			);
			this.lastConsumedLog = null;
			this.lastConsumedLogTick = -1;
		}

		this.fireIds.putIfAbsent(obj.getHash(),
				new FireTimeLocation(
						obj,
						obj.getWorldLocation(),
						0,
						this.lastTrueTickUpdate,
						fireType,
						maxOverride
				)
		);
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned objectDespawned) {
		if (FireType.fromObjectId(objectDespawned.getGameObject().getId()) != null) {
			this.fireIds.remove(objectDespawned.getGameObject().getHash());
		}
	}

	@Subscribe
	public void onGameTick(GameTick change) {
		this.lastTrueTickUpdate = this.client.getTickCount();

		this.fireIds.forEach((fireIdHash, fireTimeLocation) ->
						fireTimeLocation.setTicksSinceFireLit(
								this.lastTrueTickUpdate - fireTimeLocation.getTickFireStarted()));

		if (this.lastConsumedLog != null
				&& (this.lastTrueTickUpdate - this.lastConsumedLogTick) > RECENT_LOG_TIMEOUT_TICKS)
		{
			this.lastConsumedLog = null;
			this.lastConsumedLogTick = -1;
		}
	}

	@Provides
	FireTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FireTimerConfig.class);
	}

	private FireTimeLocation findAdjacentRefuelableFire(WorldPoint playerLoc)
	{
		for (FireTimeLocation loc : this.fireIds.values())
		{
			if (!loc.getFireType().isCanRefuel())
			{
				continue;
			}
			WorldPoint cf = loc.getFire().getWorldLocation();
			if (cf == null || cf.getPlane() != playerLoc.getPlane())
			{
				continue;
			}
			if (Math.abs(cf.getX() - playerLoc.getX()) <= 1
					&& Math.abs(cf.getY() - playerLoc.getY()) <= 1)
			{
				return loc;
			}
		}
		return null;
	}

	private void applyLogToExistingCampfire(FireTimeLocation campfire, CampfireLog log)
	{
		FireType fireType = campfire.getFireType();
		long elapsed = this.lastTrueTickUpdate - campfire.getTickFireStarted();
		Integer currentMax = campfire.getMaxTicksOverride();

		long newRemaining;
		if (currentMax == null)
		{
			// First log we observe on this campfire. Two sub-cases:
			// (a) Just spawned this tick -- this is the initial light XP
			//     drop arriving after the GameObjectSpawned. Apply 99 base.
			// (b) Walked-up campfire -- conservatively assume 0 prior
			//     remaining and credit only this log's gain.
			if (elapsed <= RECENT_LOG_TIMEOUT_TICKS)
			{
				newRemaining = CampfireLog.CAMPFIRE_BASE_TICKS + log.getTicksAdded();
			}
			else
			{
				newRemaining = log.getTicksAdded();
			}
		}
		else
		{
			long currentRemaining = Math.max(0, currentMax - elapsed);
			newRemaining = currentRemaining + log.getTicksAdded();
		}

		newRemaining = Math.min(fireType.getMaxTicks(), newRemaining);
		campfire.setMaxTicksOverride((int) (elapsed + newRemaining));
	}
}
