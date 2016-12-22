package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "motor", group = "test")
public class MotorTest extends OpMode {
    public void init() {}
    public void loop() {
        String motors[] = {"left_back", "left_front", "right_back", "right_front"};
        /*for(String name : motors) {
            DcMotor m = hardwareMap.dcMotor.get(name);
            Log.i("MOTOR", name);
            m.setPower(1);

            try {
                wait(1000);
            } catch(InterruptedException e) {}

            m.setPower(0);
        }*/
        if(gamepad1.a)
            hardwareMap.dcMotor.get(motors[0]).setPower(1);
        else
            hardwareMap.dcMotor.get(motors[0]).setPower(0);
        if(gamepad1.b)
            hardwareMap.dcMotor.get(motors[1]).setPower(1);
        else
            hardwareMap.dcMotor.get(motors[1]).setPower(0);
        if(gamepad1.x)
            hardwareMap.dcMotor.get(motors[2]).setPower(1);
        else
            hardwareMap.dcMotor.get(motors[2]).setPower(0);

        if(gamepad1.y)
            hardwareMap.dcMotor.get(motors[3]).setPower(1);
        else
            hardwareMap.dcMotor.get(motors[3]).setPower(0);
    }
    public void stop() {}
}
