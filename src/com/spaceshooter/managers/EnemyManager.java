package com.spaceshooter.managers;

import com.spaceshooter.entities.Enemy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EnemyManager {

  private List<Enemy> enemies;

  private Random random;

  private int spawnCooldown = 0;

  private int spawnDelay = 60;

  private double speedMultiplier = 1.0;

  private boolean spawningEnabled = true;

  public EnemyManager() {
    enemies = new ArrayList<>();
    random = new Random();
  }

  public void update(int panelWidth, int panelHeight) {

    if (spawningEnabled) {

      if (spawnCooldown > 0) {
        spawnCooldown--;
      }

      if (spawnCooldown == 0) {

        int enemyX = random.nextInt(
            Math.max(1, panelWidth - 60));

        int baseSpeed = random.nextInt(2) + 2;

        int enemySpeed = Math.max(
            1,
            (int) Math.round(baseSpeed * speedMultiplier));

        enemies.add(new Enemy(enemyX, -40, enemySpeed));

        spawnCooldown = spawnDelay;
      }
    }

    Iterator<Enemy> iterator = enemies.iterator();

    while (iterator.hasNext()) {

      Enemy enemy = iterator.next();

      enemy.update();

      if (enemy.isOffScreen(panelHeight)) {
        iterator.remove();
      }
    }
  }

  public void setLevelDifficulty(
      int newSpawnDelay,
      double newSpeedMultiplier) {

    spawnDelay = newSpawnDelay;
    speedMultiplier = newSpeedMultiplier;
  }

  public void setSpawningEnabled(boolean enabled) {
    spawningEnabled = enabled;
  }

  public void resetForNextLevel() {
    enemies.clear();
    spawnCooldown = 0;
    spawningEnabled = true;
  }

  public List<Enemy> getEnemies() {
    return enemies;
  }
}