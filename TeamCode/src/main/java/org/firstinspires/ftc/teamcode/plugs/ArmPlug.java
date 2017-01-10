package org.firstinspires.ftc.teamcode.plugs;

import org.firstinspires.ftc.teamcode.*;

public class ArmPlug implements Plug {
    public void init(Robot robot) {

    }
    public void loop(Robot robot) {
        if(robot.c1.dpad_left()) {
            robot.button_presser.setPower(1);
        } else if(robot.c1.dpad_right()) {
            robot.button_presser.setPower(-1);
        } else {
            robot.button_presser.setPower(0);
        }
    }
    public void stop(Robot robot) {

    }
}
