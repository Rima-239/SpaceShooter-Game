package com.spaceshooter.managers;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.entities.Asteroid;
import com.spaceshooter.entities.Boss;
import com.spaceshooter.entities.Bullet;
import com.spaceshooter.entities.Enemy;
import com.spaceshooter.entities.Particle;
import com.spaceshooter.entities.Player;
import com.spaceshooter.entities.PowerUp;
import com.spaceshooter.entities.Star;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameManager {

  private Player player;

  private List<Star> stars;
  private List<Bullet> bullets;
  private List<Particle> particles;

  private EnemyManager enemyManager;
  private AsteroidManager asteroidManager;
  private PowerUpManager powerUpManager;
  private BossManager bossManager;

  private final Random random = new Random();

  // Player score
  private int score = 0;

  // Current level reached
  private LevelManager levelManager = new LevelManager();

  private int levelTransitionCooldown = 0;

  // Player damage cooldown
  private int damageCooldown = 0;

  // Player shooting cooldown
  private int shootCooldown = 0;
  private final int shootDelay = 10;

  // Game over settings
  private boolean gameOver = false;

  // Player respawn settings
  private int respawnCooldown = 0;
  private final int respawnDelay = 90;

  // Screen shake
  private int screenShakeFrames = 0;
  private double screenShakeIntensity = 0;
  private int screenShakeX = 0;
  private int screenShakeY = 0;

  public GameManager(int panelWidth, int panelHeight) {
    player = new Player(
        panelWidth / 2 - 20,
        panelHeight - 100);

    stars = new ArrayList<>();
    bullets = new ArrayList<>();
    particles = new ArrayList<>();

    enemyManager = new EnemyManager();
    asteroidManager = new AsteroidManager();
    powerUpManager = new PowerUpManager();
    bossManager = new BossManager();
    applyLevelDifficulty();

    createStars(panelWidth, panelHeight);
  }

  // Create background stars
  private void createStars(int panelWidth, int panelHeight) {
    stars.clear();
    for (int i = 0; i < 120; i++) {
      int x = random.nextInt(Math.max(1, panelWidth));
      int y = random.nextInt(Math.max(1, panelHeight));

      int size = random.nextInt(3) + 1;
      int speed = random.nextInt(3) + 1;

      stars.add(new Star(x, y, size, speed));
    }
  }

  // Apply level difficulty settings to enemy manager
  private void applyLevelDifficulty() {
    enemyManager.setLevelDifficulty(
        levelManager.getEnemySpawnDelay(),
        levelManager.getEnemySpeedMultiplier());
  }

  public void triggerScreenShake(int frames, double intensity) {
    this.screenShakeFrames = frames;
    this.screenShakeIntensity = intensity;
  }

  // Main game update
  public void update(int panelWidth, int panelHeight) {

    // Update screen shake
    if (screenShakeFrames > 0) {
      screenShakeFrames--;
      screenShakeX = (int) ((random.nextDouble() * 2.0 - 1.0) * screenShakeIntensity);
      screenShakeY = (int) ((random.nextDouble() * 2.0 - 1.0) * screenShakeIntensity);
      screenShakeIntensity *= 0.90;
    } else {
      screenShakeX = 0;
      screenShakeY = 0;
    }

    // Update particles even during game over / victory
    updateParticles();

    // Stop updating gameplay after Game Over
    if (gameOver) {
      return;
    }

    // Wait briefly before starting the next level
    if (levelManager.isLevelComplete()) {
      levelTransitionCooldown--;

      if (levelTransitionCooldown <= 0) {
        levelManager.nextLevel();
        enemyManager.resetForNextLevel();
        applyLevelDifficulty();
      }
      return;
    }

    // Stop the game after completing the final level
    if (levelManager.isGameComplete()) {
      gameOver = true;
      SoundManager.getInstance().playLevelComplete();
      return;
    }

    // Update player
    player.update();
    player.keepInsideScreen(panelWidth, panelHeight);

    // Update background stars across dynamic screen bounds
    for (Star star : stars) {
      star.update(panelWidth, panelHeight);
    }

    // Update player bullets
    updateBullets();

    // Update enemies
    enemyManager.update(panelWidth, panelHeight);

    // Update asteroids
    asteroidManager.update(panelWidth, panelHeight);

    // Update power-ups
    powerUpManager.update(panelWidth, panelHeight);

    // Update boss and its attacks
    bossManager.update(panelWidth, panelHeight);

    Boss boss = bossManager.getBoss();

    // Check boss bullets hitting player
    if (boss != null && !boss.isDestroyed() && !player.isInvulnerable()) {
      CollisionManager.checkBossBulletPlayer(
          player,
          boss.getBullets(),
          particles);
    }

    // Check player bullets hitting enemies
    int enemiesDestroyed = CollisionManager.checkBulletEnemy(
        bullets,
        enemyManager.getEnemies(),
        particles);

    // Check player bullets hitting asteroids
    int asteroidsDestroyed = CollisionManager.checkBulletAsteroid(
        bullets,
        asteroidManager.getAsteroids(),
        particles);

    // Add score
    score += enemiesDestroyed * 10;
    score += asteroidsDestroyed * 5;

    // Register defeated enemies for level progression
    levelManager.registerEnemyDefeats(enemiesDestroyed);

    // Start boss fight when required enemies are defeated
    if (levelManager.isBossReady()) {
      enemyManager.setSpawningEnabled(false);
      enemyManager.getEnemies().clear();

      bossManager.spawnBoss(
          panelWidth,
          levelManager.getBossHealthMultiplier());

      levelManager.startBossFight();
      SoundManager.getInstance().playBossWarning();
      triggerScreenShake(20, 6.0);
    }

    // Check player bullets hitting boss
    int bossHits = CollisionManager.checkBulletBoss(
        bullets,
        boss,
        particles);

    // Add points for boss hits
    score += bossHits;

    // Check whether the boss has been defeated
    if (boss != null && boss.isDestroyed()) {
      CollisionManager.spawnParticles(particles, boss.getX() + 50, boss.getY() + 30, new Color(255, 0, 200), 50, 7f);
      SoundManager.getInstance().playExplosion();
      triggerScreenShake(30, 10.0);

      bossManager.removeDefeatedBoss();
      levelManager.bossDefeated();

      // Show level complete state for 3 seconds
      if (levelManager.isLevelComplete()) {
        levelTransitionCooldown = 180;
        SoundManager.getInstance().playLevelComplete();
      }

      // Final boss defeated
      if (levelManager.isGameComplete()) {
        gameOver = true;
        SoundManager.getInstance().playLevelComplete();
      }
    }

    // Reduce damage cooldown
    if (damageCooldown > 0) {
      damageCooldown--;
    }

    // Check player-enemy and player-asteroid collisions
    if (damageCooldown == 0 && !player.isInvulnerable()) {
      boolean playerHitByEnemy = CollisionManager.checkPlayerEnemy(
          player,
          enemyManager.getEnemies(),
          particles);

      boolean playerHitByAsteroid = false;

      if (!playerHitByEnemy) {
        playerHitByAsteroid = CollisionManager.checkPlayerAsteroid(
            player,
            asteroidManager.getAsteroids(),
            particles);
      }

      if (playerHitByEnemy || playerHitByAsteroid) {
        damageCooldown = 30;
        triggerScreenShake(12, 7.0);
      }
    }

    // Check power-up collection
    CollisionManager.checkPlayerPowerUp(
        player,
        powerUpManager.getPowerUps(),
        particles);

    // Handle player death
    if (player.getHealth() <= 0 && respawnCooldown == 0) {
      player.loseLife();
      triggerScreenShake(25, 9.0);
      CollisionManager.spawnParticles(particles, player.getX() + 20, player.getY() + 20, new Color(0, 240, 255), 35, 6f);

      if (player.getLives() <= 0) {
        gameOver = true;
        SoundManager.getInstance().playGameOver();
      } else {
        player.respawn(panelWidth, panelHeight);
        player.setInvulnerable(true);
        respawnCooldown = respawnDelay;
        damageCooldown = 0;
      }
    }

    // Handle respawn cooldown
    if (respawnCooldown > 0) {
      respawnCooldown--;
      if (respawnCooldown == 0) {
        player.setInvulnerable(false);
      }
    }

    // Handle shooting cooldown
    if (shootCooldown > 0) {
      shootCooldown--;
    }

    // Shoot while Space is held
    if (player.isShooting() && shootCooldown == 0) {
      bullets.addAll(player.shootBullets());
      shootCooldown = shootDelay;
      SoundManager.getInstance().playShoot();
    }
  }

  // Update player bullets
  private void updateBullets() {
    Iterator<Bullet> iterator = bullets.iterator();
    while (iterator.hasNext()) {
      Bullet bullet = iterator.next();
      bullet.update();
      if (bullet.isOffScreen()) {
        iterator.remove();
      }
    }
  }

  // Update particles
  private void updateParticles() {
    Iterator<Particle> iterator = particles.iterator();
    while (iterator.hasNext()) {
      Particle p = iterator.next();
      p.update();
      if (p.isDead()) {
        iterator.remove();
      }
    }
  }

  // Getters

  public Player getPlayer() {
    return player;
  }

  public List<Star> getStars() {
    return stars;
  }

  public List<Bullet> getBullets() {
    return bullets;
  }

  public List<Particle> getParticles() {
    return particles;
  }

  public List<Enemy> getEnemies() {
    return enemyManager.getEnemies();
  }

  public List<Asteroid> getAsteroids() {
    return asteroidManager.getAsteroids();
  }

  public List<PowerUp> getPowerUps() {
    return powerUpManager.getPowerUps();
  }

  public Boss getBoss() {
    return bossManager.getBoss();
  }

  public int getScore() {
    return score;
  }

  public int getPlayerHealth() {
    return player.getHealth();
  }

  public int getPlayerLives() {
    return player.getLives();
  }

  public int getLevelReached() {
    return levelManager.getCurrentLevel();
  }

  public LevelManager getLevelManager() {
    return levelManager;
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public int getScreenShakeX() {
    return screenShakeX;
  }

  public int getScreenShakeY() {
    return screenShakeY;
  }

  // Restart game
  public void restart(int panelWidth, int panelHeight) {
    player = new Player(
        panelWidth / 2 - 20,
        panelHeight - 100);

    bullets.clear();
    particles.clear();

    enemyManager = new EnemyManager();
    asteroidManager = new AsteroidManager();
    powerUpManager = new PowerUpManager();
    bossManager = new BossManager();

    createStars(panelWidth, panelHeight);

    score = 0;
    damageCooldown = 0;
    shootCooldown = 0;
    respawnCooldown = 0;

    levelManager.reset();
    levelTransitionCooldown = 0;

    applyLevelDifficulty();

    gameOver = false;
  }
}
