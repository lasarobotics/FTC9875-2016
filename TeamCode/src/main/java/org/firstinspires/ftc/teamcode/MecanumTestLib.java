package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.HashMap;

@TeleOp(name = "mecanum-lib", group = "test")
public class MecanumTestLib extends OpMode {
    private enum ArmState {
        STOP, IN, OUT;
    }
    private enum IntakeState {
        STOP, INTAKE, OUTTAKE;
    }
    private enum ShooterState {
        EXTENDED, //not ready to shoot
        RECOILED, //arm pulled back
        TRANSITIONING, //motor is moving from extended -> recoiled
        RESETTING, //setting encoder to 0
        FIRING //firing
    }

    DcMotor left_back, left_front, right_back, right_front, intake, shooter;
    CRServo arm;
    private InputHandler inputHandler = new InputHandler();
    private ArmState arm_state = ArmState.STOP;
    private float tol = 0.05f;
    private float trigger_tol = 0.25f;
    private boolean left_trigger_pressed = false;
    private boolean right_trigger_pressed = false;
    private boolean right_bumper_pressed = false;
    private IntakeState intake_state = IntakeState.STOP;
    private ShooterState shooter_state = ShooterState.RESETTING;
    private static final int DAMPEN_CONSTANT = 4;
    private static final int SHOOTER_FIRE_POSITION = -1550;
    private static final int SHOOTER_DESIRED_POSITION = -1350;

    public void init() {
        initializeMotors(); //get motors
        initializeEncoders(); //turn on and reset all encoders
        initializeToggles(); //sets up toggles for buttons
    }

    private boolean shooterControlPressed() {
        return inputHandler.justPressed("shooter_control");
    }

    //register all toggle buttons with input handler
    private void initializeToggles() {
        inputHandler.registerButtons(
                "reverse_direction",
                "shooter_control",
                "manual_shooter"
        );
    }

    private class InputHandler {
        //variables and inner classes
        private class ToggleState {
            public boolean pressed = false;
            public boolean justPressed = false;
            public boolean justReleased = false;
            public boolean toggled = false;
        }
        private HashMap<String, ToggleState> toggles = new HashMap<>();

        //methods
        public void registerButtons(String... names) {
            for(String name : names) {
                registerButton(name);
            }
        }
        public void registerButton(String name) {
            ToggleState ts = new ToggleState();
            toggles.put(name, ts);
        }
        public void updateState(String name, boolean newState) {
            ToggleState ts = toggles.get(name);
            ts.justPressed = false;
            ts.justReleased = false;
            if(ts.pressed != newState) { //the state was changed
                if(newState) {
                    //!button -> button
                    ts.justPressed = true;
                    ts.toggled = ts.toggled ? false : true; //update toggle
                } else {
                    //button -> !button
                    ts.justReleased = true;
                }
            }
            ts.pressed = newState;
        }
        public boolean getToggled(String name) {
            return toggles.get(name).toggled;
        }
        public boolean justPressed(String name) {
            return toggles.get(name).justPressed;
        }
        public boolean justReleased(String name) {
            return toggles.get(name).justReleased;
        }
        public boolean pressed(String name) {
            return toggles.get(name).pressed;
        }
    }

    //Sets the zero power behavior (i.e. what should the motor be doing
    //when power == 0) of the four main wheel motors.
    private void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        left_back.setZeroPowerBehavior(behavior);
        left_front.setZeroPowerBehavior(behavior);
        right_back.setZeroPowerBehavior(behavior);
        right_front.setZeroPowerBehavior(behavior);
    }

    //Gets all motors from the hardwareMap and does some initial configuration
    private void initializeMotors(){
        left_back = hardwareMap.dcMotor.get("left_back");
        left_front = hardwareMap.dcMotor.get("left_front");
        right_back = hardwareMap.dcMotor.get("right_back");
        right_front = hardwareMap.dcMotor.get("right_front");
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        intake = hardwareMap.dcMotor.get("intake");
        shooter = hardwareMap.dcMotor.get("shooter");
        arm = hardwareMap.crservo.get("button_presser");
    }

    //Sets the mode for all encoder-enabled motors
    private void setEncoderMode(DcMotor.RunMode mode) {
        left_back.setMode(mode);
        left_front.setMode(mode);
        right_back.setMode(mode);
        right_front.setMode(mode);
        intake.setMode(mode);
    }

    //Resets all encoders and then sets to proper runmode
    private void initializeEncoders() {
        setEncoderMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER); //set everything to 0
    }

    @Override
    public void start() {
        setEncoderMode(DcMotor.RunMode.RUN_USING_ENCODER); //adjust speed using encoder
    }

    public void loop() {
        inputHandler.updateState("reverse_direction", gamepad1.a);
        inputHandler.updateState("shooter_control", gamepad1.dpad_up);
        inputHandler.updateState("manual_shooter", gamepad1.dpad_down);

        float left_x  =  damp(tol, gamepad1.left_stick_x);
        float left_y  =  damp(tol, gamepad1.left_stick_y);
        float right_x = -damp(tol, gamepad1.right_stick_x);
        if(inputHandler.getToggled("reverse_direction")) {
            left_y  *= -1;
            right_x *= -1;
        }
        if(gamepad1.b) {
            //fine-grained control, slower movement
            left_x  /= DAMPEN_CONSTANT;
            left_y  /= DAMPEN_CONSTANT;
            right_x  = 0;
        }
        if(right_bumper_pressed) {
            left_x  = 0;
            left_y  = 0;
            right_x = 0;
        }
        Mecanum.arcade(left_x, left_y, right_x, left_front, right_front, left_back, right_back);
        arm_state = gamepad1.dpad_left ? ArmState.IN : gamepad1.dpad_right ? ArmState.OUT : ArmState.STOP;

        if(left_trigger_pressed) {
            if(gamepad1.left_trigger <= trigger_tol) {
                //left_trigger -> !left_trigger
                left_trigger_pressed = false;
            }
        } else {
            if(gamepad1.left_trigger > trigger_tol) {
                //!left_trigger -> left_trigger
                intake_state = intake_state == IntakeState.OUTTAKE ?
                        IntakeState.STOP :
                        IntakeState.OUTTAKE; //toggle outtake
                left_trigger_pressed = true;
            }
        }
        telemetry.addData("LEFT", left_back.getCurrentPosition());
        telemetry.addData("Typ", left_back.getZeroPowerBehavior());
        telemetry.addData("Shooter", shooter.getCurrentPosition());
        telemetry.addData("Shooter state", shooter_state);

        if(right_trigger_pressed) {
            if(gamepad1.right_trigger <= trigger_tol) {
                //right_trigger -> !right_trigger
                right_trigger_pressed = false;
            }
        } else {
            if(gamepad1.right_trigger > trigger_tol) {
                //!right_trigger -> right_trigger
                intake_state = intake_state == IntakeState.INTAKE ?
                        IntakeState.STOP :
                        IntakeState.INTAKE; //toggle intake
                right_trigger_pressed = true;
            }
        }

        if(right_bumper_pressed) {
            if(!gamepad1.right_bumper) {
                //right_bumper -> !right_bumper
                left_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                left_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                right_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                right_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                right_bumper_pressed = false;
            }
        } else {
            if(gamepad1.right_bumper) {
                //!right_bumper -> right_bumper
                left_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                left_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                right_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                right_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                right_bumper_pressed = true;
            }
        }

        switch(shooter_state) {
            case EXTENDED:
                if(shooterControlPressed()) {
                    shooter.setPower(-0.4f);
                    shooter_state = ShooterState.TRANSITIONING;
                } else {
                    //shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                }
                break;
            case RECOILED:
                if(shooterControlPressed()) {
                    shooter_state = ShooterState.FIRING;
                } else {
                    //attempt to normalize shooter position
                    if(shooter.getCurrentPosition() < SHOOTER_DESIRED_POSITION) {
                        //shooter.setPower(0.1f);
                        shooter.setPower(0f);
                    } else if(shooter.getCurrentPosition() > SHOOTER_DESIRED_POSITION) {
                        shooter.setPower(-0.2f);
                    } else {
                        shooter.setPower(0f);
                    }
                }
                break;
            case TRANSITIONING:
                if(shooter.getCurrentPosition() < SHOOTER_DESIRED_POSITION) {
                    shooter.setPower(0f);
                    shooter_state = ShooterState.RECOILED;
                }
                break;
            case FIRING:
                if(shooter.getCurrentPosition() > SHOOTER_FIRE_POSITION) {
                    shooter.setPower(-0.4f);
                } else {
                    shooter_state = ShooterState.RESETTING;
                }
                break;
            case RESETTING:
                shooter.setPower(0f);
                shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                //check to see if resetting complete
                if(shooter.getCurrentPosition() == 0) {
                    //if complete, move to extended state
                    shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    shooter_state = ShooterState.EXTENDED;
                }
                break;
        }

        switch(arm_state) {
            case STOP:
                arm.setPower(0);
                break;
            case IN:
                arm.setPower(1);
                break;
            case OUT:
                arm.setPower(-1);
                break;
        }

        switch (intake_state) {
            case STOP:
                intake.setPower(0);
                break;
            case INTAKE:
                intake.setPower(1);
                break;
            case OUTTAKE:
                intake.setPower(-1);
                break;
        }
        if(inputHandler.pressed("manual_shooter")) {
            shooter.setPower(-1);
        }
        if(inputHandler.justReleased("manual_shooter")) {
            shooter.setPower(0);
        }
        //shooter.setPower(gamepad1.dpad_down ? -1 : 0);
    }

    public void stop() {
        left_back.setPower(0);
        left_front.setPower(0);
        right_back.setPower(0);
        right_front.setPower(0);
        intake.setPower(0);
        shooter.setPower(0);
        arm.setPower(0);
    }

    private float damp(float tol, float val) {
        return Math.abs(val) < tol ? 0 : val;
    }
}