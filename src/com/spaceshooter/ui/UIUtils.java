package com.spaceshooter.ui;

import com.spaceshooter.audio.SoundManager;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shared aesthetic constants, futuristic arcade color palettes,
 * custom glowing buttons, and glassmorphic card renderers.
 */
public class UIUtils {

    // Theme Color Palette
    public static final Color COLOR_BG_DEEP = new Color(6, 9, 18);
    public static final Color COLOR_BG_PANEL = new Color(14, 20, 38, 220);
    public static final Color COLOR_BG_CARD = new Color(20, 28, 52, 190);

    public static final Color NEON_CYAN = new Color(0, 240, 255);
    public static final Color NEON_BLUE = new Color(0, 130, 255);
    public static final Color NEON_PURPLE = new Color(175, 40, 255);
    public static final Color NEON_PINK = new Color(255, 0, 130);
    public static final Color NEON_GREEN = new Color(0, 255, 140);
    public static final Color NEON_GOLD = new Color(255, 210, 40);
    public static final Color NEON_RED = new Color(255, 45, 75);

    public static final Color TEXT_WHITE = new Color(245, 250, 255);
    public static final Color TEXT_MUTED = new Color(140, 160, 195);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 42);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HUD = new Font("Consolas", Font.BOLD, 15);
    public static final Font FONT_HUD_LARGE = new Font("Consolas", Font.BOLD, 22);

    /**
     * Creates an animated, glowing arcade button with smooth hover transitions.
     */
    public static JButton createGlowingButton(String text, Color baseColor, Color glowColor, int width, int height) {
        JButton button = new JButton(text) {
            private boolean hovered = false;
            private boolean pressed = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                        SoundManager.getInstance().playMenuClick();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Button background gradient
                Color topColor = pressed ? baseColor.darker() : (hovered ? glowColor.darker() : baseColor);
                Color botColor = pressed ? baseColor.darker().darker() : baseColor.darker();

                GradientPaint gp = new GradientPaint(0, 0, topColor, 0, h, botColor);
                g2d.setPaint(gp);
                g2d.fillRoundRect(2, 2, w - 4, h - 4, 16, 16);

                // Glowing border
                if (hovered) {
                    AlphaComposite orig = (AlphaComposite) g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                    g2d.setColor(glowColor);
                    g2d.setStroke(new BasicStroke(4.0f));
                    g2d.drawRoundRect(2, 2, w - 4, h - 4, 16, 16);
                    g2d.setComposite(orig);
                }

                g2d.setColor(hovered ? glowColor : Color.WHITE);
                g2d.setStroke(new BasicStroke(hovered ? 2.0f : 1.2f));
                g2d.drawRoundRect(2, 2, w - 4, h - 4, 16, 16);

                // Text
                g2d.setFont(getFont());
                g2d.setColor(hovered ? Color.WHITE : TEXT_WHITE);
                int strW = g2d.getFontMetrics().stringWidth(getText());
                int strH = g2d.getFontMetrics().getAscent();
                g2d.drawString(getText(), (w - strW) / 2, (h + strH) / 2 - 2);

                g2d.dispose();
            }
        };

        button.setFont(FONT_BUTTON);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        return button;
    }

    /**
     * Renders a glassmorphic card container with rounded corners and glowing border.
     */
    public static void drawGlassCard(Graphics2D g2d, int x, int y, int width, int height, Color borderColor) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark translucent background
        g2d.setColor(COLOR_BG_CARD);
        g2d.fillRoundRect(x, y, width, height, 16, 16);

        // Outer glow
        AlphaComposite orig = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawRoundRect(x, y, width, height, 16, 16);

        // Crisp inner border
        g2d.setComposite(orig);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(x, y, width, height, 16, 16);
    }
}

