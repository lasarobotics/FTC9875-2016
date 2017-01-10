package org.firstinspires.ftc.teamcode.plugs;

import org.firstinspires.ftc.teamcode.Controller.*;
import org.firstinspires.ftc.teamcode.*;

public class IntakePlug implements Plug {
    private enum State {
        STOP, INTAKE, OUTTAKE
    }
    State state = State.STOP;

    public void init(Robot robot) {
        robot.c1.register(Button.LEFT_TRIGGER);
        robot.c1.register(Button.RIGHT_TRIGGER);
    }
    public void loop(Robot robot) {
        if(robot.c1.justPressed(Button.LEFT_TRIGGER)) {
           state = state == State.OUTTAKE ? State.STOP : State.OUTTAKE;
        }
        if(robot.c1.justPressed(Button.RIGHT_TRIGGER)) {
            state = state == State.INTAKE ? State.STOP : State.INTAKE;
        }

        switch(state) {
            case STOP:
                robot.intake.setPower(0);
                break;
            case INTAKE:
                robot.intake.setPower(1);
                break;
            case OUTTAKE:
                robot.intake.setPower(-1);
                break;
        }
    }
    public void stop(Robot robot) {

    }
}
