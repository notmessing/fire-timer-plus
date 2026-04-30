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
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
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
	private static final int PENDING_LOG_TIMEOUT_TICKS = 30;

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

	private Map<WorldPoint, PendingLog> pendingLogs;

	@Override
	protected void startUp() throws Exception
	{
		this.fireIds = new HashMap<>();
		this.pendingLogs = new HashMap<>();
		this.overlayManager.add(this.fireTimerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.fireIds.clear();
		this.pendingLogs.clear();
		this.overlayManager.remove(this.fireTimerOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
				event.getGameState() == GameState.HOPPING)
		{
			this.fireIds.clear();
			this.pendingLogs.clear();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() != MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
		{
			return;
		}

		Widget selected = client.getSelectedWidget();
		if (selected == null)
		{
			return;
		}
		int itemId = selected.getItemId();
		if (itemId <= 0)
		{
			return;
		}

		CampfireLog log = CampfireLog.fromItemId(itemId);
		if (log == null)
		{
			return;
		}

		WorldPoint targetTile = WorldPoint.fromScene(
				client,
				event.getMenuEntry().getParam0(),
				event.getMenuEntry().getParam1(),
				client.getPlane()
		);
		this.pendingLogs.put(targetTile, new PendingLog(log, this.lastTrueTickUpdate));
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned objectSpawned) {
		GameObject obj = objectSpawned.getGameObject();
		FireType fireType = FireType.fromObjectId(obj.getId());
		if (fireType == null) {
			return;
		}

		Integer maxOverride = null;
		if (fireType.isCanRefuel()) {
			PendingLog pending = this.pendingLogs.remove(obj.getWorldLocation());
			if (pending != null) {
				maxOverride = Math.min(fireType.getMaxTicks(), pending.logType.getTicksAdded());
			}
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

		this.pendingLogs.entrySet().removeIf(e ->
				(this.lastTrueTickUpdate - e.getValue().tickStamp) > PENDING_LOG_TIMEOUT_TICKS);
	}

	@Provides
	FireTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FireTimerConfig.class);
	}

	private static final class PendingLog
	{
		final CampfireLog logType;
		final long tickStamp;

		PendingLog(CampfireLog logType, long tickStamp)
		{
			this.logType = logType;
			this.tickStamp = tickStamp;
		}
	}
}
