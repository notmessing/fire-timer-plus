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
        FireType fireType = fireTimeLocation.getFireType();
        long elapsed = fireTimeLocation.getTicksSinceFireLit();
        Integer override = fireTimeLocation.getMaxTicksOverride();

        final long displayValue;
        final Color timerColor;

        if (fireType.isCanRefuel() && override != null)
        {
            long timeLeft = Math.max(0, override - elapsed);
            displayValue = timeLeft;
            timerColor = timeLeft <= fireType.getLowWarningTicks()
                    ? this.config.lowTimerColor()
                    : this.config.normalTimerColor();
        }
        else if (fireType.isCanRefuel())
        {
            displayValue = Math.max(0, elapsed);
            timerColor = elapsed >= (fireType.getMaxTicks() - fireType.getLowWarningTicks())
                    ? this.config.lowTimerColor()
                    : this.config.normalTimerColor();
        }
        else
        {
            long timeLeft = Math.max(0, fireType.getMaxTicks() - elapsed);
            displayValue = timeLeft;
            timerColor = timeLeft <= fireType.getLowWarningTicks()
                    ? this.config.lowTimerColor()
                    : this.config.normalTimerColor();
        }

        String displayText = format.format(displayValue);
        final Point canvasPoint = fireTimeLocation.getFire().getCanvasTextLocation(graphics, displayText, 40);
        if (canvasPoint != null)
        {
            OverlayUtil.renderTextLocation(graphics, canvasPoint, displayText, timerColor);
        }
    }
}
