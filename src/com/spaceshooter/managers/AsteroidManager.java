package com.spaceshooter.managers;

import com.spaceshooter.entities.Asteroid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AsteroidManager {

  private final List<Asteroid> asteroids = new ArrayList<>();

  private final Random random = new Random();

  private int spawnCooldown = 0;

  // Lower value means more frequent asteroids
  private final int spawnDelay = 100;

  public void update(int panelWidth, int panelHeight) {

    // Update existing asteroids
    Iterator<Asteroid> iterator = asteroids.iterator();

    while (iterator.hasNext()) {

      Asteroid asteroid = iterator.next();

      asteroid.update();

      if (asteroid.isOffScreen(panelHeight)) {
        iterator.remove();
      }
    }

    // Spawn new asteroids
    if (spawnCooldown > 0) {
      spawnCooldown--;
    }

    if (spawnCooldown == 0) {

      int size = random.nextInt(25) + 25;
      int x = random.nextInt(Math.max(1, panelWidth - size));
      int speed = random.nextInt(3) + 2;

      Asteroid asteroid = new Asteroid(
          x,
          -size,
          size,
          speed);

      asteroids.add(asteroid);

      spawnCooldown = spawnDelay;
    }
  }

  public List<Asteroid> getAsteroids() {
    return asteroids;
  }
}