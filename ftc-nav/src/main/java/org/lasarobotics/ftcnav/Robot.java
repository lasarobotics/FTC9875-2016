package org.lasarobotics.ftcnav;

public class Robot {
    public Coord location;
    public double rotation;

    public Robot() {
        this(new Coord(), 0);
    }
    public Robot(Coord location, double rotation) {
        this.location = location;
        this.rotation = rotation;
    }

    public Coord locationDelta(Coord delta) {
        location.add(delta);
        return location;
    }
    public double rotationDelta(double delta) {
        rotation += delta;
        return delta;
    }
}
