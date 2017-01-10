package org.firstinspires.ftc.teamcode.plugs;

import org.firstinspires.ftc.teamcode.*;

public class ControllerPlug implements Plug {
    public void init(Robot robot) {
        robot.c1 = new Controller(robot.context.gamepad1);
        robot.c2 = new Controller(robot.context.gamepad2);

        robot.c1.setStickTol(0.05f);
        robot.c2.setTriggerTol(0.25f);
    }
    public void loop(Robot robot) {
        robot.c1.update();
        robot.c2.update();
    }
    public void stop(Robot robot) {

    }
}
