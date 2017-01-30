package org.firstinspires.ftc.teamcode.plugs;


import org.firstinspires.ftc.teamcode.Controller.*;
import org.firstinspires.ftc.teamcode.*;

public class MecanumPlug implements Plug {
    double scale = 1;

    public void init(Robot robot) {
        robot.c1.register(Button.B);
    }
    public void loop(Robot robot) {
        scale = robot.c1.toggled(Button.B) ? 0.25 : 1;
        // strafe if the right trigger is being used, otherwise drive normally
        if(Math.abs(robot.c1.right_stick_x()) > 0)
            strafe(robot);
        else
            drive(robot);
    }
    public void stop(Robot robot) {

    }

    // normal drive with left stick without strafing
    private void drive(Robot robot) {
        // add forward/backward movement values from left_y to rotation values from left_x
        float l = robot.c1.left_stick_x() - robot.c1.left_stick_y();
        float r = robot.c1.left_stick_x() + robot.c1.left_stick_y();

        // normalize values
        float max = Math.max(Math.abs(l), Math.abs(r));
        if(max > 1) {
            l /= max;
            r /= max;
        }

        robot.left_back.setPower(scale * l);
        robot.left_front.setPower(scale * l);
        robot.right_back.setPower(scale * r);
        robot.right_front.setPower(scale * r);
    }
    // strafe with right_x
    private void strafe(Robot robot) {
        float s = robot.c1.right_stick_x();

        robot.left_back.setPower(scale * s);
        robot.left_front.setPower(-scale * s);
        robot.right_back.setPower(scale * s);
        robot.right_front.setPower(-scale * s);
    }
}
