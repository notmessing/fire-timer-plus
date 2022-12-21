package com.firetimer;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.Instant;
import java.util.ArrayList;

@Slf4j
@PluginDescriptor(
	name = "Fire Timer"
)
public class FireTimerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private FireTimerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private FireTimerOverlay fireTimerOverlay;

	@Getter(AccessLevel.PACKAGE)
	private Instant lastTickUpdate;

	@Getter(AccessLevel.PACKAGE)
	private long lastTrueTickUpdate;

	@Getter(AccessLevel.PACKAGE)
	private ArrayList<FireTimeLocation> fireIds;

	private int counter = 0;

	private boolean playerIsFiremaking = false;

	@Override
	protected void startUp() throws Exception
	{
		this.fireIds = new ArrayList<>(10);
		this.overlayManager.add(fireTimerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
		fireIds.clear();
		overlayManager.remove(fireTimerOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
				event.getGameState() == GameState.HOPPING)
		{
			fireIds.clear();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned objectSpawned)
	{
		if (objectSpawned.getGameObject().getId() == ObjectID.FIRE_26185 && this.playerIsFiremaking) {
			log.info("Player fire spawned.");
			this.playerIsFiremaking = false;
			this.fireIds.add(
					new FireTimeLocation(
							objectSpawned.getGameObject(),
							objectSpawned.getGameObject().getWorldLocation(),
							0,
							Instant.now()
					)
			);
		}
	}

	@Subscribe
	public void onGameTick(GameTick change) {
		lastTrueTickUpdate = client.getTickCount();
		lastTickUpdate = Instant.now();

		for (FireTimeLocation fireTimeLocation : this.fireIds) {
			fireTimeLocation.setTimeSinceFireLit(lastTickUpdate.getEpochSecond() - fireTimeLocation.getTickFireStarted().getEpochSecond());
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged change) {
		Actor animationActor = change.getActor();
		if (client.getLocalPlayer() != null) {
			if (animationActor.getName() == client.getLocalPlayer().getName() && animationActor.getAnimation() == AnimationID.FIREMAKING) {
				log.info("Firemaking animation started.");
				this.playerIsFiremaking = true;
			}
		}
	}


	@Provides
	FireTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FireTimerConfig.class);
	}
}
