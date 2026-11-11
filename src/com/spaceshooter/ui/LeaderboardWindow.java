package com.spaceshooter.ui;

import com.spaceshooter.managers.ScoreManager;
import com.spaceshooter.managers.ScoreManager.ScoreRecord;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.List;

/**
 * High-tech arcade leaderboard dialog with custom styled top-3 ranks,
 * glowing neon highlights, and persistent local storage integration.
 */
public class LeaderboardWindow extends JDialog {

    private final ScoreManager scoreManager = new ScoreManager();
    private final DefaultTableModel tableModel;

    public LeaderboardWindow(Window owner) {
        super(owner, "Space Shooter - Top 10 Galactic Leaderboard", ModalityType.MODELESS);

        setSize(720, 520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UIUtils.COLOR_BG_DEEP);
        setLayout(new BorderLayout(10, 10));

        // Header Card
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(18, 25, 48),
                        0, getHeight(), new Color(10, 14, 28));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(UIUtils.NEON_CYAN);
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2d.dispose();
            }
        };
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));

        JLabel heading = new JLabel("TOP 10 GALACTIC ACES", SwingConstants.CENTER);
        heading.setFont(UIUtils.FONT_TITLE);
        heading.setForeground(UIUtils.NEON_CYAN);
        headerPanel.add(heading, BorderLayout.CENTER);

        JLabel subHeading = new JLabel("Hall of Fame - Top combat records saved locally", SwingConstants.CENTER);
        subHeading.setFont(UIUtils.FONT_BODY);
        subHeading.setForeground(UIUtils.TEXT_MUTED);
        headerPanel.add(subHeading, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Columns
        String[] columns = {"Rank", "Pilot Callsign", "Final Score", "Sector", "Date Recorded"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(UIUtils.FONT_BOLD);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(new Color(12, 17, 32));
        table.setForeground(UIUtils.TEXT_WHITE);
        table.setSelectionBackground(new Color(30, 50, 90));
        table.setSelectionForeground(Color.WHITE);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(UIUtils.FONT_BUTTON);
        header.setBackground(new Color(20, 28, 54));
        header.setForeground(UIUtils.NEON_CYAN);
        header.setPreferredSize(new Dimension(100, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.NEON_BLUE));

        // Custom Cell Renderer with Rank 1 (Gold), Rank 2 (Silver), Rank 3 (Bronze)
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                setFont(column == 0 || column == 2 ? UIUtils.FONT_HUD_LARGE.deriveFont(16f) : UIUtils.FONT_BODY);

                // Alternating row background
                if (!isSelected) {
                    if (row == 0) {
                        c.setBackground(new Color(40, 36, 12, 160)); // Gold tint
                        c.setForeground(UIUtils.NEON_GOLD);
                    } else if (row == 1) {
                        c.setBackground(new Color(25, 38, 54, 160)); // Silver tint
                        c.setForeground(new Color(220, 240, 255));
                    } else if (row == 2) {
                        c.setBackground(new Color(38, 26, 18, 160)); // Bronze tint
                        c.setForeground(new Color(245, 175, 120));
                    } else {
                        c.setBackground(row % 2 == 0 ? new Color(14, 20, 38) : new Color(10, 14, 28));
                        c.setForeground(UIUtils.TEXT_WHITE);
                    }
                }

                if (column == 0 && row < 3) {
                    // Medals
                    String medal = row == 0 ? "\uD83E\uDD47 " : (row == 1 ? "\uD83E\uDD48 " : "\uD83E\uDD49 ");
                    setText(medal + value);
                }

                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(12, 17, 32));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 42, 75), 1));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 16, 10));

        JButton refreshButton = UIUtils.createGlowingButton("REFRESH", new Color(20, 45, 80), UIUtils.NEON_CYAN, 160, 42);
        JButton closeButton = UIUtils.createGlowingButton("CLOSE", new Color(40, 45, 65), UIUtils.NEON_PURPLE, 160, 42);

        refreshButton.addActionListener(e -> loadScores());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadScores();
    }

    private void loadScores() {
        tableModel.setRowCount(0);
        try {
            List<ScoreRecord> scores = scoreManager.getTopScores();

            if (scores.isEmpty()) {
                tableModel.addRow(new Object[]{"-", "No galactic records yet", "-", "-", "-"});
                return;
            }

            int rank = 1;
            for (ScoreRecord record : scores) {
                tableModel.addRow(new Object[]{
                        rank++,
                        record.playerName,
                        record.score + " pts",
                        "Sector " + record.levelReached,
                        record.playedAt
                });
            }
        } catch (Exception e) {
            tableModel.addRow(new Object[]{"!", "Error loading leaderboard", "-", "-", e.getMessage()});
        }
    }

    public static void showLeaderboard(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        LeaderboardWindow window = new LeaderboardWindow(owner);
        window.setVisible(true);
    }
}
