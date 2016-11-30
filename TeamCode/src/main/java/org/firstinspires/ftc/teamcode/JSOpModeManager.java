package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Russell on 11/29/2016.
 */

public class JSOpModeManager {
    private String filename = null;
    private String fileContents = null;
    private Context cx;
    private ScriptableObject scope;
    private Gamepad gamepad1, gamepad2;
    private HardwareMap hardwareMap;

    public JSOpModeManager(String filename, Gamepad gamepad1, Gamepad gamepad2, HardwareMap hardwareMap) {
        this.filename = filename;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.hardwareMap = hardwareMap;
    }

    private void killContext() {
        cx.exit();
        cx = null;
    }

    public void init() {
        try {
            Scanner scan = new Scanner(new File(filename));

            //load in file
            StringBuilder sb = new StringBuilder();
            while(scan.hasNextLine()) {
                sb.append(scan.nextLine());
            }
            fileContents = sb.toString();

            scan.close();

            if(cx != null) {
                killContext();
            }
            cx = Context.enter();
            cx.setOptimizationLevel(-1); //make compatible with Android
            scope = cx.initStandardObjects();
            ScriptableObject.putProperty(scope, "gamepad1", Context.javaToJS(gamepad1, scope));
            ScriptableObject.putProperty(scope, "gamepad2", Context.javaToJS(gamepad2, scope));
            ScriptableObject.putProperty(scope, "hardwareMap", Context.javaToJS(hardwareMap, scope));
            cx.evaluateString(scope, fileContents, "<opmode>", 1, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void callFunction(String name) {
        Object obj = null;
        try {
            obj = scope.get(name, scope);
            Function fct = (Function)obj;
            if(fct == null) {
                return;
            }
            fct.call(cx, scope, scope, new Object[] {});
        } catch(ClassCastException cce) {
            //do nothing
        }
    }

    public void start() {
        callFunction("start");
    }

    public void loop() {
        callFunction("loop");
    }

    public void stop() {
        //TODO add some form of destruction mechanism to free up memory
        callFunction("stop");
        //fileContents = null;
        //killContext();
    }
}
