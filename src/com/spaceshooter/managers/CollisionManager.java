package com.spaceshooter.managers;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.entities.Asteroid;
import com.spaceshooter.entities.Boss;
import com.spaceshooter.entities.BossBullet;
import com.spaceshooter.entities.Bullet;
import com.spaceshooter.entities.Enemy;
import com.spaceshooter.entities.Particle;
import com.spaceshooter.entities.Player;
import com.spaceshooter.entities.PowerUp;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CollisionManager {

  private static final Random RANDOM = new Random();

  public static void spawnParticles(List<Particle> particles, double x, double y, Color color, int count, float size) {
    if (particles == null) return;
    for (int i = 0; i < count; i++) {
      double angle = RANDOM.nextDouble() * Math.PI * 2;
      double speed = 1.0 + RANDOM.nextDouble() * 3.5;
      double vx = Math.cos(angle) * speed;
      double vy = Math.sin(angle) * speed;
      int life = 12 + RANDOM.nextInt(15);
      particles.add(new Particle(x, y, vx, vy, color, size, life, Particle.Type.SPARK));
    }
    // Also shockwave
    particles.add(new Particle(x, y, 0, 0, color, size * 2.5f, 16, Particle.Type.SHOCKWAVE));
  }

  // Bullet hits enemy
  public static int checkBulletEnemy(
      List<Bullet> bullets,
      List<Enemy> enemies) {
    return checkBulletEnemy(bullets, enemies, null);
  }

  public static int checkBulletEnemy(
      List<Bullet> bullets,
      List<Enemy> enemies,
      List<Particle> particles) {

    int enemiesDestroyed = 0;
    Iterator<Bullet> bulletIterator = bullets.iterator();

    while (bulletIterator.hasNext()) {
      Bullet bullet = bulletIterator.next();
      Iterator<Enemy> enemyIterator = enemies.iterator();

      while (enemyIterator.hasNext()) {
        Enemy enemy = enemyIterator.next();

        if (bullet.getBounds().intersects(enemy.getBounds())) {
          bulletIterator.remove();
          enemyIterator.remove();
          enemiesDestroyed++;

          spawnParticles(particles, enemy.getX() + 20, enemy.getY() + 17, new Color(255, 90, 30), 18, 5f);
          SoundManager.getInstance().playExplosion();
          break;
        }
      }
    }

    return enemiesDestroyed;
  }

  // Enemy touches player
  public static boolean checkPlayerEnemy(
      Player player,
      List<Enemy> enemies) {
    return checkPlayerEnemy(player, enemies, null);
  }

  public static boolean checkPlayerEnemy(
      Player player,
      List<Enemy> enemies,
      List<Particle> particles) {

    Iterator<Enemy> enemyIterator = enemies.iterator();

    while (enemyIterator.hasNext()) {
      Enemy enemy = enemyIterator.next();

      if (player.getBounds().intersects(enemy.getBounds())) {
        enemyIterator.remove();
        player.takeDamage(20);

        spawnParticles(particles, enemy.getX() + 20, enemy.getY() + 17, new Color(255, 60, 60), 20, 6f);
        SoundManager.getInstance().playPlayerDamage();
        return true;
      }
    }

    return false;
  }

  // Check bullet-asteroid collisions
  public static int checkBulletAsteroid(
      List<Bullet> bullets,
      List<Asteroid> asteroids) {
    return checkBulletAsteroid(bullets, asteroids, null);
  }

  public static int checkBulletAsteroid(
      List<Bullet> bullets,
      List<Asteroid> asteroids,
      List<Particle> particles) {

    int destroyed = 0;
    Iterator<Bullet> bulletIterator = bullets.iterator();

    while (bulletIterator.hasNext()) {
      Bullet bullet = bulletIterator.next();
      Iterator<Asteroid> asteroidIterator = asteroids.iterator();

      while (asteroidIterator.hasNext()) {
        Asteroid asteroid = asteroidIterator.next();

        if (bullet.getBounds().intersects(asteroid.getBounds())) {
          bulletIterator.remove();
          asteroidIterator.remove();
          destroyed++;

          spawnParticles(particles, asteroid.getX() + asteroid.getSize() / 2.0,
              asteroid.getY() + asteroid.getSize() / 2.0, new Color(170, 160, 150), 14, 4f);
          SoundManager.getInstance().playHit();
          break;
        }
      }
    }

    return destroyed;
  }

  // Check player-asteroid collisions
  public static boolean checkPlayerAsteroid(
      Player player,
      List<Asteroid> asteroids) {
    return checkPlayerAsteroid(player, asteroids, null);
  }

  public static boolean checkPlayerAsteroid(
      Player player,
      List<Asteroid> asteroids,
      List<Particle> particles) {

    Iterator<Asteroid> iterator = asteroids.iterator();

    while (iterator.hasNext()) {
      Asteroid asteroid = iterator.next();

      if (player.getBounds().intersects(asteroid.getBounds())) {
        iterator.remove();
        player.takeDamage(20);

        spawnParticles(particles, asteroid.getX() + asteroid.getSize() / 2.0,
            asteroid.getY() + asteroid.getSize() / 2.0, new Color(200, 120, 60), 20, 5f);
        SoundManager.getInstance().playPlayerDamage();
        return true;
      }
    }

    return false;
  }

  // Check whether player collects a power-up
  public static boolean checkPlayerPowerUp(
      Player player,
      List<PowerUp> powerUps) {
    return checkPlayerPowerUp(player, powerUps, null);
  }

  public static boolean checkPlayerPowerUp(
      Player player,
      List<PowerUp> powerUps,
      List<Particle> particles) {

    Iterator<PowerUp> iterator = powerUps.iterator();

    while (iterator.hasNext()) {
      PowerUp powerUp = iterator.next();

      if (player.getBounds().intersects(powerUp.getBounds())) {
        iterator.remove();
        player.upgradeWeapon();

        spawnParticles(particles, powerUp.getX() + 12, powerUp.getY() + 12, new Color(0, 255, 180), 22, 6f);
        SoundManager.getInstance().playPowerUp();
        return true;
      }
    }

    return false;
  }

  // Check bullet-boss collisions
  public static int checkBulletBoss(
      List<Bullet> bullets,
      Boss boss) {
    return checkBulletBoss(bullets, boss, null);
  }

  public static int checkBulletBoss(
      List<Bullet> bullets,
      Boss boss,
      List<Particle> particles) {

    if (boss == null) {
      return 0;
    }

    int hits = 0;
    Iterator<Bullet> iterator = bullets.iterator();

    while (iterator.hasNext()) {
      Bullet bullet = iterator.next();

      if (bullet.getBounds().intersects(boss.getBounds())) {
        iterator.remove();
        boss.takeDamage(1);
        hits++;

        spawnParticles(particles, bullet.getX(), bullet.getY(), new Color(255, 0, 180), 6, 3.5f);
        SoundManager.getInstance().playHit();
      }
    }

    return hits;
  }

  // Check boss bullet-player collisions
  public static void checkBossBulletPlayer(
      Player player,
      List<BossBullet> bullets) {
    checkBossBulletPlayer(player, bullets, null);
  }

  public static void checkBossBulletPlayer(
      Player player,
      List<BossBullet> bullets,
      List<Particle> particles) {

    for (BossBullet bullet : bullets) {
      if (bullet.getBounds().intersects(player.getBounds())) {
        player.takeDamage(10);
        bullets.remove(bullet);

        spawnParticles(particles, player.getX() + 20, player.getY() + 20, new Color(255, 50, 200), 16, 5f);
        SoundManager.getInstance().playPlayerDamage();
        break;
      }
    }
  }
}
