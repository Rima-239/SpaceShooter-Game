package com.spaceshooter;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.entities.Asteroid;
import com.spaceshooter.entities.Boss;
import com.spaceshooter.entities.BossBullet;
import com.spaceshooter.entities.Bullet;
import com.spaceshooter.entities.Enemy;
import com.spaceshooter.entities.Player;
import com.spaceshooter.entities.PowerUp;
import com.spaceshooter.managers.CollisionManager;
import com.spaceshooter.managers.GameManager;
import com.spaceshooter.managers.LevelManager;
import com.spaceshooter.managers.ScoreManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated test suite that rigorously validates all core mechanics,
 * local storage integrity, collision detection, weapon tiers, levels,
 * boss encounters, pause state preservation, audio volume controls,
 * and error handling without requiring MySQL.
 */
public class ComprehensiveSystemTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    private static void assertEquals(String testName, Object expected, Object actual) {
        testsRun++;
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName + " - Expected: <" + expected + ">, Got: <" + actual + ">");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName + " - Condition was false!");
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  SPACESHOOTER COMPREHENSIVE AUTOMATED TEST SUITE");
        System.out.println("==================================================");

        testScoreManagerAndLocalStorage();
        testPlayerMechanicsAndWeapons();
        testEnemyAndAsteroidMechanics();
        testBossMechanics();
        testCollisionManager();
        testLevelManagerProgression();
        testGameManagerLoop();
        testPauseAndResumePreservation();
        testSoundManagerAndVolumeControl();

        System.out.println("\n--------------------------------------------------");
        System.out.println("  TEST RESULTS: " + testsPassed + " / " + testsRun + " passed.");
        System.out.println("--------------------------------------------------");

        if (testsPassed == testsRun) {
            System.out.println(">>> ALL TESTS PASSED SUCCESSFULLY! <<<");
            System.exit(0);
        } else {
            System.err.println(">>> SOME TESTS FAILED! <<<");
            System.exit(1);
        }
    }

    private static void testScoreManagerAndLocalStorage() {
        System.out.println("\n--- 1. Testing Local Score Storage & Validation ---");
        ScoreManager sm = new ScoreManager();

        // 1. Username Validation across various lengths
        assertEquals("Empty username rejected", "Player name cannot be empty!", ScoreManager.validatePlayerName(""));
        assertEquals("Whitespace-only rejected", "Player name cannot be empty!", ScoreManager.validatePlayerName("   "));
        assertEquals("Single character accepted", null, ScoreManager.validatePlayerName("A"));
        assertEquals("Normal callsign accepted", null, ScoreManager.validatePlayerName("Maverick"));
        assertEquals("Exactly 25 characters accepted", null, ScoreManager.validatePlayerName("1234567890123456789012345"));
        assertEquals("26 characters rejected", "Player name must be 25 characters or fewer!",
                ScoreManager.validatePlayerName("12345678901234567890123456"));

        // 2. Sanitization
        assertEquals("Sanitize null to Pilot", "Pilot", ScoreManager.sanitizePlayerName(null));
        assertEquals("Sanitize empty to Pilot", "Pilot", ScoreManager.sanitizePlayerName(""));
        assertEquals("Truncate excessively long", 25, ScoreManager.sanitizePlayerName("123456789012345678901234567890").length());

        // 3. Save & Retrieve Scores
        try {
            sm.saveScore("TesterAce", 350, 2);
            sm.saveScore("TesterCommander", 920, 3);
            sm.saveScore("TesterNovice", 110, 1);

            List<ScoreManager.ScoreRecord> topScores = sm.getTopScores();
            assertTrue("Top scores not empty", !topScores.isEmpty());
            assertTrue("Top scores sorted highest to lowest", topScores.get(0).score >= topScores.get(topScores.size() - 1).score);
            assertTrue("Top score is Commander", topScores.get(0).playerName.equals("TesterCommander") || topScores.get(0).score >= 920);

            // Personal best
            int pb = sm.getPlayerHighScore("TesterAce");
            assertTrue("Personal best retrieved correctly", pb >= 350);

            // Last player name
            assertEquals("Last player name retrieved", "TesterNovice", sm.getLastPlayerName());

            // Corrupted line handling
            Path scoresPath = Path.of("data", "scores.csv");
            Files.writeString(scoresPath, "CORRUPTED_LINE_WITHOUT_COMMAS\n\"Broken\",not_a_number,abc\n", StandardOpenOption.APPEND);
            List<ScoreManager.ScoreRecord> safeScores = sm.getTopScores();
            assertTrue("Corrupted lines skipped safely without crashing", safeScores != null && !safeScores.isEmpty());

        } catch (IOException e) {
            assertTrue("Score save/load failed with exception: " + e.getMessage(), false);
        }
    }

    private static void testPlayerMechanicsAndWeapons() {
        System.out.println("\n--- 2. Testing Player Movement, Damage, and Weapons ---");
        Player player = new Player(400, 500);

        // Movement
        player.setMovingUp(true);
        player.update();
        assertEquals("Player moves up by speed 5", 495, player.getY());
        player.setMovingUp(false);

        player.setMovingRight(true);
        player.update();
        assertEquals("Player moves right by speed 5", 405, player.getX());
        player.setMovingRight(false);

        // Screen clamping
        player.keepInsideScreen(800, 600);
        assertTrue("Player inside screen", player.getX() >= 0 && player.getY() >= 0);

        // Weapon upgrades
        assertEquals("Initial weapon level 1", 1, player.getWeaponLevel());
        List<Bullet> bulletsLv1 = player.shootBullets();
        assertEquals("Level 1 shoots 1 bullet", 1, bulletsLv1.size());

        player.upgradeWeapon();
        assertEquals("Upgraded weapon level 2", 2, player.getWeaponLevel());
        List<Bullet> bulletsLv2 = player.shootBullets();
        assertEquals("Level 2 shoots 2 bullets", 2, bulletsLv2.size());

        player.upgradeWeapon();
        assertEquals("Upgraded weapon level 3", 3, player.getWeaponLevel());
        List<Bullet> bulletsLv3 = player.shootBullets();
        assertEquals("Level 3 shoots 3 bullets", 3, bulletsLv3.size());

        player.upgradeWeapon();
        assertEquals("Weapon level caps at 3", 3, player.getWeaponLevel());

        // Health & Damage
        assertEquals("Initial health is 100", 100, player.getHealth());
        player.takeDamage(20);
        assertEquals("Health drops to 80 after 20 damage", 80, player.getHealth());

        player.setInvulnerable(true);
        player.takeDamage(50);
        assertEquals("Damage ignored while invulnerable", 80, player.getHealth());
        player.setInvulnerable(false);

        // Lives & Respawn
        assertEquals("Initial lives 3", 3, player.getLives());
        player.loseLife();
        assertEquals("Lives decrement to 2", 2, player.getLives());
        player.respawn(800, 600);
        assertEquals("Health restored to 100 on respawn", 100, player.getHealth());
    }

    private static void testEnemyAndAsteroidMechanics() {
        System.out.println("\n--- 3. Testing Enemy & Asteroid Spawning & Movement ---");
        Enemy enemy = new Enemy(100, 50, 4);
        enemy.update();
        assertEquals("Enemy moves down by speed", 54, enemy.getY());
        assertTrue("Enemy not off screen", !enemy.isOffScreen(600));
        assertTrue("Enemy off screen when past panelHeight", enemy.isOffScreen(40));

        Asteroid asteroid = new Asteroid(200, 10, 30, 3);
        asteroid.update();
        assertEquals("Asteroid moves down by speed", 13, asteroid.getY());
        assertTrue("Asteroid off screen check", asteroid.isOffScreen(10));
    }

    private static void testBossMechanics() {
        System.out.println("\n--- 4. Testing Boss Spawning, Movement, and Attacks ---");
        Boss boss = new Boss(800, 1.0);
        assertEquals("Boss initial max health is 30", 30, boss.getMaxHealth());
        assertEquals("Boss current health equals max", 30, boss.getHealth());

        // Movement
        int initialX = boss.getX();
        boss.update(800, 600);
        assertTrue("Boss moves horizontally", boss.getX() != initialX);

        // Damage & destruction
        boss.takeDamage(10);
        assertEquals("Boss health reduced after damage", 20, boss.getHealth());
        assertTrue("Boss not destroyed yet", !boss.isDestroyed());

        boss.takeDamage(20);
        assertEquals("Boss health is 0", 0, boss.getHealth());
        assertTrue("Boss destroyed when health <= 0", boss.isDestroyed());
    }

    private static void testCollisionManager() {
        System.out.println("\n--- 5. Testing Collision Detection ---");
        // Bullet vs Enemy
        List<Bullet> bullets = new ArrayList<>();
        bullets.add(new Bullet(100, 100));
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(95, 95, 2));

        int destroyed = CollisionManager.checkBulletEnemy(bullets, enemies);
        assertEquals("Bullet hits enemy and destroys it", 1, destroyed);
        assertEquals("Bullet consumed on hit", 0, bullets.size());
        assertEquals("Enemy removed on hit", 0, enemies.size());

        // Bullet vs Asteroid
        bullets.add(new Bullet(200, 200));
        List<Asteroid> asteroids = new ArrayList<>();
        asteroids.add(new Asteroid(190, 190, 30, 2));

        int astDestroyed = CollisionManager.checkBulletAsteroid(bullets, asteroids);
        assertEquals("Bullet destroys asteroid", 1, astDestroyed);
        assertEquals("Bullet consumed on asteroid hit", 0, bullets.size());
        assertEquals("Asteroid removed on hit", 0, asteroids.size());

        // Player vs PowerUp
        Player p = new Player(300, 300);
        List<PowerUp> powerUps = new ArrayList<>();
        powerUps.add(new PowerUp(305, 305));
        boolean collected = CollisionManager.checkPlayerPowerUp(p, powerUps);
        assertTrue("Player collects power-up", collected);
        assertEquals("Power-up upgrades weapon to level 2", 2, p.getWeaponLevel());
        assertEquals("Power-up removed from list", 0, powerUps.size());

        // Boss Bullet vs Player
        List<BossBullet> bossBullets = new ArrayList<>();
        bossBullets.add(new BossBullet(310, 310));
        int hpBefore = p.getHealth();
        CollisionManager.checkBossBulletPlayer(p, bossBullets);
        assertEquals("Player takes 10 damage from boss bullet", hpBefore - 10, p.getHealth());
        assertEquals("Boss bullet consumed on hit", 0, bossBullets.size());
    }

    private static void testLevelManagerProgression() {
        System.out.println("\n--- 6. Testing Level Manager & Waves ---");
        LevelManager lm = new LevelManager();
        assertEquals("Starts at Level 1", 1, lm.getCurrentLevel());
        assertEquals("Level 1 requires 10 enemies", 10, lm.getEnemiesRequired());

        lm.registerEnemyDefeats(5);
        assertEquals("5 enemies defeated", 5, lm.getEnemiesDefeated());
        assertTrue("Boss not ready yet", !lm.isBossReady());

        lm.registerEnemyDefeats(5);
        assertEquals("10 enemies defeated", 10, lm.getEnemiesDefeated());
        assertTrue("Boss ready when required enemies reached", lm.isBossReady());

        lm.startBossFight();
        assertEquals("State is BOSS_FIGHT", LevelManager.LevelState.BOSS_FIGHT, lm.getState());

        lm.bossDefeated();
        assertEquals("State is LEVEL_COMPLETE", LevelManager.LevelState.LEVEL_COMPLETE, lm.getState());

        lm.nextLevel();
        assertEquals("Advances to Level 2", 2, lm.getCurrentLevel());
        assertEquals("Enemies defeated reset for level 2", 0, lm.getEnemiesDefeated());
    }

    private static void testGameManagerLoop() {
        System.out.println("\n--- 7. Testing Game Manager Loop & Screen Shake ---");
        GameManager gm = new GameManager(1024, 768);
        assertTrue("Game not over initially", !gm.isGameOver());
        assertEquals("Score starts at 0", 0, gm.getScore());

        // Screen shake
        gm.triggerScreenShake(15, 8.0);
        gm.update(1024, 768);
        assertTrue("Stars generated", gm.getStars().size() > 50);

        // Restart
        gm.restart(1024, 768);
        assertEquals("Score reset on restart", 0, gm.getScore());
        assertEquals("Level reset to 1", 1, gm.getLevelReached());
    }

    private static void testPauseAndResumePreservation() {
        System.out.println("\n--- 8. Testing Pause / Resume Game State Preservation ---");
        GamePanel gp = new GamePanel("PilotAce");

        // Set initial custom values
        Player player = gp.getGameManager().getPlayer();
        player.takeDamage(30); // 70 HP
        int initialHp = player.getHealth();
        int initialLives = player.getLives();
        int initialScore = gp.getGameManager().getScore();
        int initialX = player.getX();
        int initialY = player.getY();

        assertEquals("Initial state is PLAYING", GameState.PLAYING, gp.getGameState());

        // Pause game
        gp.pauseGame();
        assertEquals("State after pause is PAUSED", GameState.PAUSED, gp.getGameState());

        // Resume game
        gp.resumeGame();
        assertEquals("State after resume is PLAYING", GameState.PLAYING, gp.getGameState());

        // Verify all state variables are 100% preserved
        assertEquals("Player X preserved after pause/resume", initialX, player.getX());
        assertEquals("Player Y preserved after pause/resume", initialY, player.getY());
        assertEquals("Player health preserved after pause/resume", initialHp, player.getHealth());
        assertEquals("Player lives preserved after pause/resume", initialLives, player.getLives());
        assertEquals("Player score preserved after pause/resume", initialScore, gp.getGameManager().getScore());

        // Toggle pause
        gp.togglePause();
        assertEquals("Toggle pause gives PAUSED", GameState.PAUSED, gp.getGameState());
        gp.togglePause();
        assertEquals("Toggle pause gives PLAYING", GameState.PLAYING, gp.getGameState());
    }

    private static void testSoundManagerAndVolumeControl() {
        System.out.println("\n--- 9. Testing Audio Volume Controls & Synthesis ---");
        SoundManager sm = SoundManager.getInstance();
        assertTrue("SoundManager singleton exists", sm != null);

        // Master Volume
        sm.setMasterVolume(0.50f);
        assertTrue("Master volume set to 0.50", Math.abs(sm.getMasterVolume() - 0.50f) < 0.01f);
        sm.setMasterVolume(1.50f);
        assertTrue("Master volume clamped at 1.0", sm.getMasterVolume() <= 1.0f);
        sm.setMasterVolume(-0.50f);
        assertTrue("Master volume clamped at 0.0", sm.getMasterVolume() >= 0.0f);
        sm.setMasterVolume(0.80f);

        // SFX and Music Volume
        sm.setSfxVolume(0.70f);
        assertTrue("SFX volume set to 0.70", Math.abs(sm.getSfxVolume() - 0.70f) < 0.01f);
        sm.setMusicVolume(0.40f);
        assertTrue("Music volume set to 0.40", Math.abs(sm.getMusicVolume() - 0.40f) < 0.01f);

        // Verify mute toggle
        boolean initialMute = sm.isMuted();
        sm.toggleMute();
        assertEquals("Mute toggled", !initialMute, sm.isMuted());
        sm.toggleMute();
        assertEquals("Mute restored", initialMute, sm.isMuted());

        // Verify triggering sounds throws no exceptions even on headless or non-audio systems
        try {
            sm.playShoot();
            sm.playHit();
            sm.playExplosion();
            sm.playPowerUp();
            sm.playPlayerDamage();
            sm.playBossWarning();
            sm.playLevelComplete();
            sm.playGameOver();
            sm.playMenuClick();
            assertTrue("All procedural sounds triggered safely without exception", true);
        } catch (Throwable t) {
            assertTrue("Audio triggered exception: " + t.getMessage(), false);
        }
    }
}
