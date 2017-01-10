package org.firstinspires.ftc.teamcode.plugs;

import org.firstinspires.ftc.teamcode.*;

public class MotorPlug implements Plug {
    public void init(Robot robot) {
        robot.left_back = robot.context.hardwareMap.dcMotor.get("left_back");
        robot.left_front = robot.context.hardwareMap.dcMotor.get("left_front");
        robot.right_back = robot.context.hardwareMap.dcMotor.get("right_back");
        robot.right_front = robot.context.hardwareMap.dcMotor.get("right_front");

        robot.intake = robot.context.hardwareMap.dcMotor.get("intake");
        robot.shooter = robot.context.hardwareMap.dcMotor.get("shooter");
        robot.lift_left = robot.context.hardwareMap.dcMotor.get("lift_left");
        robot.lift_right = robot.context.hardwareMap.dcMotor.get("lift_right");

        robot.button_presser = robot.context.hardwareMap.crservo.get("button_presser");
        robot.latch = robot.context.hardwareMap.crservo.get("latch");
    }
    public void loop(Robot robot) {

    }
    public void stop(Robot robot) {
        robot.left_back.setPower(0);
        robot.left_front.setPower(0);
        robot.right_back.setPower(0);
        robot.right_front.setPower(0);

        robot.intake.setPower(0);
        robot.shooter.setPower(0);
        robot.lift_left.setPower(0);
        robot.lift_right.setPower(0);

        robot.button_presser.setPower(0);
        robot.latch.setPower(0);
    }
}
