package com.spaceshooter;

import com.spaceshooter.ui.LeaderboardWindow;

import javax.swing.SwingUtilities;

public class GuiIntegrationTest {

    public static void main(String[] args) {
        System.out.println("Starting GUI Integration Test...");

        SwingUtilities.invokeLater(() -> {
            try {
                GameWindow window = new GameWindow();
                System.out.println("  [PASS] GameWindow instantiated.");

                window.showLoginScreen();
                System.out.println("  [PASS] Switched to LoginScreen.");

                window.showInstructions();
                System.out.println("  [PASS] Switched to Instructions.");

                window.showMainMenu();
                System.out.println("  [PASS] Switched to MainMenu.");

                window.startGame("TestPilotAce");
                System.out.println("  [PASS] Started Game with Pilot Ace.");

                LeaderboardWindow.showLeaderboard(window);
                System.out.println("  [PASS] Opened LeaderboardWindow.");

                window.toggleFullscreen();
                System.out.println("  [PASS] Fullscreen toggled ON.");

                window.toggleFullscreen();
                System.out.println("  [PASS] Fullscreen toggled OFF (restored).");

                System.out.println(">>> GUI INTEGRATION TEST PASSED SUCCESSFULLY! <<<");
                window.dispose();
                System.exit(0);

            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        });
    }
}

