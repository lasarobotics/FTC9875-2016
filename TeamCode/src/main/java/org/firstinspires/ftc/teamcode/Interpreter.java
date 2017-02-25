package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class Interpreter {
    private final String src;
    private final OpMode opMode;

    Context cx;
    ScriptableObject scope;

    public Interpreter(OpMode opMode, String src) {
        this.opMode = opMode;
        this.src = src;
    }

    public void init() {
        cx = Context.enter();
        cx.setOptimizationLevel(-1);
        scope = cx.initStandardObjects();
        ScriptableObject.putProperty(scope, "gamepad1", Context.javaToJS(opMode.gamepad1, scope));
        ScriptableObject.putProperty(scope, "gamepad2", Context.javaToJS(opMode.gamepad2, scope));
        ScriptableObject.putProperty(scope, "hardwareMap", Context.javaToJS(opMode.hardwareMap, scope));
        ScriptableObject.putProperty(scope, "server", Context.javaToJS(new ClientCodeUtilities(), scope));
        cx.evaluateString(scope, src, "<offline>", 1, null);
        callFunction("init");
    }

    public void loop() {
        callFunction("loop");
    }

    public void stop() {
        callFunction("stop");
        Context.exit();
    }

    public class ClientCodeUtilities {
        public void print(String msg) {
            System.out.println(msg);
        }
    }

    private void callFunction(String name) {
        if (scope == null) {
            Log.e("JS", "Null scope.");
        }
        Object obj = null;
        try {
            obj = scope.get(name, scope);
            Function fct = (Function) obj;
            if (fct == null) {
                Log.e("JS", "Function not found: " + name);
            }
            fct.call(cx, scope, scope, new Object[]{});
        } catch (ClassCastException cce) {
            Log.e("JS", "Function " + name + " is of type " + obj);
        }
    }
}
