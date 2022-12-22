package com.firetimer;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Fire Timer"
)
public class FireTimerPlugin extends Plugin
{
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

	@Override
	protected void startUp() throws Exception
	{
		this.fireIds = new HashMap<>();
		this.overlayManager.add(this.fireTimerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.fireIds.clear();
		this.overlayManager.remove(this.fireTimerOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
				event.getGameState() == GameState.HOPPING)
		{
			this.fireIds.clear();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned objectSpawned) {
		if (objectSpawned.getGameObject().getId() == ObjectID.FIRE_26185) {
			log.info("Fire spawned.");
			this.fireIds.putIfAbsent(objectSpawned.getGameObject().getHash(),
					new FireTimeLocation(
							objectSpawned.getGameObject(),
							objectSpawned.getGameObject().getWorldLocation(),
							0,
							this.lastTrueTickUpdate
					)
			);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned objectDespawned) {
		if (objectDespawned.getGameObject().getId() == ObjectID.FIRE_26185) {
			log.info("Fire despawned.");
			this.fireIds.remove(objectDespawned.getGameObject().getHash());
		}
	}

	@Subscribe
	public void onGameTick(GameTick change) {
		this.lastTrueTickUpdate = this.client.getTickCount();

		this.fireIds.forEach((fireIdHash, fireTimeLocation) ->
						fireTimeLocation.setTicksSinceFireLit(
								this.lastTrueTickUpdate - fireTimeLocation.getTickFireStarted()));
	}

	@Provides
	FireTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FireTimerConfig.class);
	}
}
