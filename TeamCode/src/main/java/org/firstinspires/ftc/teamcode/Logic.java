package org.firstinspires.ftc.teamcode;

import java.util.*;

public class Logic {
    private ArrayList<Plug> plugs = new ArrayList<>();
    private Robot robot;

    public Logic(Robot robot) {
        this.robot = robot;
    }

    public void plug(Plug plug) {
        plugs.add(plug);
    }

    public void init() {
        for(Plug plug : plugs) {
            plug.init(robot);
        }
    }

    public void loop() {
        for(Plug plug : plugs) {
            plug.loop(robot);
        }
    }

    public void stop() {
        for(Plug plug : plugs) {
            plug.stop(robot);
        }
    }
}
