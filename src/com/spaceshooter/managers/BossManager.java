package com.spaceshooter.managers;

import com.spaceshooter.entities.Boss;

public class BossManager {

  private Boss boss;

  public void update(int panelWidth) {
    update(panelWidth, 600);
  }

  public void update(int panelWidth, int panelHeight) {
    if (boss != null && !boss.isDestroyed()) {
      boss.update(panelWidth, panelHeight);
    }
  }

  public void spawnBoss(
      int panelWidth,
      double healthMultiplier) {

    if (boss == null) {
      boss = new Boss(panelWidth, healthMultiplier);
    }
  }

  public void removeDefeatedBoss() {
    if (boss != null && boss.isDestroyed()) {
      boss = null;
    }
  }

  public Boss getBoss() {
    return boss;
  }

  public void reset() {
    boss = null;
  }
}
