package org.firstinspires.ftc.teamcode.plugs;

import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.teamcode.*;

public class BrakePlug implements Plug {
    public void init(Robot robot) {

    }
    public void loop(Robot robot) {
        if(robot.c1.right_bumper()) {
            robot.left_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            robot.left_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            robot.right_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            robot.right_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            robot.left_back.setPower(0);
            robot.left_front.setPower(0);
            robot.right_back.setPower(0);
            robot.right_front.setPower(0);
        }
    }
    public void stop(Robot robot) {

    }
}
