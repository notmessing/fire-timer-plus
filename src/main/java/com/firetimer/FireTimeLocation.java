package com.firetimer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

@AllArgsConstructor
@Getter
@Setter
public class FireTimeLocation {
    private GameObject fire;
    private final WorldPoint worldPoint;
    private long ticksSinceFireLit;
    private long tickFireStarted;
}
