package com.spaceshooter;

import com.spaceshooter.audio.SoundManager;
import com.spaceshooter.ui.InstructionsPanel;
import com.spaceshooter.ui.LeaderboardWindow;
import com.spaceshooter.ui.LoginPanel;
import com.spaceshooter.ui.MenuPanel;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application window featuring robust fullscreen management,
 * normal minimize/maximize/restore support, global F11 / ESC / P shortcuts,
 * and seamless card-based screen transitions.
 */
public class GameWindow extends JFrame {

    private final JPanel rootContainer;
    private final CardLayout cardLayout;

    private MenuPanel menuPanel;
    private LoginPanel loginPanel;
    private InstructionsPanel instructionsPanel;
    private GamePanel gamePanel;

    private JPanel currentPanel;

    // Fullscreen state management
    private boolean isFullscreen = false;
    private Rectangle previousBounds;
    private int previousExtendedState = JFrame.MAXIMIZED_BOTH;

    public GameWindow() {
        setTitle("Space Shooter - Arcade Edition");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        setResizable(true);

        // Open maximized by default as required
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Prompt confirmation on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitGame();
            }
        });

        // Setup CardLayout Container
        cardLayout = new CardLayout();
        rootContainer = new JPanel(cardLayout);
        setContentPane(rootContainer);

        initScreens();
        setupGlobalHotkeys();

        showMainMenu();
        setVisible(true);
    }

    private void initScreens() {
        menuPanel = new MenuPanel(
                this::showLoginScreen,
                this::showLeaderboard,
                this::showInstructions,
                this::exitGame
        );

        loginPanel = new LoginPanel(
                this::startGame,
                this::showMainMenu
        );

        instructionsPanel = new InstructionsPanel(
                this::showMainMenu
        );

        rootContainer.add(menuPanel, "MENU");
        rootContainer.add(loginPanel, "LOGIN");
        rootContainer.add(instructionsPanel, "INSTRUCTIONS");
    }

    private void setupGlobalHotkeys() {
        // F11 Fullscreen Toggle
        rootContainer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");
        rootContainer.getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });

        // ESC Key Priority:
        // 1. If fullscreen is active, ESC exits fullscreen.
        // 2. Otherwise, ESC opens / toggles the Pause Menu during gameplay.
        rootContainer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "globalEscape");
        rootContainer.getActionMap().put("globalEscape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFullscreen) {
                    toggleFullscreen();
                } else if (currentPanel instanceof GamePanel) {
                    ((GamePanel) currentPanel).togglePause();
                } else if (currentPanel instanceof LoginPanel || currentPanel instanceof InstructionsPanel) {
                    showMainMenu();
                }
            }
        });

        // P Key: Pause / Resume toggle
        rootContainer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "togglePauseKey");
        rootContainer.getActionMap().put("togglePauseKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPanel instanceof GamePanel) {
                    ((GamePanel) currentPanel).togglePause();
                }
            }
        });

        // M Key: Mute toggle
        rootContainer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "toggleMuteKey");
        rootContainer.getActionMap().put("toggleMuteKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.getInstance().toggleMute();
            }
        });
    }

    public synchronized void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        dispose();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (isFullscreen) {
            previousBounds = getBounds();
            previousExtendedState = getExtendedState();
            setUndecorated(true);

            if (gd.isFullScreenSupported()) {
                try {
                    gd.setFullScreenWindow(this);
                } catch (Exception e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        } else {
            if (gd.getFullScreenWindow() == this) {
                try {
                    gd.setFullScreenWindow(null);
                } catch (Exception ignored) {}
            }
            setUndecorated(false);
            if (previousBounds != null) {
                setBounds(previousBounds);
            }
            setExtendedState(previousExtendedState);
        }

        setVisible(true);
        revalidate();
        repaint();

        // Restore keyboard focus immediately
        SwingUtilities.invokeLater(() -> {
            if (currentPanel != null) {
                currentPanel.requestFocusInWindow();
                if (currentPanel instanceof GamePanel) {
                    ((GamePanel) currentPanel).registerInputHandler();
                }
            }
        });
    }

    public void showMainMenu() {
        switchScreen(menuPanel, "MENU");
        menuPanel.startAnimation();
    }

    public void showLoginScreen() {
        switchScreen(loginPanel, "LOGIN");
        loginPanel.startAnimation();
    }

    public void showInstructions() {
        switchScreen(instructionsPanel, "INSTRUCTIONS");
        instructionsPanel.startAnimation();
    }

    public void showLeaderboard() {
        LeaderboardWindow.showLeaderboard(this);
    }

    public void startGame(String playerName) {
        if (gamePanel != null) {
            rootContainer.remove(gamePanel);
        }

        gamePanel = new GamePanel(playerName, this::showMainMenu);
        rootContainer.add(gamePanel, "GAME");

        switchScreen(gamePanel, "GAME");
        gamePanel.registerInputHandler();
    }

    private void switchScreen(JPanel targetPanel, String cardName) {
        if (currentPanel instanceof MenuPanel && currentPanel != targetPanel) {
            ((MenuPanel) currentPanel).stopAnimation();
        } else if (currentPanel instanceof LoginPanel && currentPanel != targetPanel) {
            ((LoginPanel) currentPanel).stopAnimation();
        } else if (currentPanel instanceof InstructionsPanel && currentPanel != targetPanel) {
            ((InstructionsPanel) currentPanel).stopAnimation();
        }

        cardLayout.show(rootContainer, cardName);
        currentPanel = targetPanel;

        revalidate();
        repaint();

        SwingUtilities.invokeLater(targetPanel::requestFocusInWindow);
    }

    public static boolean promptExitConfirmation(Component parent) {
        int choice = JOptionPane.showOptionDialog(
                parent,
                "Are you sure you want to exit SpaceShooter?",
                "Exit SpaceShooter",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"YES – Exit Game", "NO – Continue Playing"},
                "NO – Continue Playing");

        return choice == 0;
    }

    public void exitGame() {
        if (promptExitConfirmation(this)) {
            System.exit(0);
        }
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }
}
