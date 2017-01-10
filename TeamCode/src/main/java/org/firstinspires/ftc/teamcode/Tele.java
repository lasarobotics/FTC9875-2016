package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.*;
import org.firstinspires.ftc.teamcode.plugs.*;

@TeleOp(name = "tele")
public class Tele extends OpMode {
    private Robot robot;
    private Logic logic;

    public void init() {
        robot = new Robot(this);
        logic = new Logic(robot);

        logic.plug(new MotorPlug());
        logic.plug(new ControllerPlug());
        logic.plug(new MecanumPlug());
        logic.plug(new LiftPlug());
        logic.plug(new ArmPlug());

        logic.init();
    }
    public void loop() {
        logic.loop();
    }
    public void stop() {
        logic.stop();
    }
}
