package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "mechanum", group = "test")
public class MechanumTest extends OpMode {
    DcMotor left_back, left_front, right_back, right_front;
    private float tol = 0.05f;

    public void init() {
        left_back = hardwareMap.dcMotor.get("left_back");
        left_front = hardwareMap.dcMotor.get("left_front");
        right_back = hardwareMap.dcMotor.get("right_back");
        right_front = hardwareMap.dcMotor.get("right_front");
    }

    public void loop() {
        left_back.setPower((double) damp(tol, gamepad1.left_stick_y));
        left_front.setPower((double) damp(tol, gamepad1.right_stick_y));
        right_back.setPower((double) damp(tol, gamepad1.left_stick_y));
        right_front.setPower((double) damp(tol, gamepad1.right_stick_y));
    }

    public void stop() {
        left_back.setPower(0);
        left_front.setPower(0);
        right_back.setPower(0);
        right_front.setPower(0);
    }

    private float damp(float tol, float val) {
        return Math.abs(val) < tol ? 0 : val;
    }
}
