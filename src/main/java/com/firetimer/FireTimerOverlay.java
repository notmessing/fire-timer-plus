package com.firetimer;

import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.inject.Inject;


public class FireTimerOverlay extends Overlay {
    private final FireTimerPlugin plugin;
    private final FireTimerConfig config;

    NumberFormat format = new DecimalFormat("#");

    final int FIRE_MAX_TICKS = 200;
    final int FIRE_MIN_TICKS = 100;

    @Inject
    FireTimerOverlay(FireTimerPlugin plugin, FireTimerConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        this.plugin.getFireIds().forEach((fireIdHash, fireTimeLocation) -> renderTimer(fireTimeLocation, graphics));
        return null;
    }

    private void renderTimer(final FireTimeLocation fireTimeLocation, final Graphics2D graphics)
    {
        double timeLeft = this.FIRE_MAX_TICKS - fireTimeLocation.getTicksSinceFireLit();

        Color timerColor = this.config.normalTimerColor();

        if (timeLeft < 0)
        {
            timeLeft = 0;
        }

        if (timeLeft <= this.FIRE_MIN_TICKS)
        {
            timerColor = this.config.lowTimerColor();
        }

        String timeLeftString = String.valueOf(format.format(timeLeft));

        final Point canvasPoint = fireTimeLocation.getFire().getCanvasTextLocation(graphics, timeLeftString, 40);

        if (canvasPoint != null && (timeLeft >= 0))
        {
            OverlayUtil.renderTextLocation(graphics, canvasPoint, timeLeftString, timerColor);
        }
    }
}
