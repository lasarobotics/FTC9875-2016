import com.kauailabs.navx.ftc.AHRS;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "navx-test")
public class NavXTest extends OpMode {
    private final int NAVX_DIM_I2C_PORT = 0;
    private AHRS navx;

    @Override
    public void init() {
        navx = AHRS.getInstance(hardwareMap.deviceInterfaceModule.get("navx"), NAVX_DIM_I2C_PORT, AHRS.DeviceDataType.kProcessedData);
    }

    @Override
    public void loop() {
        if(navx.isConnected()) {
            telemetry.addData("gyro_x: ", navx.getPitch());
        }
    }

    @Override
    public void stop() {
        navx.close();
    }
}
