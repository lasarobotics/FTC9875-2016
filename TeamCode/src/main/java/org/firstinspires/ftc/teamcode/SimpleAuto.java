package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name = "simpleauto", group = "test")
public class SimpleAuto extends OpMode {
    DcMotor left_back, left_front, right_back, right_front, intake, shooter;
    boolean timeSet = false;
    long time;
    int powerState = 0;
    float power = 0;

    @Override
    public void init() {
        left_back = hardwareMap.dcMotor.get("left_back");
        left_front = hardwareMap.dcMotor.get("left_front");
        right_back = hardwareMap.dcMotor.get("right_back");
        right_front = hardwareMap.dcMotor.get("right_front");
        shooter = hardwareMap.dcMotor.get("shooter");
        timeSet = false;
    }

    @Override
    public void start() {}

    @Override
    public void loop() {
        if(!timeSet) {
            time = System.currentTimeMillis();
            timeSet = true;
        }
        long elapsed = System.currentTimeMillis() - time;
        if(elapsed < 3000) {
            shooter.setPower(-1);
        } else if(elapsed < 10000) {
            shooter.setPower(0);
        } else if(elapsed < 12500) {
            int power = 1;
            left_back.setPower(power);
            right_back.setPower(-power);
            left_front.setPower(power);
            right_front.setPower(-power);
        } else {
            stop();
        }
    }

    public void stop() {
        left_back.setPower(0);
        left_front.setPower(0);
        right_back.setPower(0);
        right_front.setPower(0);
        shooter.setPower(0);
    }

    private float damp(float tol, float val) {
        return Math.abs(val) < tol ? 0 : val;
    }
}