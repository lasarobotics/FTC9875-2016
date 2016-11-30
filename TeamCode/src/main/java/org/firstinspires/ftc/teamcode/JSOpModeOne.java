package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Created by Russell on 11/29/2016.
 */

@Autonomous(name = "js-1", group = "test")
public class JSOpModeOne extends OpMode {
    private JSOpModeManager manager = null;

    @Override
    public void init() {
        if(manager == null) {
            manager = new JSOpModeManager("one.js", gamepad1, gamepad2, hardwareMap);
        }
        manager.init();
    }

    @Override
    public void start() {
        manager.start();
    }

    @Override
    public void loop() {
        manager.loop();
    }

    @Override
    public void stop() {
        manager.stop();
    }
}
