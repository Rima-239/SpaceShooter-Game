# 🚀 SpaceShooter – Arcade Edition

![Java](https://img.shields.io/badge/Java-21%2B-blue?logo=openjdk&logoColor=white)
![GUI](https://img.shields.io/badge/GUI-Java%20Swing%20%2F%20Java2D-orange)
![Storage](https://img.shields.io/badge/Storage-Local%20CSV-green)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey)

A standalone 2D arcade space shooter desktop game built using **Java Swing and Java2D**.

SpaceShooter features animated space environments, enemy waves, asteroid fields, boss battles, weapon upgrades, procedural audio, pause functionality, fullscreen support, and a persistent local leaderboard.

**100% database-free:** No MySQL, XAMPP, JDBC, Maven, or Gradle required.

---

## 📑 Table of Contents

- [Game Overview](#-game-overview)
- [Key Features](#-key-features)
- [Technologies Used](#-technologies-used)
- [System Requirements](#-system-requirements)
- [Screenshots and UI Preview](#-screenshots-and-ui-preview)
- [Keyboard Controls](#-keyboard-controls)
- [Project Architecture](#-project-architecture)
- [Installation and Quick Start](#-installation-and-quick-start)
- [Local Leaderboard and Data Storage](#-local-leaderboard-and-data-storage)
- [Audio System](#-audio-and-procedural-sound-system)
- [Developer and Contributing Guide](#-developer-and-contributing-guide)

---

## 🌌 Game Overview

In **SpaceShooter**, you command an advanced galactic starfighter to defend civilization against alien enemies, dangerous asteroids, and powerful boss battles.

Battle through 3 sector waves of increasing difficulty, collect energy capsules to upgrade your weapons, defeat sector bosses, and compete for a place among the **Top 10 Galactic Aces** on the local leaderboard.

---

## ✨ Key Features

### 🎮 Gameplay

- Three-sector wave progression with increasing difficulty.
- Enemy drones, asteroids, and boss encounters.
- Weapon upgrade system with single and triple-shot weapons.
- Player lives, health, score, and shield management.
- Collision detection and particle effects.
- Power-ups and temporary invulnerability.

### 👨‍🚀 Player Registration

- Custom pilot name entry.
- Real-time input validation.
- Name length validation (1–25 characters).
- Personal best score retrieval.

### 🏆 Local Leaderboard

- Persistent Top 10 leaderboard.
- Automatic creation of `data/scores.csv`.
- Score-based ranking with timestamp sorting.
- Gold, silver, and bronze ranking highlights.
- Handles missing or malformed score records.

### ⏸️ Pause System

- Pause using `ESC` or `P`.
- Preserves player position, score, lives, and weapon upgrades.
- Resume, return to main menu, and exit options.
- Audio volume adjustments through the pause menu.

### 🖥️ Window and Fullscreen Support

- Maximized window by default.
- Standard minimize, maximize, and restore support.
- Fullscreen toggle using `F11`.
- Dynamic game rendering and coordinate scaling.

### 🎨 Graphics and Visual Effects

- Multi-layer parallax starfield.
- Animated spaceship and engine thrusters.
- Enemy and asteroid animations.
- Boss health bar and visual effects.
- Particle effects, sparks, and explosions.
- Screen shake and shield effects.

### 🔊 Procedural Audio

- Laser and explosion sound effects.
- Procedurally generated background music.
- Master, SFX, and music volume controls.
- Mute and unmute functionality.
- Audio concurrency management.

---

## 🛠️ Technologies Used

| Technology     | Purpose                              |
| -------------- | ------------------------------------ |
| Java 21+       | Core programming language            |
| Java Swing     | Desktop GUI and navigation           |
| Java2D         | Graphics rendering and animations    |
| Java Sound API | Sound effects and music              |
| Java NIO       | File handling and local data storage |
| CSV            | Local leaderboard persistence        |

---

## 📋 System Requirements

- **Operating System:** Windows 10/11, macOS, or Linux.
- **Java:** JDK 21 or higher.
- **RAM:** 256 MB minimum (512 MB recommended).
- **Display:** Minimum resolution of 800 × 600.
- **Audio:** Standard audio output device (optional).

---

## 📸 Screenshots and UI Preview

The following is a conceptual preview of the gameplay HUD.

```text
+----------------------------------------------------------+
|  [PILOT]            000450 PTS          SHIELD: 100%      |
|                                                          |
|  SECTOR 1              LIVES: 3          WEAPON: LEVEL 1  |
|                                                          |
|                    *             .                       |
|                                                          |
|                         ▲                                |
|                    Enemy Drone                           |
|                                                          |
|                              ●                           |
|                           Asteroid                        |
|                                                          |
|                         |                                |
|                      Plasma Bolt                          |
|                         ^                                |
|                        /▲\                               |
|                       /___\                              |
|                    Player Ship                            |
|                                                          |
+----------------------------------------------------------+
```

### Screen Highlights

1. **Pilot Registration:** Enter your pilot name before starting the game.
2. **Main Menu:** Navigate through the game's main options.
3. **Gameplay HUD:** View score, health, lives, sector, and weapon level.
4. **Pause Menu:** Resume, return to the main menu, exit, and adjust audio.
5. **Leaderboard:** View the Top 10 pilot rankings.

---

## 🎮 Keyboard Controls

| Key             | Action                                       |
| --------------- | -------------------------------------------- |
| `↑` `↓` `←` `→` | Move spaceship                               |
| `W` `A` `S` `D` | Alternative movement controls                |
| `SPACEBAR`      | Fire weapons                                 |
| `ESC`           | Pause game / exit fullscreen                 |
| `P`             | Pause or resume                              |
| `F11`           | Toggle fullscreen                            |
| `M`             | Mute / unmute audio                          |
| `L`             | Open leaderboard                             |
| `ENTER`         | Confirm pilot name / restart after Game Over |

---

## 📂 Project Architecture

```text
SpaceShooter/
│
├── .gitignore
├── README.md
├── sources.txt
│
├── data/
│   └── .gitkeep
│
└── src/
    └── com/spaceshooter/
        │
        ├── Main.java
        ├── GameWindow.java
        ├── GamePanel.java
        ├── GameState.java
        ├── InputHandler.java
        ├── ComprehensiveSystemTest.java
        ├── GuiIntegrationTest.java
        │
        ├── audio/
        │   └── SoundManager.java
        │
        ├── entities/
        │   ├── Player.java
        │   ├── Bullet.java
        │   ├── Enemy.java
        │   ├── Asteroid.java
        │   ├── Boss.java
        │   ├── BossBullet.java
        │   ├── PowerUp.java
        │   ├── Star.java
        │   └── Particle.java
        │
        ├── managers/
        │   ├── GameManager.java
        │   ├── CollisionManager.java
        │   ├── EnemyManager.java
        │   ├── AsteroidManager.java
        │   ├── BossManager.java
        │   ├── LevelManager.java
        │   ├── PowerUpManager.java
        │   └── ScoreManager.java
        │
        └── ui/
            ├── UIUtils.java
            ├── MenuPanel.java
            ├── LoginPanel.java
            ├── InstructionsPanel.java
            └── LeaderboardWindow.java
```

---

## 🚀 Installation and Quick Start

### Prerequisites

Install Java JDK 21 or higher and make sure Java is available in your system PATH.

Verify your installation:

```powershell
java -version
javac -version
```

### 1. Clone the Repository

```powershell
git clone https://github.com/Rima-239/SpaceShooter-Game.git

cd SpaceShooter-Game
```

### 2. Compile the Project

Compile all Java source files into the `out` directory using the provided `sources.txt` file:

```powershell
javac -encoding UTF-8 -d out "@sources.txt"
```

Alternatively, compile using PowerShell:

```powershell
$files = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName

javac -encoding UTF-8 -d out $files
```

### 3. Launch the Game

```powershell
java -cp out com.spaceshooter.Main
```

### 4. Run Automated System Tests

```powershell
java -cp out com.spaceshooter.ComprehensiveSystemTest
```

### 5. Run GUI Integration Tests

```powershell
java -cp out com.spaceshooter.GuiIntegrationTest
```

---

## 💾 Local Leaderboard and Data Storage

SpaceShooter uses local CSV file storage instead of a database.

**Storage Location:**

```text
data/scores.csv
```

### Features

- Automatically creates the data directory and score file when required.
- Stores pilot names, scores, sectors, and timestamps.
- Maintains Top 10 leaderboard rankings.
- Handles missing or malformed score records.
- Keeps personal leaderboard data out of the GitHub repository through `.gitignore`.

### CSV Format

```csv
"PilotCallsign",Score,SectorReached,"YYYY-MM-DD HH:MM:SS"
```

The `data/.gitkeep` file preserves the directory structure in Git without uploading personal score records.

---

## 🔊 Audio and Procedural Sound System

The game's audio is generated using Java's built-in `javax.sound.sampled` API.

### Audio Features

- Procedurally generated laser sounds.
- Explosion sound effects.
- Background space music.
- Independent volume adjustments.
- Master volume and mute controls.
- Audio concurrency management.

No external audio files are required.

---

## 👨‍💻 Developer and Contributing Guide

Contributions, suggestions, and improvements are welcome!

### 1. Fork the Repository

Fork the project from GitHub:

https://github.com/Rima-239/SpaceShooter-Game

### 2. Clone Your Fork

```bash
git clone <your-fork-url>
cd SpaceShooter-Game
```

### 3. Create a Feature Branch

```bash
git checkout -b feature/NewFeature
```

### 4. Make Your Changes

Write clean, readable, and properly documented Java code.

### 5. Test Your Changes

```powershell
javac -encoding UTF-8 -d out "@sources.txt"

java -cp out com.spaceshooter.ComprehensiveSystemTest
```

### 6. Commit and Push

```bash
git add .
git commit -m "Add new feature"
git push origin feature/NewFeature
```

### 7. Open a Pull Request

Submit a pull request with a description of your changes.

---

## 👩‍💻 Developer

**Rima Azad Gazi**

Java Developer | Aspiring Full-Stack Developer

GitHub: https://github.com/Rima-239

---

**Thank you for checking out SpaceShooter! 🚀**

If you enjoy the project, consider giving the repository a ⭐ on GitHub.
