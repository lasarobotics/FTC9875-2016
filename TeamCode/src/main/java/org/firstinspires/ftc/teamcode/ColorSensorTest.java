package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;

/**
 * Created by Russell on 12/6/2016.
 */

@Autonomous(name="Color sensor test", group="test")
public class ColorSensorTest extends OpMode {
    private ColorSensor top, bottom;
    private int counter = 0;

    @Override
    public void init() {
        top = hardwareMap.colorSensor.get("colors");
        bottom = hardwareMap.colorSensor.get("bottom");
        top.setI2cAddress(I2cAddr.create8bit(0x1c));
        bottom.setI2cAddress(I2cAddr.create8bit(0x2c));
    }

    @Override
    public void start() {
        counter = 0;
    }

    @Override
    public void loop() {
        telemetry.addData("Top", top.red());
        telemetry.addData("Bottom", bottom.red());
        if(counter / 5000 % 3 == 0) {
            top.enableLed(true);
            bottom.enableLed(false);
        } else if(counter / 5000 % 3 == 1) {
            top.enableLed(false);
            bottom.enableLed(true);
        } else {
            top.enableLed(true);
            bottom.enableLed(true);
        }
    }
}
