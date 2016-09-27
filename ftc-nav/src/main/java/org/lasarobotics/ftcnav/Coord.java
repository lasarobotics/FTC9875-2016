package org.lasarobotics.ftcnav;

public class Coord {
    public double x, y;

    public Coord() {
        this(0, 0);
    }
    public Coord(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Coord add(Coord delta) {
        x += delta.x;
        y += delta.y;
        return this;
    }
}
