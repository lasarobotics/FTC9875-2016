package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by Russell on 11/21/2016.
 */

@TeleOp(name = "encoder-test", group = "test")
public class EncoderTest extends OpMode {
    DcMotor left_back;

    @Override
    public void init() {
        left_back = hardwareMap.dcMotor.get("left_back");
        while(!initializeEncoder(left_back)) {}
    }

    private static boolean initializeEncoder(DcMotor m) {
        m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if(m.getCurrentPosition() != 0) {
            return false;
        }
        m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        return true;
    }

    @Override
    public void loop() {
        telemetry.addData("position", left_back.getCurrentPosition());
        left_back.setPower(0.3f);
    }

    @Override
    public void stop() {
        left_back.setPower(0f);
    }
}
