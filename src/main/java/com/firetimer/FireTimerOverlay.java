package com.firetimer;

import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;
import javax.inject.Inject;


public class FireTimerOverlay extends Overlay {
    private final FireTimerPlugin plugin;
    private final FireTimerConfig config;

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
        long elapsedTicks = fireTimeLocation.getTicksSinceFireLit();
        Integer override = fireTimeLocation.getMaxTicksOverride();

        final long ticksValue;
        final Color timerColor;

        if (fireType.isCanRefuel() && override != null)
        {
            long timeLeft = override - elapsedTicks;
            if (!this.config.allowNegative())
            {
                timeLeft = Math.max(0, timeLeft);
            }
            ticksValue = timeLeft;
            timerColor = timeLeft <= fireType.getLowWarningTicks()
                    ? this.config.lowTimerColor()
                    : this.config.normalTimerColor();
        }
        else if (fireType.isCanRefuel())
        {
            ticksValue = Math.max(0, elapsedTicks);
            timerColor = elapsedTicks >= (fireType.getMaxTicks() - fireType.getLowWarningTicks())
                    ? this.config.lowTimerColor()
                    : this.config.normalTimerColor();
        }
        else
        {
            long timeLeft = Math.max(0, fireType.getMaxTicks() - elapsedTicks);
            ticksValue = timeLeft;
            timerColor = timeLeft <= fireType.getLowWarningTicks()
                    ? this.config.lowTimerColor()
                    : this.config.normalTimerColor();
        }

        String displayText;
        if (this.config.displayUnit() == FireTimerConfig.DisplayUnit.TICKS)
        {
            displayText = Long.toString(ticksValue);
        }
        else
        {
            displayText = formatMmSs(fireTimeLocation, fireType, override);
        }

        final Point canvasPoint = fireTimeLocation.getFire().getCanvasTextLocation(graphics, displayText, 40);
        if (canvasPoint != null)
        {
            OverlayUtil.renderTextLocation(graphics, canvasPoint, displayText, timerColor);
        }
    }

    private String formatMmSs(FireTimeLocation loc, FireType fireType, Integer override)
    {
        // Wall-clock-driven so the displayed seconds tick down once per real
        // second instead of jittering with the ~600 ms game tick boundary.
        long elapsedMillis = System.currentTimeMillis() - loc.getTickFireStartedMillis();

        long secondsValue;
        if (fireType.isCanRefuel() && override != null)
        {
            long timeLeftMillis = (long) override * 600L - elapsedMillis;
            if (!this.config.allowNegative())
            {
                timeLeftMillis = Math.max(0L, timeLeftMillis);
            }
            secondsValue = timeLeftMillis / 1000L;
        }
        else if (fireType.isCanRefuel())
        {
            secondsValue = Math.max(0L, elapsedMillis / 1000L);
        }
        else
        {
            long timeLeftMillis = Math.max(0L, (long) fireType.getMaxTicks() * 600L - elapsedMillis);
            secondsValue = timeLeftMillis / 1000L;
        }

        boolean negative = secondsValue < 0;
        long abs = Math.abs(secondsValue);
        long minutes = abs / 60;
        long seconds = abs % 60;
        String formatted = minutes + ":" + (seconds < 10 ? "0" + seconds : Long.toString(seconds));
        return negative ? "-" + formatted : formatted;
    }
}
