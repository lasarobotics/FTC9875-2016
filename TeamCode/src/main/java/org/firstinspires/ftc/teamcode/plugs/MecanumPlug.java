package org.firstinspires.ftc.teamcode.plugs;

import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.teamcode.*;

public class MecanumPlug implements Plug {
    public void init(Robot robot) {
        // prevent coasting
        robot.left_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.left_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.right_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.right_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    public void loop(Robot robot) {
        // strafe if the right trigger is being used, otherwise drive normally
        if(robot.c1.right_stick_x() > 0)
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

        robot.left_back.setPower(l);
        robot.left_front.setPower(l);
        robot.right_back.setPower(r);
        robot.right_front.setPower(r);
    }
    // strafe with right_x
    private void strafe(Robot robot) {
        float x = robot.c1.right_stick_x();

        robot.left_back.setPower(x);
        robot.left_front.setPower(-x);
        robot.right_back.setPower(x);
        robot.right_front.setPower(-x);
    }
}
