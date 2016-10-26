package org.firstinspires.ftc.teamcode;

import com.lasarobotics.library.drive.Mecanum;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.opmode.extensions.CameraControlExtension;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Size;

@TeleOp(name = "mechanum-lib", group = "test")
public class MecanumTestLib extends VisionOpMode {
    DcMotor left_back, left_front, right_back, right_front;
    GyroSensor gyro;
    private float tol = 0.05f;
    Beacon beacon = new Beacon();

    public void init() {
        super.init();
        this.setCamera(Cameras.PRIMARY);
        this.setFrameSize(new Size(512, 512));
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);
        beacon.setColorToleranceRed(0);
        beacon.setColorToleranceBlue(0);

        rotation.setIsUsingSecondaryCamera(false);
        rotation.disableAutoRotate();
        rotation.setActivityOrientationFixed(ScreenOrientation.LANDSCAPE);

        cameraControl.setColorTemperature(CameraControlExtension.ColorTemperature.AUTO);
        cameraControl.setAutoExposureCompensation();

        left_back = hardwareMap.dcMotor.get("left_back");
        left_front = hardwareMap.dcMotor.get("left_front");
        right_back = hardwareMap.dcMotor.get("right_back");
        right_front = hardwareMap.dcMotor.get("right_front");
        gyro = hardwareMap.gyroSensor.get("gyro");
        gyro.calibrate();
    }

    public void loop() {
        super.loop();
        Mecanum.fieldOriented(damp(tol, gamepad1.left_stick_y), damp(tol, gamepad1.left_stick_x), damp(tol, gamepad1.right_stick_x), gyro.getHeading(), left_front, right_front, left_back, right_back);
    }

    public void stop() {
        super.stop();
        left_back.setPower(0);
        left_front.setPower(0);
        right_back.setPower(0);
        right_front.setPower(0);
    }

    private float damp(float tol, float val) {
        return Math.abs(val) < tol ? 0 : val;
    }
}
