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
        for(String name : motors) {
            DcMotor m = hardwareMap.dcMotor.get(name);
            Log.i("MOTOR", name);
            m.setPower(1);

            try {
                wait(1000);
            } catch(InterruptedException e) {}

            m.setPower(0);
        }
    }
    public void stop() {}
}
