package org.firstinspires.ftc.teamcode.plugs;

import org.firstinspires.ftc.teamcode.*;

public class ShooterPlug implements Plug {
    public void init(Robot robot) {

    }
    public void loop(Robot robot) {
        if(robot.c1.dpad_down() || robot.c2.dpad_down()) {
            robot.shooter.setPower(-1);
        } else {
            robot.shooter.setPower(0);
        }
    }
    public void stop(Robot robot) {

    }
}
