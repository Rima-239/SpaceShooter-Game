package com.spaceshooter.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Random;

/**
 * Procedural shaded space asteroid with craters, realistic 3D lighting,
 * and slow tumbling rotation.
 */
public class Asteroid {

    private int x;
    private int y;

    private final int size;
    private final int speed;

    private double rotation = 0;
    private final double rotationSpeed;

    // Generated jagged rock points relative to center
    private final Polygon rockPoly;

    public Asteroid(int x, int y, int size, int speed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;

        Random rand = new Random((long) x * 31 + y);
        this.rotationSpeed = (rand.nextDouble() - 0.5) * 0.05;

        // Generate irregular polygon for realistic rock silhouette
        int numPoints = 10;
        int[] px = new int[numPoints];
        int[] py = new int[numPoints];
        double radius = size / 2.0;

        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double r = radius * (0.8 + rand.nextDouble() * 0.35);
            px[i] = (int) (r * Math.cos(angle));
            py[i] = (int) (r * Math.sin(angle));
        }
        rockPoly = new Polygon(px, py, numPoints);
    }

    // Move asteroid downward and rotate
    public void update() {
        y += speed;
        rotation += rotationSpeed;
    }

    // Draw asteroid with rotation and 3D rock lighting
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform oldTx = g2d.getTransform();
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        g2d.translate(centerX, centerY);
        g2d.rotate(rotation);

        // 3D spherical lighting gradient (light source from top-left)
        GradientPaint rockGrad = new GradientPaint(
                -size / 2.0f, -size / 2.0f, new Color(130, 135, 145),
                size / 2.0f, size / 2.0f, new Color(45, 48, 55));
        g2d.setPaint(rockGrad);
        g2d.fillPolygon(rockPoly);

        // Outer rock contour
        g2d.setColor(new Color(30, 32, 38));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawPolygon(rockPoly);

        // Craters with rim highlight and inner shadow
        drawCrater(g2d, -size / 5, -size / 6, size / 5);
        drawCrater(g2d, size / 6, size / 5, size / 6);
        drawCrater(g2d, size / 5, -size / 4, size / 7);

        g2d.setTransform(oldTx);
    }

    private void drawCrater(Graphics2D g2d, int cx, int cy, int craterSize) {
        // Shadow base
        g2d.setColor(new Color(25, 28, 33));
        g2d.fillOval(cx - craterSize / 2, cy - craterSize / 2, craterSize, craterSize);

        // Rim highlight
        g2d.setColor(new Color(165, 170, 180, 180));
        g2d.drawArc(cx - craterSize / 2, cy - craterSize / 2, craterSize, craterSize, 45, 180);
    }

    // Collision area
    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    // Check whether asteroid has left the screen
    public boolean isOffScreen(int panelHeight) {
        return y > panelHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }
}
