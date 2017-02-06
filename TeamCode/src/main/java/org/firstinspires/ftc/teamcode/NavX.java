package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.kauailabs.navx.ftc.AHRS;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class NavX {
    public AHRS navx;

    private class AxisData {
        float last = 0;
        float current = 0;
        int direction = 0;
        boolean rolledOver = false;

        void loop(float raw) {
            float norm = normAngle(raw);
            float diff = angleDiff(norm, current % 360);
            int newDirection = signBetween(norm, current % 360);

            if (newDirection != direction)
                rolledOver = false;

            direction = newDirection;

            if (diff < rolloverTolerance) {
                if (!rolledOver) {
                    rolledOver = true;
                    if (direction < 0) {
                        current -= 360;
                    } else if (direction > 0) {
                        current += 360;
                    }
                }
            }

            current = 360 * (float) Math.floor(current / 360) + norm;
        }
    }

    private static final float rolloverTolerance = 0.2f;
    private AxisData yaw = new AxisData();
    private AxisData pitch = new AxisData();
    private AxisData roll = new AxisData();

    public NavX(HardwareMap hardwareMap, String dimName) {
        navx = AHRS.getInstance(hardwareMap.deviceInterfaceModule.get(dimName), 0, AHRS.DeviceDataType.kProcessedData);
        Log.d("NavX init", "");
    }

    public void loop() {
        yaw.loop(navx.getYaw());
        pitch.loop(navx.getPitch());
        roll.loop(navx.getRoll());
    }

    public double getYaw() {
        return yaw.current;
    }

    public double getPitch() {
        return pitch.current;
    }

    public double getRoll() {
        return roll.current;
    }

    /**
     * converts raw navx angles to [0, 360)
     *
     * @return normalized angle
     */
    private static float normAngle(float theta) {
        return theta >= 0 ? theta : 360 + theta;
    }

    /**
     * computes shortest difference between normed angles
     *
     * @param theta angle [0, 360)
     * @param phi   angle [0, 360)
     * @return difference in range [0, 180)
     */
    private float angleDiff(float theta, float phi) {
        return Math.min((theta - phi) % 360, (phi - theta) % 360);
    }

    /**
     * calculates sign of rotation between angles assuming difference less than 180
     * @param theta angle [0, 360)
     * @param phi angle [0, 360)
     * @return value in {-1, 0, 1} representing sign
     */
    private int signBetween(float theta, float phi) {
        if (theta == phi) return 0;

        float psi = theta - phi;
        if (psi > 0)
            return psi <= 180 ? 1 : -1;
        else
            return Math.abs(psi) <= 180 ? -1 : 1;
    }
}
