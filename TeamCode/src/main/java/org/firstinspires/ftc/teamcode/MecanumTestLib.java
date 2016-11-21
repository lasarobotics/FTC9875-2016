package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "mecanum-lib", group = "test")
public class MecanumTestLib extends OpMode {
    private enum ArmState {
        STOP, IN, OUT;
    }
    private enum IntakeState {
        STOP, INTAKE, OUTTAKE;
    }

    DcMotor left_back, left_front, right_back, right_front, intake, shooter;
    CRServo arm;
    private ArmState arm_state = ArmState.STOP;
    private float tol = 0.05f;
    private float trigger_tol = 0.25f;
    private boolean a_pressed = false;
    private boolean left_trigger_pressed = false;
    private boolean right_trigger_pressed = false;
    private boolean right_bumper_pressed = false;
    private IntakeState intake_state = IntakeState.STOP;
    private boolean reverse_direction = false;
    private static final int DAMPEN_CONSTANT = 4;
    //TODO: Look into ZeroPowerBehavior

    public void init() {
        left_back = hardwareMap.dcMotor.get("left_back");
        left_front = hardwareMap.dcMotor.get("left_front");
        right_back = hardwareMap.dcMotor.get("right_back");
        right_front = hardwareMap.dcMotor.get("right_front");
        left_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        left_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        right_back.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        right_front.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        intake = hardwareMap.dcMotor.get("intake");
        shooter = hardwareMap.dcMotor.get("shooter");
        arm = hardwareMap.crservo.get("button_presser");
    }

    public void loop() {
        float left_x  =  damp(tol, gamepad1.left_stick_x);
        float left_y  =  damp(tol, gamepad1.left_stick_y);
        float right_x = -damp(tol, gamepad1.right_stick_x);
        if(reverse_direction) {
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

        if(a_pressed) {
            if(!gamepad1.a) {
                //a -> !a
                a_pressed = false;
            }
        } else {
            if(gamepad1.a) {
                //!a -> a
                reverse_direction = reverse_direction ? false : true;
                a_pressed = true;
            }
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
        shooter.setPower(gamepad1.dpad_down ? -1 : 0);
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