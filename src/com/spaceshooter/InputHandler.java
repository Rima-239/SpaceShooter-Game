package com.spaceshooter;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.entities.Player;
import com.spaceshooter.ui.LeaderboardWindow;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles keyboard input for flight navigation (Arrow Keys and WASD),
 * firing plasma weapons (Space), pausing/resuming (ESC and P),
 * and audio/leaderboard toggles.
 */
public class InputHandler implements KeyListener {

    private final Player player;
    private final GamePanel gamePanel;

    public InputHandler(Player player, GamePanel gamePanel) {
        this.player = player;
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        GameState state = gamePanel.getGameState();

        // Pause / Resume toggle via P key
        if (key == KeyEvent.VK_P) {
            gamePanel.togglePause();
            return;
        }

        // ESC Key Priority:
        // 1. If fullscreen is active, ESC exits fullscreen.
        // 2. Otherwise, ESC opens or toggles the Pause Menu.
        if (key == KeyEvent.VK_ESCAPE) {
            Window win = SwingUtilities.getWindowAncestor(gamePanel);
            if (win instanceof GameWindow && ((GameWindow) win).isFullscreen()) {
                ((GameWindow) win).toggleFullscreen();
            } else {
                gamePanel.togglePause();
            }
            return;
        }

        // Leaderboard shortcut (L)
        if (key == KeyEvent.VK_L) {
            LeaderboardWindow.showLeaderboard(gamePanel);
            return;
        }

        // Mute / Unmute shortcut (M)
        if (key == KeyEvent.VK_M) {
            SoundManager.getInstance().toggleMute();
            return;
        }

        // Start game from Main Menu
        if (state == GameState.MENU && key == KeyEvent.VK_ENTER) {
            gamePanel.startGame();
            return;
        }

        // Restart after Game Over
        if (state == GameState.GAME_OVER && key == KeyEvent.VK_ENTER) {
            gamePanel.restartGame();
            return;
        }

        // No player controls outside active gameplay
        if (state != GameState.PLAYING) {
            return;
        }

        // Player movement (Arrow Keys + WASD)
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            player.setMovingUp(true);
        }
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            player.setMovingDown(true);
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            player.setMovingLeft(true);
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            player.setMovingRight(true);
        }

        // Shooting (Space)
        if (key == KeyEvent.VK_SPACE) {
            player.setShooting(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            player.setMovingUp(false);
        }
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            player.setMovingDown(false);
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            player.setMovingLeft(false);
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            player.setMovingRight(false);
        }
        if (key == KeyEvent.VK_SPACE) {
            player.setShooting(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not required
    }
}
