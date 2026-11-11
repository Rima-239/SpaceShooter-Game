package com.spaceshooter.managers;

import com.spaceshooter.entities.PowerUp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PowerUpManager {

  private final List<PowerUp> powerUps = new ArrayList<>();

  private final Random random = new Random();

  private int spawnCooldown = 0;

  // Power-up spawn interval
  private final int spawnDelay = 600;

  public void update(int panelWidth, int panelHeight) {

    // Update existing power-ups
    Iterator<PowerUp> iterator = powerUps.iterator();

    while (iterator.hasNext()) {

      PowerUp powerUp = iterator.next();

      powerUp.update();

      if (powerUp.isOffScreen(panelHeight)) {
        iterator.remove();
      }
    }

    // Spawn power-ups
    if (spawnCooldown > 0) {
      spawnCooldown--;
    }

    if (spawnCooldown == 0) {

      int x = random.nextInt(Math.max(1, panelWidth - 24));

      powerUps.add(new PowerUp(x, -24));

      spawnCooldown = spawnDelay;
    }
  }

  public List<PowerUp> getPowerUps() {
    return powerUps;
  }
}