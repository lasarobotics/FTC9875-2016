package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.mozilla.javascript.*;

/**
 * Created by Russell on 11/20/2016.
 */

public class JSOpMode extends OpMode {
    String text;

    public JSOpMode(String text) {
        this.text = text;
        Context cx = Context.enter();
        //Make android compatible
        cx.setOptimizationLevel(-1);

        // Set version to JavaScript1.2 so that we get object-literal style
        // printing instead of "[object Object]"
        cx.setLanguageVersion(Context.VERSION_1_2);

        // Initialize the standard objects (Object, Function, etc.)
        // This must be done before scripts can be executed.
        Scriptable scope = cx.initStandardObjects();

        cx.exit();
    }

    @Override
    public void init() {

    }

    @Override
    public void loop() {

    }
}
