package com.spaceshooter.managers;

public class LevelManager {

  public enum LevelState {
    ACTIVE,
    BOSS_FIGHT,
    LEVEL_COMPLETE,
    GAME_COMPLETE
  }

  private int currentLevel = 1;
  private int enemiesDefeated = 0;

  private final int maxLevel = 3;

  private final int[] enemiesRequired = { 10, 15, 20 };
  private final double[] enemySpeedMultiplier = { 1.0, 1.3, 1.6 };
  private final int[] enemySpawnDelay = { 60, 45, 30 };
  private final double[] bossHealthMultiplier = { 1.0, 1.5, 2.0 };

  private LevelState state = LevelState.ACTIVE;

  public void registerEnemyDefeats(int count) {
    if (state != LevelState.ACTIVE || count <= 0) {
      return;
    }

    enemiesDefeated += count;

    if (enemiesDefeated >= getEnemiesRequired()) {
      enemiesDefeated = getEnemiesRequired();
    }
  }

  public boolean isBossReady() {
    return state == LevelState.ACTIVE
        && enemiesDefeated >= getEnemiesRequired();
  }

  public void startBossFight() {
    if (isBossReady()) {
      state = LevelState.BOSS_FIGHT;
    }
  }

  public void bossDefeated() {
    if (state != LevelState.BOSS_FIGHT) {
      return;
    }

    if (currentLevel >= maxLevel) {
      state = LevelState.GAME_COMPLETE;
    } else {
      state = LevelState.LEVEL_COMPLETE;
    }
  }

  public void nextLevel() {
    if (state == LevelState.LEVEL_COMPLETE
        && currentLevel < maxLevel) {

      currentLevel++;
      enemiesDefeated = 0;
      state = LevelState.ACTIVE;
    }
  }

  public void reset() {
    currentLevel = 1;
    enemiesDefeated = 0;
    state = LevelState.ACTIVE;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public int getEnemiesDefeated() {
    return enemiesDefeated;
  }

  public int getEnemiesRequired() {
    return enemiesRequired[currentLevel - 1];
  }

  public int getEnemiesRemaining() {
    return Math.max(0, getEnemiesRequired() - enemiesDefeated);
  }

  public double getEnemySpeedMultiplier() {
    return enemySpeedMultiplier[currentLevel - 1];
  }

  public int getEnemySpawnDelay() {
    return enemySpawnDelay[currentLevel - 1];
  }

  public double getBossHealthMultiplier() {
    return bossHealthMultiplier[currentLevel - 1];
  }

  public LevelState getState() {
    return state;
  }

  public boolean isLevelComplete() {
    return state == LevelState.LEVEL_COMPLETE;
  }

  public boolean isGameComplete() {
    return state == LevelState.GAME_COMPLETE;
  }

  public int getMaxLevel() {
    return maxLevel;
  }
}