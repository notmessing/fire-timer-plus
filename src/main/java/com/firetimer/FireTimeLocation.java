package com.firetimer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
public class FireTimeLocation {
    private GameObject fire;
    private final WorldPoint worldPoint;
    private long timeSinceFireLit;
    private Instant tickFireStarted;
}
