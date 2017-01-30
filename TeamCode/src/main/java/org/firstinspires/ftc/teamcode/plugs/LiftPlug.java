package org.firstinspires.ftc.teamcode.plugs;

import org.firstinspires.ftc.teamcode.*;
import org.firstinspires.ftc.teamcode.Controller.*;

public class LiftPlug implements Plug {
    public void init(Robot robot) {
        // register buttons
        robot.c1.register(Button.Y);
    }
    public void loop(Robot robot) {
        if(robot.c1.x()) {
            // raise lift
            robot.lift_left.setPower(1);
            robot.lift_right.setPower(1);
        } else {
            if(robot.c1.toggled(Button.Y)) {
                // lower lift
                robot.lift_left.setPower(-1);
                robot.lift_right.setPower(-1);
            } else {
                // stop lift
                robot.lift_left.setPower(0);
                robot.lift_right.setPower(0);
            }
        }
        // latch
        if(robot.c1.left_bumper()) {
            robot.latch.setPower(-1);
        } else if(robot.c1.right_bumper()) {
            robot.latch.setPower(1);
        }
    }
    public void stop(Robot robot) {

    }
}
