# 🚀 SpaceShooter – Arcade Edition

![Java Version](https://img.shields.io/badge/Java-21%2B-blue?logo=openjdk&logoColor=white)
![Framework](https://img.shields.io/badge/GUI-Java%20Swing%20%2F%20Java2D-orange)
![Database](https://img.shields.io/badge/Database-Free%20(Local%20Storage)-green)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey)
![License](https://img.shields.io/badge/License-MIT-blue)

A high-performance, standalone 2D arcade space shooter desktop game built purely with **Java Swing** and **Java2D**. Features high-fidelity vector graphics, a multi-layered parallax starfield, 16-bit procedural audio synthesis, persistent local leaderboard rankings, responsive scaling, and a complete window and fullscreen management system.

**100% standalone and database-free** — runs natively with zero external JARs, zero database engines (no MySQL, no XAMPP), and zero build tools required (no Maven or Gradle).

---

## 📑 Table of Contents

- [Game Overview](#-game-overview)
- [Key Features](#-key-features)
- [Technologies Used](#-technologies-used)
- [System Requirements](#-system-requirements)
- [Screenshots & UI Previews](#-screenshots--ui-previews)
- [Keyboard Controls](#-keyboard-controls)
- [Project Architecture](#-project-architecture)
- [Installation & Quick Start](#-installation--quick-start)
  - [Compiling via PowerShell](#1-compile-powershell)
  - [Launching the Game](#2-launch-the-game)
  - [Running Automated Verification Tests](#3-run-automated-system-tests)
- [Local Leaderboard & Data Storage](#-local-leaderboard--data-storage)
- [Audio & Procedural Sound System](#-audio--procedural-sound-system)
- [Developer & Contributing Guide](#-developer--contributing-guide)
- [License](#-license)

---

## 🌌 Game Overview

In **SpaceShooter**, you take command of an advanced galactic starfighter deployed to defend civilization against alien interceptor swarms, dense asteroid fields, and heavily armored dreadnought flagships.

Battle across 3 sector waves of escalating difficulty, collect holographic energy capsules to upgrade your ship's plasma cannon systems from single bolts to triple spread-cannons, defeat sector bosses, and earn your place among the **Top 10 Galactic Aces** on the local persistent leaderboard.

---

## ✨ Key Features

- **100% Database-Free Architecture**: Operates with zero MySQL, JDBC, or database connection overhead. Everything runs on standard Java runtime libraries.
- **High-Visibility Callsign Registration**: High-contrast pilot login screen featuring glowing neon text, real-time input validation (1–25 characters), automatic trimming, and pilot personal-best score retrieval.
- **Persistent Local Leaderboard**:
  - Automatically provisions `data/scores.csv` on first launch.
  - Ranks the all-time Top 10 pilots by final score with secondary sorting by timestamp.
  - Visual ranking highlights: 🥇 **Gold (Rank 1)**, 🥈 **Silver (Rank 2)**, 🥉 **Bronze (Rank 3)**.
  - Fault-tolerant parser: gracefully recovers from missing, corrupted, or malformed data files.
- **Robust In-Game Pause System**:
  - Pause anytime during flight with `ESC` or `P`.
  - Freezes all game updates: enemies, asteroids, plasma bolts, and timers stop immediately.
  - Zero state loss: ship position, hull integrity, lives, score, and weapon tiers are preserved upon resuming.
  - Safe exit protection: quit confirmation modal prevents accidental mission abandonment.
- **Seamless Window & Fullscreen Engine**:
  - Opens maximized by default.
  - Supports standard OS minimize, maximize, and restore operations.
  - Borderless exclusive fullscreen toggle with `F11`.
  - Priority-based `ESC` handling: exits fullscreen if active, opens Pause Menu if windowed.
  - Dynamic coordinate scaling: game canvas, starfield, and HUD adapt to 800×600, 1080p, 1440p, or 4K.
- **Java2D Vector Art & Visual FX**:
  - Multi-tier parallax starfield with distant, mid-range, and twinkling stars.
  - Sleek vector starfighter with animated dual ion thruster plumes.
  - Shimmering translucent energy shield bubble for post-respawn invulnerability.
  - Jagged procedural space asteroids with 3D spherical shading and tumbling rotation.
  - Predator alien attack drones with flashing sensor slits and hit feedback.
  - Multi-turret Dreadnought Boss flagship with pulsing dark matter reactor core and segmented health bar.
  - Particle physics system: sparks, smoke embers, expanding shockwave rings, and camera screen shake.
- **16-Bit Procedural Audio Synthesizer**:
  - Smooth sine-glide lasers (eliminates harsh square-wave buzz).
  - Cinematic low-passed explosion rumbles with 50 Hz sub-bass drop (zero high-frequency hiss).
  - Ambient procedural space synth chord progression looping continuously.
  - Independent Master, SFX, and Music volume controls with Pause Menu adjustments.
  - Concurrency limiting to prevent sound saturation during intense combat.

---

## 🛠 Technologies Used

| Technology | Purpose |
| :--- | :--- |
| **Java 21+ (OpenJDK / Temurin)** | Core programming language and runtime environment |
| **Java Swing (`javax.swing.*`)** | UI components, card layout navigation, and custom modal dialogs |
| **Java2D (`java.awt.*`, `Graphics2D`)** | Vector rendering, double-buffered canvas, particle physics, anti-aliasing |
| **Java Sound API (`javax.sound.sampled`)** | Real-time procedural 16-bit sound synthesis, volume DSP, and clip playback |
| **Java NIO (`java.nio.file.*`)** | Safe atomic file I/O, directory creation, and CSV persistence |

---

## 📋 System Requirements

- **Operating System**: Windows 10/11, macOS 11+, or modern Linux distribution
- **Java Runtime**: JDK or JRE 21 or higher
- **Memory**: Minimum 256 MB RAM (512 MB recommended)
- **Display**: Minimum resolution 800×600 (scales up to 4K Ultra HD)
- **Audio**: Any standard stereo sound device (game handles headless systems without crashing)

---

## 📸 Screenshots & UI Previews

```
+----------------------------------------------------------------------------+
|  [🚀 MAVERICK]           000450 PTS          SHIELD: 100% [==========]     |
|  SECTOR 1 • 8/10                             LIVES: 🚀 🚀 🚀  WEAPON: [■■□]|
|                                                                            |
|                      Press ESC to Pause • [P] Quick Pause                  |
|                                                                            |
|          *                   .                  *               .          |
|                     ▲ (Enemy Drone)                                        |
|                     |                                                      |
|                     v                                                      |
|                                  ● (Asteroid)                              |
|                                                                            |
|                                   | (Plasma Bolt)                          |
|                                   ^                                        |
|                              /▲\                                           |
|                             /_|_ \ (Player Starfighter)                    |
|                              | | (Ion Thrusters)                           |
+----------------------------------------------------------------------------+
```

### Screen Highlights:
1. **Pilot Registration**: Glassmorphic modal with glowing neon cyan border, placeholder feedback, and personal best tracking.
2. **Main Menu**: Deep space backdrop with dynamic animated starfield and live galactic high-score ticker.
3. **Gameplay HUD**: Floating translucent glass cards for shields, lives, sector waves, and weapon tier pips.
4. **Pause Overlay**: Clean semi-transparent card with `RESUME`, `MAIN MENU`, `EXIT GAME`, and real-time audio volume sliders.
5. **Leaderboard Window**: Top 10 rankings table with gold, silver, and bronze visual badges for the top 3 pilots.

---

## 🎮 Keyboard Controls

| Key Binding | Action |
| :--- | :--- |
| `↑` `↓` `←` `→` / `W` `A` `S` `D` | Maneuver spaceship through space |
| `SPACEBAR` | Fire plasma cannons (hold for continuous burst) |
| `ESC` | Open / Close Pause Menu (or exit Fullscreen if active) |
| `P` | Dedicated Pause / Resume toggle |
| `F11` | Toggle Fullscreen mode |
| `ESC` *(while Fullscreen)* | Smoothly restore windowed mode |
| `M` | Mute / Unmute all sound effects and music |
| `L` | Open Galactic Top 10 Leaderboard |
| `ENTER` | Confirm callsign on Login / Restart on Game Over |

---

## 📂 Project Architecture

```
SpaceShooter/
├── .gitignore                         # Excludes bytecode, temporary logs, and local player data
├── README.md                          # Repository documentation
├── sources.txt                        # Clean cross-platform relative source listing
├── data/
│   └── .gitkeep                       # Preserves directory structure while ignoring player CSV data
├── src/
│   └── com/spaceshooter/
│       ├── Main.java                  # Application entry point (invokes GameWindow on Swing EDT)
│       ├── GameWindow.java            # Top-level JFrame, CardLayout navigation, fullscreen engine
│       ├── GamePanel.java             # 60 FPS loop, HUD, state machine, particle rendering
│       ├── GameState.java             # Enum: MENU, PLAYING, PAUSED, GAME_OVER
│       ├── InputHandler.java          # Key listener handling movement, firing, and hotkeys
│       ├── ComprehensiveSystemTest.java # 88 automated unit and integration tests
│       ├── GuiIntegrationTest.java    # Swing UI and fullscreen verification harness
│       ├── audio/
│       │   └── SoundManager.java      # 16-bit procedural synthesis, BGM loop, volume control
│       ├── entities/
│       │   ├── Player.java            # Spaceship, ion thrusters, shield bubble, weapon tiers
│       │   ├── Bullet.java            # Glowing neon plasma bolts
│       │   ├── Enemy.java             # Alien interceptor drones with damage flash
│       │   ├── Asteroid.java          # Procedural 3D shaded rock with rotation
│       │   ├── Boss.java              # Armored Dreadnought flagship
│       │   ├── BossBullet.java        # Boss energy plasma blasts
│       │   ├── PowerUp.java           # Holographic weapon upgrade capsule
│       │   ├── Star.java              # Parallax starfield with twinkling phases
│       │   └── Particle.java          # Particle system (sparks, shockwaves, smoke)
│       ├── managers/
│       │   ├── GameManager.java       # Coordinates game loop, entities, score, screen shake
│       │   ├── CollisionManager.java  # Collision detection & particle/sound triggering
│       │   ├── EnemyManager.java      # Enemy spawning and flight paths
│       │   ├── AsteroidManager.java   # Asteroid spawning and lifecycle
│       │   ├── BossManager.java       # Boss encounter management
│       │   ├── LevelManager.java      # 3-sector wave progression & difficulty curve
│       │   ├── PowerUpManager.java    # Power-up spawning and pickup handling
│       │   └── ScoreManager.java      # Dedicated local file-based score persistence
│       └── ui/
│           ├── UIUtils.java           # Shared palette, typography, glowing buttons & cards
│           ├── MenuPanel.java         # Main menu with animated stars & high-score banner
│           ├── LoginPanel.java        # High-visibility pilot login screen
│           ├── InstructionsPanel.java # Comprehensive flight briefing & control guide
│           └── LeaderboardWindow.java # Top 10 leaderboard with Gold/Silver/Bronze styling
```

---

## 🚀 Installation & Quick Start

### Prerequisites
Ensure Java JDK 21 or higher is installed and added to your system `PATH`:
```powershell
java -version
javac -version
```

### Clone the Repository
```powershell
git clone https://github.com/<your-username>/SpaceShooter.git
cd SpaceShooter
```

### 1. Compile (PowerShell)
Compile all Java source files into the `out/` directory using the provided cross-platform `sources.txt`:
```powershell
javac -encoding UTF-8 -d "out" "@sources.txt"
```
*Alternatively, you can compile directly using PowerShell:*
```powershell
$files = Get-ChildItem -Path "src" -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d "out" $files
```

### 2. Launch the Game
```powershell
java -cp "out" com.spaceshooter.Main
```

### 3. Run Automated System Tests
Execute the comprehensive 88-point test suite covering score storage, physics, collisions, weapon levels, pause preservation, and audio volume controls:
```powershell
java -cp "out" com.spaceshooter.ComprehensiveSystemTest
```

### 4. Run GUI Integration Test
Verify window creation, card switching, and fullscreen toggle:
```powershell
java -cp "out" com.spaceshooter.GuiIntegrationTest
```

---

## 💾 Local Leaderboard & Data Storage

SpaceShooter utilizes a lightweight, zero-dependency file storage mechanism implemented in [`ScoreManager.java`](src/com/spaceshooter/managers/ScoreManager.java):

- **Data Directory**: `data/scores.csv` is automatically created on first launch if missing.
- **Format**: Standard CSV format storing:
  ```csv
  "PilotCallsign",Score,SectorReached,"YYYY-MM-DD HH:MM:SS"
  ```
- **Privacy Guaranteed**: The `.gitignore` file excludes `data/*.csv` so personal leaderboard records are never committed or exposed on GitHub. The `data/.gitkeep` file ensures the folder structure is preserved when cloned.
- **Corruption Recovery**: If a line in the CSV is corrupted, malformed, or edited improperly, the parser safely ignores the corrupted line and continues loading valid records without crashing.

---

## 🔊 Audio & Procedural Sound System

All sound effects and ambient music are generated procedurally in memory via Java's `javax.sound.sampled` API in [`SoundManager.java`](src/com/spaceshooter/audio/SoundManager.java):

- **Zero External Audio Files**: No external `.wav` or `.mp3` assets required; no missing-file errors or copyright licensing concerns.
- **Dynamic Volume Balancing**:
  - Sound effects (SFX) play at balanced, comfortable decibel levels.
  - Ambient background music sits softly in the mix without drowning out laser bolts or warning alarms.
  - Concurrency Limiter: Restricts simultaneous SFX to 4 active channels, preventing sound stacking.
- **In-Game Adjustment**: Press `ESC` during gameplay to adjust master volume directly with `[ — ]` and `[ + ]` controls, or press `M` to toggle mute.

---

## 👨‍💻 Developer & Contributing Guide

Contributions and improvements are welcome!

1. **Fork the Repository** on GitHub.
2. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/NewFeature
   ```
3. **Write Clean, Documented Java Code**:
   - Maintain the zero-database, zero-external-dependency standard.
   - Run the automated test suite before opening a pull request:
     ```powershell
     java -cp "out" com.spaceshooter.ComprehensiveSystemTest
     ```
4. **Commit Your Changes**:
   ```bash
   git commit -m "Add new feature: <description>"
   ```
5. **Push to Your Branch & Open a Pull Request**.

---

## 📄 License

This project is open-source and available under the [MIT License](https://opensource.org/licenses/MIT). You are free to use, modify, and distribute it for educational and personal projects.
