package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Robot {
    public DcMotor left_back, left_front, right_back, right_front, intake, shooter, lift_left, lift_right;
    public CRServo button_presser, latch;

    public Controller c1;
    public Controller c2;

    public OpMode context;

    public Robot(OpMode context) {
        this.context = context;
    }
}
