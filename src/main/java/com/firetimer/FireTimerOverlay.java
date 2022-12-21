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

    final int FIRE_TOTAL_TICKS = 119;

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
        plugin.getFireIds().forEach(fireTimeLocation -> renderTimer(fireTimeLocation, graphics));
        return null;
    }

    private void renderTimer(final FireTimeLocation fireTimeLocation, final Graphics2D graphics)
    {
        double timeLeft = FIRE_TOTAL_TICKS - fireTimeLocation.getTimeSinceFireLit();

        double lowDisplay = 59;

        Color timerColor = Color.GREEN;

        if (timeLeft < 0)
        {
            timeLeft = 0;
        }

        if (timeLeft <= lowDisplay)
        {
            timerColor = Color.RED;
        }

        String timeLeftString = String.valueOf(format.format(timeLeft));

        final Point canvasPoint = fireTimeLocation.getFire().getCanvasTextLocation(graphics, timeLeftString, 40);

        if (canvasPoint != null && (FIRE_TOTAL_TICKS >= timeLeft))
        {
            OverlayUtil.renderTextLocation(graphics, canvasPoint, timeLeftString, timerColor);
        }
    }
}
