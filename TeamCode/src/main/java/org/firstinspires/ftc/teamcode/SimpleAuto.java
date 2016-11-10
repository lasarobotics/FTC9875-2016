package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "simpleauto", group = "test")
public class SimpleAuto extends OpMode {
    DcMotor left_back, left_front, right_back, right_front, intake, shooter;
    boolean timeSet = false;
    long time;

    public void init() {
        left_back = hardwareMap.dcMotor.get("left_back");
        left_front = hardwareMap.dcMotor.get("left_front");
        right_back = hardwareMap.dcMotor.get("right_back");
        right_front = hardwareMap.dcMotor.get("right_front");
        timeSet = false;
    }

    public void loop() {
        if(!timeSet) {
            time = System.currentTimeMillis();
            timeSet = true;
        }
        long elapsed = System.currentTimeMillis() - time;
        if(elapsed < 2000) {
            shooter.setPower(1);
        } else if(elapsed < 3500) {
            shooter.setPower(0);
            Mecanum.arcade(0, 0, 1f, left_front, right_front, left_back, right_back);
        } else {
            int power = 0;
            left_back.setPower(power);
            right_back.setPower(power);
            left_front.setPower(power);
            right_front.setPower(power);
            shooter.setPower(0);
        }
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