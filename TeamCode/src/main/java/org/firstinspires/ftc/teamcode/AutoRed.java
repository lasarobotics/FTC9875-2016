package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by Russell on 11/23/2016.
 */

@Autonomous(name = "auto-red", group = "test")
public class AutoRed extends OpMode {
    private static Script scriptObj;
    private static volatile boolean context = false;

    private class Script {
        private Context cx;
        private boolean inContext = false;
        private ScriptableObject scope;
        private final Object scriptLock = new Object();
        private long threadId = -1;

        public String callFunction(String name) {
            synchronized (scriptLock) {
                if (scope == null) {
                    return "Null scope.";
                }
                Object obj = null;
                try {
                    obj = scope.get(name, scope);
                    Function fct = (Function) obj;
                    if (fct == null) {
                        return "Function not found: " + name;
                    }
                    fct.call(cx, scope, scope, new Object[]{});
                } catch (ClassCastException cce) {
                    return "Function " + name + " is of type " + obj;
                }
                return null;
            }
        }

        public Object evaluateString(String str, String name) {
            if (cx == null) {
                return null;
            }
            return cx.evaluateString(scope, str, name, 1, null);
        }

        //do not run me if a context already exists
        private void createContext() {
            synchronized (scriptLock) {
                if (inContext) {
                    return;
                }
                threadId = Thread.currentThread().getId();
                cx = Context.enter();
                context = true;
                cx.setOptimizationLevel(-1); //make compatible with Android

                scope = cx.initStandardObjects();
                ScriptableObject.putProperty(scope, "gamepad1", Context.javaToJS(gamepad1, scope));
                ScriptableObject.putProperty(scope, "gamepad2", Context.javaToJS(gamepad2, scope));
                ScriptableObject.putProperty(scope, "hardwareMap", Context.javaToJS(hardwareMap, scope));
                ScriptableObject.putProperty(scope, "server", Context.javaToJS(new ClientCodeUtilities(), scope));
                inContext = true;
            }
        }

        public void clearContext() {
            if (!inContext) return;
            if (Thread.currentThread().getId() != threadId) return; //not running on context thread
            if (context) {
                Context.exit();
                context = false;
            }
            createContext();
            inContext = false;
        }
    }

    public class ClientCodeUtilities {
        public void print(String msg) {
            System.out.println(msg);
        }
    }

    @Override
    public void init() {
        if (scriptObj == null) {
            scriptObj = new Script();
        }
        scriptObj.createContext();
        scriptObj.evaluateString(scriptLiteral, "<offline>");
        if (scriptObj.callFunction("init") != null) {
            throw new RuntimeException("Unable to init");
        }
    }

    @Override
    public void start() {
        if (scriptObj.callFunction("start") != null) {
            throw new RuntimeException("Unable to start");
        }
    }

    @Override
    public void stop() {
        if (scriptObj.callFunction("stop") != null) {
            throw new RuntimeException("Unable to stop");
        }
        scriptObj.clearContext();
    }

    private void smallDelay() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ie) {
            System.out.println("Interrupted.");
        }
    }

    @Override
    public void loop() {
        String err = scriptObj.callFunction("loop");
        if (err != null) {
            System.out.println("Unable to loop!");
            scriptObj.callFunction("stop");
            throw new RuntimeException("Unable to loop: " + err);
        }
    }

    private static final String scriptLiteral = "var left_back, left_front, right_back, right_front, arm, latch, \n" +
            "bottom, colors, range;\n" +
            "var hardware = Packages.com.qualcomm.robotcore.hardware;\n" +
            "var tol = 0.05;\n" +
            "var commandIndex = 0;\n" +
            "var ninety_degree_angle = 1650; //TODO get better at this\n" +
            "var full_rotation = 1440;\n" +
            "var CENTIMETERS = \n" +
            "Packages.org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit.CM;\n" +
            "var run_once_db = {};\n" +
            "var AHRS = Packages.com.kauailabs.navx.ftc.AHRS;\n" +
            "\n" +
            "var commands = [\n" +
            "\t/*rotate(-180), //test out motor ability to return to \n" +
            "start\n" +
            "\tdrive(1000),\n" +
            "\trotate(90),\n" +
            "\tdrive(500),\n" +
            "\tstrafe(400),\n" +
            "\tstrafe(-400),\n" +
            "\trotate(-180),\n" +
            "\tdrive(500),\n" +
            "\trotate(-90),\n" +
            "\tdrive(1000),\n" +
            "\texit(),*/\n" +
            "\t/*drive(1600),\n" +
            "\tsleep(300),\n" +
            "\tnice_rotation(90),\n" +
            "\tsleep(300),\n" +
            "\tdrive(5900),\n" +
            "\tsleep(300),\n" +
            "\tnice_rotation(-90),\n" +
            "\treset_motors(),\n" +
            "\tsleep(300),\n" +
            "\tenable_led(true),\n" +
            "\t_while(\"toplevel\", times(2)),\n" +
            "\t\tdrive(2000, false),\n" +
            "\t\toutput(\"toplevel\"),\n" +
            "\t\tenable_led(true), //KILL ME\n" +
            "\t\tdrive_power(0.2),\n" +
            "\t\toutput(\"waiting until touching white\"),\n" +
            "\t\twait_until(touching_white()),\n" +
            "\t\t_while(\"nearbeacondoublecheck\", \n" +
            "not(near_beacon())),\n" +
            "\t\t\toutput(\"strafing\"),\n" +
            "\t\t\tenable_top_led(true),\n" +
            "\t\t\tstrafe_power(-0.25),\n" +
            "\t\t\t_while(\"stay_on_white\", \n" +
            "not(near_beacon())),\n" +
            "\t\t\t\t_if(\"off\", \n" +
            "not(touching_white())),\n" +
            "\t\t\t\t\toutput(\"    not \n" +
            "touching white\"),\n" +
            "\t\t\t\t\tdrive_power(-0.1),\n" +
            "\t\t\t\t\t\n" +
            "wait_until(or(touching_white(), driven_forward(200))),\n" +
            "\t\t\t\t\toutput(\"    wait \n" +
            "done\"),\n" +
            "\t\t\t\t\t_if(\"twinner\", \n" +
            "not(touching_white())),\n" +
            "\t\t\t\t\t\toutput(\"        \n" +
            "not touching white\"),\n" +
            "\t\t\t\t\t\t\n" +
            "drive_power(0.1),\n" +
            "\t\t\t\t\t\t\n" +
            "wait_until(touching_white()),\n" +
            "\t\t\t\t\t_fi(\"twinner\"),\n" +
            "\t\t\t\t\toutput(\"    going back \n" +
            "to strafing\"),\n" +
            "\t\t\t\t\tstrafe_power(-0.25),\n" +
            "\t\t\t\t_fi(\"off\"),\n" +
            "\t\t\t_done(\"stay_on_white\"),\n" +
            "\t\t\toutput(\"double checking beacon...\"),\n" +
            "\t\t\tdrive_power(0.0),\n" +
            "\t\t\tsleep(100),\n" +
            "\t\t_done(\"nearbeacondoublecheck\"),\n" +
            "\t\tdrive_power(0),\n" +
            "\t\tenable_top_led(false),\n" +
            "\t\treset_motors(),\n" +
            "\t\tsleep(800),\n" +
            "\t\toutput(\"hit beacon\"),\n" +
            "\t\t_if(\"unknown\", beacon_color_unknown()),\n" +
            "\t\t\toutput(\"Skipping, color UNKNOWN\"),\n" +
            "\t\t\tstrafe(1200),\n" +
            "\t\t_else(\"unknown\"),\n" +
            "\t\t\t_if(\"blue?\", beacon_blue()),\n" +
            "\t\t\t\toutput(\"blue beacon\"),\n" +
            "\t\t\t\toutput(\"    strafe\"),\n" +
            "\t\t\t\t/*strafe(150),\n" +
            "\t\t\t\tsleep(200),\n" +
            "\t\t\t\toutput(\"    drive\"),\n" +
            "\t\t\t\tdrive(-50),\n" +
            "\t\t\t\tsleep(200),\n" +
            "\t\t\t\toutput(\"    strafe\"),\n" +
            "\t\t\t\tstrafe(-150),*/\n" +
            "\t\t\t\t/*drive(-50),\n" +
            "\t\t\t\treset_motors(),\n" +
            "\t\t\t\tbutton_presser(1000),\n" +
            "\t\t\t\tbutton_presser(-1000),\n" +
            "\t\t\t\tdrive(50),\n" +
            "\t\t\t\tstrafe(1200),\n" +
            "\t\t\t_else(\"blue?\"),\n" +
            "\t\t\t\toutput(\"red_beacon\"),\n" +
            "\t\t\t\toutput(\"    strafe\"),\n" +
            "\t\t\t\tstrafe(300),\n" +
            "\t\t\t\tsleep(200),\n" +
            "\t\t\t\toutput(\"    drive\"),\n" +
            "\t\t\t\tdrive(-550),\n" +
            "\t\t\t\tsleep(200),\n" +
            "\t\t\t\toutput(\"    strafe\"),\n" +
            "\t\t\t\tstrafe(-300),\n" +
            "\t\t\t\treset_motors(),\n" +
            "\t\t\t\tbutton_presser(2000),\n" +
            "\t\t\t\tbutton_presser(-2000),\n" +
            "\t\t\t\tsleep(200),\n" +
            "\t\t\t\tstrafe(1200),\n" +
            "\t\t\t\tsleep(500),\n" +
            "\t\t\t\tdrive(550),\n" +
            "\t\t\t_fi(\"blue?\"),\n" +
            "\t\t_fi(\"unknown\"),\n" +
            "\t\t//rotate(3),\n" +
            "\t\treset_motors(),\n" +
            "\t\tsleep(200),\n" +
            "\t\trun_once(\"driveafterfirstbeacon\", drive(1000)),\n" +
            "\t\toutput(\"---------DONE with one \n" +
            "beacon----------\"),\n" +
            "\t_done(\"toplevel\"),\n" +
            "\tdrive_power(0.0),\n" +
            "\tenable_led(false),\n" +
            "\tsleep(300),\n" +
            "\tdrive(-4000),\n" +
            "\tnice_rotation(-90),\n" +
            "\tdrive(2200),\n" +
            "\tnice_rotation(-20),*/\n" +
            "\t//nice_rotation(-30),\n" +
            "\tartificialZeroYaw(),\n" +
            "\tdrive(-full_rotation * 2),\n" +
            "\tnice_rotation(45),\n" +
            "\tdrive(-full_rotation * 3.8),\n" +
            "\tnice_rotation(0),\n" +
            "\toutput(\"DONE\"),\n" +
            "\tenable_led(true),\n" +
            "\tdrive_power(0.3),\n" +
            "\tenable_led(true),\n" +
            "\twait_until(touching_white()),\n" +
            "\tenable_led(false),\n" +
            "\tdrive_power(0),\n" +
            "\toutput(\"testing\"),\n" +
            "\tnice_rotation(0),\n" +
            "\tdrive(-400),\n" +
            "\tnice_rotation(0),\n" +
            "\toutput(\"preif\"),\n" +
            "\t_if(\"blue?\", beacon_blue()),\n" +
            "\t\toutput(\"blue-----------------\"),\n" +
            "\t\tdrive(-550),\n" +
            "\t\tnice_rotation(0),\n" +
            "\t_else(\"blue?\"),\n" +
            "\t\toutput(\"not blue----------\"),\n" +
            "\t_fi(\"blue?\"),\n" +
            "\t\n" +
            "\toutput(\"about to press button\"),\n" +
            "\tbutton_presser(2300),\n" +
            "\tbutton_presser(-2300),\n" +
            "\tdrive(-2300, false),\n" +
            "\tnice_rotation(0),\n" +
            "\tdrive_power(-0.3),\n" +
            "\toutput(\"waiting for white\"),\n" +
            "\tenable_led(true),\n" +
            "\twait_until(touching_white()),\n" +
            "\tenable_led(false),\n" +
            "\tdrive_power(0),\n" +
            "\tnice_rotation(0),\n" +
            "\tdrive(300),\n" +
            "\tnice_rotation(0),\n" +
            "\t_if(\"blue2?\", beacon_blue()),\n" +
            "\t\tdrive(-650),\n" +
            "\t\tnice_rotation(0),\n" +
            "\t\toutput(\"blue2----------------\"),\n" +
            "\t_else(\"blue2?\"),\n" +
            "\t\toutput(\"not blue2------------\"),\n" +
            "\t_fi(\"blue2?\"),\n" +
            "\toutput(\"about to press button\"),\n" +
            "\tbutton_presser(2700),\n" +
            "\tbutton_presser(-2700),\n" +
            "\texit(),\n" +
            "\tdrive(3000),\n" +
            "\tnice_rotation(-90),\n" +
            "\tdrive(-3000), //-2500\n" +
            "\t//drive(3000),\n" +
            "\tnice_rotation(45),\n" +
            "\tdrive(2000),\n" +
            "];\n" +
            "\n" +
            "function artificialZeroYaw() {\n" +
            "\treturn function() {\n" +
            "\t\tyawChange = navx.getYaw();\n" +
            "\t\treturn function() {\n" +
            "\t\t\tnext();\n" +
            "\t\t\treturn true;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "var yawChange = -102.44;\n" +
            "\n" +
            "function yaw() {\n" +
            "\tvar yaw = navx.getYaw();\n" +
            "\tyaw -= yawChange;\n" +
            "\tif(yaw < 0) yaw += 360;\n" +
            "\tyaw %= 360;\n" +
            "\tyaw = fix_angle(yaw);\n" +
            "\tyaw %= 360;\n" +
            "\treturn yaw;\n" +
            "}\n" +
            "\n" +
            "function nice_rotation(theta) {\n" +
            "\tvar continuousRoundsCompleted = 0;\n" +
            "\treturn function() {\n" +
            "\t\tserver.print(\"Running nice rotation\");\n" +
            "\t\tset_run_using_encoder();\n" +
            "\t\tvar target = (theta)%360;\n" +
            "\t\tvar whereToGoOriginal = \n" +
            "getCircleStuff(fix_angle(yaw()), target);\n" +
            "\t\tserver.print(\"Where to go original: \" + \n" +
            "JSON.stringify(whereToGoOriginal));\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar whereToGo = \n" +
            "getCircleStuff(fix_angle(yaw()), target);\n" +
            "\n" +
            "\t\t\t\n" +
            "//server.print((Math.abs(fix_angle(yaw()) - \n" +
            "//target)%360) + \" \" + whereToGoOriginal.dist);\n" +
            "\t\t\tvar completedFrac = \n" +
            "(Math.abs(fix_angle(yaw()) - \n" +
            "target)%360)/whereToGoOriginal.dist * 0.8;\n" +
            "\t\t\t//server.print(completedFrac);\n" +
            "\t\t\tvar power = Math.max(0.02, \n" +
            "Math.min(completedFrac, 0.1) * 0.8);\n" +
            "\n" +
            "\t\t\tvar left = (target - \n" +
            "fix_angle(yaw()))%360;\n" +
            "\t\t\t\n" +
            "//server.print((Math.round(left*100)/100) + \" \" + \n" +
            "//(Math.round(100*completedFrac)/100));\n" +
            "\n" +
            "\t\t\tforAllMotors(function(m) \n" +
            "{m.setPower(whereToGo.orientation ? -power : power);});\n" +
            "\n" +
            "\t\t\tvar angleTolerance = 1.5;\n" +
            "\t\t\t\n" +
            "if(closeInProximity(fix_angle(yaw()), target, \n" +
            "angleTolerance)) {\n" +
            "\t\t\t\t++continuousRoundsCompleted;\n" +
            "\t\t\t\t\n" +
            "server.print(continuousRoundsCompleted);\n" +
            "\t\t\t\tif(continuousRoundsCompleted > \n" +
            "10) {\n" +
            "\t\t\t\t\tserver.print(\"theta: \" + fix_angle(yaw()) + \" target: \" + target + \" angleTolerance: \" + angleTolerance);\n" +
            "\t\t\t\t\t\n" +
            "forAllMotors(function(m) {m.setPower(0);});\n" +
            "\t\t\t\t\tnext();\n" +
            "\t\t\t\t\treturn true;\n" +
            "\t\t\t\t}\n" +
            "\t\t\t} else {\n" +
            "\t\t\t\tcontinuousRoundsCompleted = 0;\n" +
            "\t\t\t}\n" +
            "\t\t\treturn false;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function closeInProximity(theta0, theta1, dist) {\n" +
            "\tvar diff = Math.abs((theta0 - theta1)) % 360;\n" +
            "\tif(diff < 0) {\n" +
            "\t\tdiff += 360;\n" +
            "\t}\n" +
            "\tif(diff <= dist) return true;\n" +
            "\tdiff = -diff;\n" +
            "\tdiff += 360;\n" +
            "\tif(diff <= dist) return true;\n" +
            "\treturn false;\n" +
            "}\n" +
            "\n" +
            "function getCircleStuff(theta0, theta1) {\n" +
            "\tvar dt0 = theta0 - theta1;\n" +
            "\tvar dt1 = -dt0;\n" +
            "\tdt0 %= 360;\n" +
            "\tdt1 %= 360;\n" +
            "\n" +
            "\tvar flip0 = false;\n" +
            "\tvar flip1 = false;\n" +
            "\tif(dt0 < 0) {\n" +
            "\t\tdt0 += 360;\n" +
            "\t\tflip0 = true;\n" +
            "\t}\n" +
            "\tif(dt1 < 0) {\n" +
            "\t\tdt1 += 360;\n" +
            "\t\tflip1 = true;\n" +
            "\t}\n" +
            "\n" +
            "\tif(dt0 < dt1) {\n" +
            "\t\tvar end = (theta0 - dt0)%360;\n" +
            "\t\tif(end < 0) {\n" +
            "\t\t\tend += 360;\n" +
            "\t\t}\n" +
            "\t\treturn {\n" +
            "\t\t\torientation: !flip0 && !(theta0 > \n" +
            "theta1),\n" +
            "\t\t\tdist: dt0\n" +
            "\t\t};\n" +
            "\t} else {\n" +
            "\t\tvar end = (theta0 - dt1)%360;\n" +
            "\t\tif(end < 0) {\n" +
            "\t\t\tend += 360;\n" +
            "\t\t}\n" +
            "\t\treturn {\n" +
            "\t\t\torientation: ((theta0 < theta1) ^ \n" +
            "flip1) == 1,\n" +
            "\t\t\tdist: dt1\n" +
            "\t\t};\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "function fix_angle(theta) {\n" +
            "\treturn theta < 0 ? 360 + theta : theta;\n" +
            "}\n" +
            "\n" +
            "/*____\t_____\t____\t_____\t____ \n" +
            " (\t_ \\(\t_\t)(\t_ \\(\t_\t)(_\t_)\n" +
            "\t)\t / )(_)(\t) _ < )(_)(\t )(\t\n" +
            " (_)\\_)(_____)(____/(_____) (__) \n" +
            "*/\n" +
            "\n" +
            "function button_presser(dist) {\n" +
            "\treturn function() {\n" +
            "\t\treturn build_button_presser(dist);\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function strafe_power(direction) {\n" +
            "\treturn function() {\n" +
            "\t\tset_run_using_encoder();\n" +
            "\t\tvar ptr = direction;\n" +
            "\t\tforAllMotors(function(m) {\n" +
            "\t\t\tm.setPower(ptr); ptr=-ptr;\n" +
            "\t\t});\n" +
            "\t\tnext();\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function rotate_power(power) {\n" +
            "\treturn function() {\n" +
            "\t\tset_run_using_encoder();\n" +
            "\t\tforAllMotors(function(m) {\n" +
            "\t\t\tm.setPower(power);\n" +
            "\t\t});\n" +
            "\t\tnext();\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function drive_power(power) {\n" +
            "\treturn function() {\n" +
            "\t\tset_run_using_encoder();\n" +
            "\t\tvar ptr = power;\n" +
            "\t\tvar counter = 0;\n" +
            "\t\tforAllMotors(function(m) {\n" +
            "\t\t\tm.setPower(ptr);\n" +
            "\t\t\tif(++counter == 2) {\n" +
            "\t\t\t\tptr = -ptr;\n" +
            "\t\t\t}\n" +
            "\t\t});\n" +
            "\t\tnext();\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function driven_forward(ticks) {\n" +
            "\treturn function() {\n" +
            "\t\tvar lbc = left_back.getCurrentPosition();\n" +
            "\t\tvar lfc = left_front.getCurrentPosition();\n" +
            "\t\tvar rbc = right_back.getCurrentPosition();\n" +
            "\t\tvar rfc = right_front.getCurrentPosition(); \n" +
            "//lf rbc\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar ret = \n" +
            "Math.abs(left_back.getCurrentPosition() - lbc) >= ticks &&\n" +
            "\t\t\t\t\n" +
            "Math.abs(left_front.getCurrentPosition() - lfc) >= ticks &&\n" +
            "\t\t\t\t\n" +
            "Math.abs(right_back.getCurrentPosition() - rbc) >= ticks &&\n" +
            "\t\t\t\t\n" +
            "Math.abs(right_front.getCurrentPosition() - rfc) >= ticks;\n" +
            "\t\t\tif(ret) next();\n" +
            "\t\t\treturn ret;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function build_button_presser(dist) { //dist is in milliseconds. positive = out, negative = in\n" +
            "\tdist = -dist; //so that positive == out\n" +
            "\tarm.setPower(dist > 0 ? 1 : -1);\n" +
            "\tvar oldTime = (new Date()).getTime();\n" +
            "\treturn function() {\n" +
            "\t\tvar currentTime = (new Date()).getTime();\n" +
            "\t\tvar ret = (currentTime-oldTime) >= \n" +
            "Math.abs(dist);\n" +
            "\t\tif(ret) {\n" +
            "\t\t\tnext();\n" +
            "\t\t\tarm.setPower(0);\n" +
            "\t\t}\n" +
            "\t\treturn ret;\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function touching_white() {\n" +
            "\treturn function() {\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn bottom.red() > 10;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function exit() {\n" +
            "\treturn function() {\n" +
            "\t\tjmp(-1);\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function near_beacon() {\n" +
            "\treturn function() {\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar ret = colors.red() >= 1 ||\n" +
            "\t\t\t\t  colors.blue() >= 1;\n" +
            "\t\t\tif(ret) server.print(\"--NEAR--\");\n" +
            "\t\t\treturn ret;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function beacon_color_unknown() {\n" +
            "\treturn function() {\n" +
            "\t\treturn function() {\n" +
            "\t\t\tserver.print('    blue: ' + \n" +
            "colors.blue());\n" +
            "\t\t\tserver.print('    red : ' + \n" +
            "colors.red());\n" +
            "\t\t\treturn colors.blue() == colors.red();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function sleep(sec) {\n" +
            "\treturn function() {\n" +
            "\t\tvar start = (new Date()).getTime();\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar cur = (new Date()).getTime();\n" +
            "\t\t\tvar ret = (cur-start) >= sec;\n" +
            "\t\t\tif(ret) next();\n" +
            "\t\t\treturn ret;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function wait(sec) {\n" +
            "\treturn function() {\n" +
            "\t\tvar start = (new Date()).getTime();\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar cur = (new Date()).getTime();\n" +
            "\t\t\tvar ret = (cur-start) >= sec;\n" +
            "\t\t\treturn ret;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function beacon_blue() {\n" +
            "\treturn function() {\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn colors.blue() > colors.red();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function beacon_red() {\n" +
            "\treturn function() {\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn colors.red() > colors.blue();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function enable_led(value) {\n" +
            "\treturn function() {\n" +
            "\t\tbottom.enableLed(value);\n" +
            "\t\tnext();\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function enable_top_led(value) {\n" +
            "\treturn function() {\n" +
            "\t\tcolors.enableLed(value);\n" +
            "\t\tnext();\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "//todo cancel the fade crap, just float\n" +
            "//and then get the rest done\n" +
            "var fadescale = 1/Math.log(11);\n" +
            "function fade_in_exp(current, max, fade) {\n" +
            "\tif(current < 0 || current > max) {\n" +
            "\t\treturn 0;\n" +
            "\t} else if(current < fade) {\n" +
            "\t\t//ease in\n" +
            "\t\treturn fadescale*Math.log(10*(current/fade)+1);\n" +
            "\t} else if(current > max-fade) {\n" +
            "\t\t//ease out\n" +
            "\t\tvar x = (1-(current-(max-fade)));\n" +
            "\t\treturn fadescale*Math.log(10*(x/fade)+1);\n" +
            "\t} else {\n" +
            "\t\treturn 1;\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "function fade_in_linear(current, max, fade) {\n" +
            "\tif(current < 0 || current > max) {\n" +
            "\t\treturn 0;\n" +
            "\t} else if(current < fade) {\n" +
            "\t\treturn current/fade;\n" +
            "\t} else if(current > max-fade) {\n" +
            "\t\treturn (fade-(current-(max-fade)))/fade;\n" +
            "\t} else {\n" +
            "\t\treturn 1;\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "function set_run_to_position() {\n" +
            "\tzero_motors();\n" +
            "\tforAllMotors(function(self) {\n" +
            "\t\t\n" +
            "self.setMode(hardware.DcMotor.RunMode.RUN_TO_POSITION);\n" +
            "\t});\n" +
            "}\n" +
            "\n" +
            "function set_run_using_encoder() {\n" +
            "\tzero_motors();\n" +
            "\tforAllMotors(function(self) {\n" +
            "\t\t\n" +
            "self.setMode(hardware.DcMotor.RunMode.RUN_USING_ENCODER);\n" +
            "\t});\n" +
            "}\n" +
            "\n" +
            "function drive(ticks, run) {\n" +
            "\treturn function() {\n" +
            "\t\treturn build_command(ticks, ticks, -ticks, \n" +
            "-ticks, run);\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function strafe(ticks) {\n" +
            "\treturn function() {\n" +
            "\t\treturn build_command(ticks, -ticks, ticks, \n" +
            "-ticks);\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function rotate(angle) {\n" +
            "\tvar turnAmount = ninety_degree_angle/90 * angle;\n" +
            "\treturn function() {\n" +
            "\t\treturn build_command(turnAmount, turnAmount, \n" +
            "turnAmount, turnAmount);\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "/*function build_command(lb, lf, rb, rf) {\n" +
            "\tforAllMotors(function(motor) {\n" +
            "\t\t\n" +
            "motor.setMode(hardware.DcMotor.RunMode.RUN_TO_POSITION);\n" +
            "\t});\n" +
            "\t//c = current\n" +
            "\tvar lbc = left_back.getCurrentPosition();\n" +
            "\tvar lfc = left_front.getCurrentPosition();\n" +
            "\tvar rbc = right_back.getCurrentPosition();\n" +
            "\tvar rfc = right_front.getCurrentPosition();\n" +
            "\tleft_back.setTargetPosition(lb + lbc);\n" +
            "\tleft_front.setTargetPosition(lf + lfc);\n" +
            "\tright_back.setTargetPosition(rb + rbc);\n" +
            "\tright_front.setTargetPosition(rf + rfc);\n" +
            "\t//p = power\n" +
            "\tvar power = 1; //max\n" +
            "\tvar lfp = (lf > 0 ? power : -power);\n" +
            "\tvar lbp = (lb > 0 ? power : -power);\n" +
            "\tvar rbp = (rb > 0 ? power : -power);\n" +
            "\tvar rfp = (rf > 0 ? power : -power);\n" +
            "\t//a = abs\n" +
            "\tvar lba = Math.abs(lb);\n" +
            "\tvar lfa = Math.abs(lf);\n" +
            "\tvar rba = Math.abs(rb);\n" +
            "\tvar rfa = Math.abs(rf);\n" +
            "\tvar fade_in_div = 3;\n" +
            "\t//fi = fade in\n" +
            "\tvar lbfi = lba/fade_in_div;\n" +
            "\tvar lffi = lfa/fade_in_div;\n" +
            "\tvar rbfi = rba/fade_in_div;\n" +
            "\tvar rffi = rfa/fade_in_div;\n" +
            "\tvar minPower = 0.4;\n" +
            "\tvar scale = 1-minPower;\n" +
            "\treturn function() {\n" +
            "\t\t//d = difference\n" +
            "\t\tvar lbd = \n" +
            "Math.abs(left_back.getCurrentPosition() - lbc);\n" +
            "\t\tvar lfd = \n" +
            "Math.abs(left_front.getCurrentPosition() - lfc);\n" +
            "\t\tvar rbd = \n" +
            "Math.abs(right_back.getCurrentPosition() - rbc);\n" +
            "\t\tvar rfd = \n" +
            "Math.abs(right_front.getCurrentPosition() - rfc);\n" +
            "\t\t//server.print('fade_in(' + lbd + ', ' + lba + \n" +
            "', ' + lbfi + ')');\n" +
            "\t\t//server.print('fade ' + fade_in_linear(lbd, \n" +
            "lba, lbfi));\n" +
            "\t\t\n" +
            "left_back.setPower(lbp*(scale*(fade_in_linear(lbd, lba, \n" +
            "lbfi))+minPower));\n" +
            "\t\t\n" +
            "left_front.setPower(lfp*(scale*(fade_in_linear(lfd, lfa, \n" +
            "lffi))+minPower));\n" +
            "\t\t\n" +
            "right_back.setPower(rbp*(scale*(fade_in_linear(rbd, rba, \n" +
            "rbfi))+minPower));\n" +
            "\t\t\n" +
            "right_front.setPower(rfp*(scale*(fade_in_linear(rfd, rfa, \n" +
            "rffi))+minPower));\n" +
            "\t\tvar res = lbd >= lba &&\n" +
            "\t\t\t\t lfd >= lfa &&\n" +
            "\t\t\t\t rbd >= rba &&\n" +
            "\t\t\t\t rfd >= rfa;\n" +
            "\t\tif(res) {\n" +
            "\t\t\tnext();\n" +
            "\t\t}\n" +
            "\t\treturn res;\n" +
            "\t};\n" +
            "}*/\n" +
            "\n" +
            "function build_command(lb, lf, rb, rf, zero) {\n" +
            "\tif(typeof zero === 'undefined' || zero) {\n" +
            "\t\tzero_motors();\n" +
            "\t}\n" +
            "\tforAllMotors(function(motor) {\n" +
            "\t\t\n" +
            "motor.setMode(hardware.DcMotor.RunMode.RUN_TO_POSITION);\n" +
            "\t});\n" +
            "\tvar lbc = left_back.getCurrentPosition();\n" +
            "\tvar lfc = left_front.getCurrentPosition();\n" +
            "\tvar rbc = right_back.getCurrentPosition();\n" +
            "\tvar rfc = right_front.getCurrentPosition();\n" +
            "\tleft_back.setTargetPosition(lb + lbc);\n" +
            "\tleft_front.setTargetPosition(lf + lfc);\n" +
            "\tright_back.setTargetPosition(rb + rbc);\n" +
            "\tright_front.setTargetPosition(rf + rfc);\n" +
            "\tvar power = 0.5;\n" +
            "\tleft_back.setPower(lb > 0 ? power : -power);\n" +
            "\tleft_front.setPower(lf > 0 ? power : -power);\n" +
            "\tright_back.setPower(rb > 0 ? power : -power);\n" +
            "\tright_front.setPower(rf > 0 ? power : -power);\n" +
            "\treturn function() {\n" +
            "\t\tleft_back.setPower(lb > 0 ? power : -power);\n" +
            "\t\tleft_front.setPower(lf > 0 ? power : -power);\n" +
            "\t\tright_back.setPower(rb > 0 ? power : -power);\n" +
            "\t\tright_front.setPower(rf > 0 ? power : -power);\n" +
            "\t\tvar lbd = \n" +
            "Math.abs(left_back.getCurrentPosition() - lbc);\n" +
            "\t\tvar lfd = \n" +
            "Math.abs(left_front.getCurrentPosition() - lfc);\n" +
            "\t\tvar rbd = \n" +
            "Math.abs(right_back.getCurrentPosition() - rbc);\n" +
            "\t\tvar rfd = \n" +
            "Math.abs(right_front.getCurrentPosition() - rfc);\n" +
            "\t\t\n" +
            "\t\tvar arr = [Math.abs(lbd - Math.abs(lb)) < 10,\n" +
            "\t\t\t  Math.abs(lfd - Math.abs(lf)) < 10,\n" +
            "\t\t\t  Math.abs(rbd - Math.abs(rb)) < 10,\n" +
            "\t\t\t  Math.abs(rfd - Math.abs(rf)) < 10];\n" +
            "\t\tvar ctr = 0;\n" +
            "\t\tif(arr[0]) ctr++;\n" +
            "\t\tif(arr[1]) ctr++;\n" +
            "\t\tif(arr[2]) ctr++;\n" +
            "\t\tif(arr[3]) ctr++;\n" +
            "\n" +
            "\t\tret = ctr > 2; //greater than 2 wheels made it\n" +
            "\t\tif(ret) {\n" +
            "\t\t\t//server.print(\"left_back desired: \" + (lb + lbc) + \" current: \" + left_back.getCurrentPosition());\n" +
            "\t\t\tnext();\n" +
            "\t\t}\n" +
            "\t\treturn ret;\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "/*___\t_____\t_\t_\t____\t____\t_____\t__\t \n" +
            " / __)(\t_\t)( \\( )(_\t_)(\t_ \\(\t_\t)(\t\n" +
            ")\t\n" +
            "( (__\t)(_)(\t)\t(\t )(\t )\t / )(_)(\t\n" +
            ")(__ \n" +
            " \\___)(_____)(_)\\_) (__) (_)\\_)(_____)(____)\n" +
            "*/\n" +
            "function init() {\n" +
            "\t//get motors\n" +
            "\tleft_back = hardwareMap.dcMotor.get(\"left_back\");\n" +
            "\tleft_front = hardwareMap.dcMotor.get(\"left_front\");\n" +
            "\tright_back = hardwareMap.dcMotor.get(\"right_back\");\n" +
            "\tright_front = hardwareMap.dcMotor.get(\"right_front\");\n" +
            "\t\n" +
            "\t//get & initialize color sensors\n" +
            "\tcolors = hardwareMap.colorSensor.get(\"colors\");\n" +
            "\tvar addr = new hardware.I2cAddr.create8bit(0x6c);\n" +
            "\tcolors.setI2cAddress(addr);\n" +
            "\t\n" +
            "\tbottom = hardwareMap.colorSensor.get(\"bottom\");\n" +
            "\tvar baddr = new hardware.I2cAddr.create8bit(0x3c);\n" +
            "\tbottom.setI2cAddress(baddr);\n" +
            "\tcolors.enableLed(false);\n" +
            "\tbottom.enableLed(false);\n" +
            "\n" +
            "\t//get range sensor\n" +
            "\t//range = hardwareMap.get(\"range\");\n" +
            "\t//var caddr = new hardware.I2cAddr.create8bit(0x9c);\n" +
            "\t//range.setI2cAddress(caddr);\n" +
            "\t\n" +
            "\t//get & initialize servos\n" +
            "\tarm = hardwareMap.crservo.get(\"button_presser\");\n" +
            "\tlatch = hardwareMap.crservo.get(\"latch\");\n" +
            "\tarm.setPower(0);\n" +
            "\tlatch.setPower(0);\n" +
            "\tnavx = \n" +
            "AHRS.getInstance(hardwareMap.deviceInterfaceModule.get(\"navx_controller\"), \n" +
            "0, AHRS.DeviceDataType.kProcessedData);\n" +
            "\t\n" +
            "\t//initialize motors\n" +
            "\tforAllMotors(function(self) {\n" +
            "\t\t\n" +
            "self.setZeroPowerBehavior(hardware.DcMotor.ZeroPowerBehavior.FLOAT);\n" +
            "\t});\n" +
            "\tresetMotors();\n" +
            "\twhile(left_back.getCurrentPosition() != 0) {\n" +
            "\t\tserver.print(\"Initializing...\");\n" +
            "\t}\n" +
            "\tset_run_using_encoder();\n" +
            "\tcommandIndex = 0;\n" +
            "}\n" +
            "\n" +
            "function start() {\n" +
            "\tprocess();\n" +
            "}\n" +
            "\n" +
            "var currentCommand = null;\n" +
            "var dead = false;\n" +
            "//var cpptr = 0;\n" +
            "function loop() {\n" +
            "\tif(dead) return;\n" +
            "\t\n" +
            "\t//handle current command if it wants us to check up on it\n" +
            "\tif(currentCommand !== null) {\n" +
            "\t\tif(currentCommand()) {\n" +
            "\t\t\tcurrentCommand = null;\n" +
            "\t\t}\n" +
            "\t\treturn;\n" +
            "\t}\n" +
            "\t\n" +
            "\tvar cmd = commands[commandIndex];\n" +
            "\tif(typeof cmd == 'undefined') {\n" +
            "\t\tstop();\n" +
            "\t\tserver.print(\"Dead\");\n" +
            "\t\tdead = true;\n" +
            "\t\treturn;\n" +
            "\t}\n" +
            "\tvar cmdres = cmd();\n" +
            "\t//cpptr++;\n" +
            "\t//if(cpptr % 50 == 0) {\n" +
            "\t//\tserver.print(typeof cmdres);\n" +
            "\t//}\n" +
            "\tif(typeof cmdres == 'function') {\n" +
            "\t\tcurrentCommand = cmdres;\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "function stop() {\n" +
            "\tzero_motors();\n" +
            "\tarm.setPower(0);\n" +
            "\tcolors.enableLed(false);\n" +
            "\tbottom.enableLed(false);\n" +
            "\tset_run_using_encoder();\n" +
            "}\n" +
            "\n" +
            "//processor: not strictly control\n" +
            "function process() {\n" +
            "\tvar loops = {};\n" +
            "\t//log all loops\n" +
            "\tfor(var i = 0; i < commands.length; i++) {\n" +
            "\t\tif(commands[i].name == 'loop_anon') {\n" +
            "\t\t\tloops[commands[i].loopname] = i;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t//find matching ends and connect them with loops\n" +
            "\tfor(i = 0; i < commands.length; i++) {\n" +
            "\t\tif(commands[i].name == 'end_anon') {\n" +
            "\t\t\tcommands[i].jumpindex = \n" +
            "loops[commands[i].loopname];\n" +
            "\t\t\t\n" +
            "commands[loops[commands[i].loopname]].jumpindex = i+1;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t//make log of all ifs, elses, and fis\n" +
            "\tvar ifs = {};\n" +
            "\tvar elses = {};\n" +
            "\tvar fis = {};\n" +
            "\tfor(i = 0; i < commands.length; i++) {\n" +
            "\t\tif(commands[i].name == 'if_anon') {\n" +
            "\t\t\tifs[commands[i].ifname] = i;\n" +
            "\t\t} else if(commands[i].name == 'else_anon') {\n" +
            "\t\t\telses[commands[i].elsename] = i;\n" +
            "\t\t} else if(commands[i].name == 'fi_anon') {\n" +
            "\t\t\tfis[commands[i].finame] = i;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "\t\n" +
            "\t//loop through ifs\n" +
            "\tfor(var key in ifs) {\n" +
            "\t\t//skip loop if the property is from prototype\n" +
            "\t\tif(!ifs.hasOwnProperty(key)) continue;\n" +
            "\t\t\n" +
            "\t\tif(typeof elses[key] !== 'undefined') {\n" +
            "\t\t\t//there is a corresponding else\n" +
            "\t\t\tcommands[ifs[key]].jumpindex = \n" +
            "elses[key]+1;\n" +
            "\t\t\tif(typeof fis[key] !== 'undefined') {\n" +
            "\t\t\t\tcommands[elses[key]].jumpindex \n" +
            "= fis[key]+1;\n" +
            "\t\t\t} else {\n" +
            "\t\t\t\tthrow(\"Unmatched if/else with fi.\");\n" +
            "\t\t\t}\n" +
            "\t\t} else if(typeof fis[key] !== 'undefined') {\n" +
            "\t\t\t//there is a corresponding fi\n" +
            "\t\t\tcommands[ifs[key]].jumpindex = \n" +
            "fis[key]+1;\n" +
            "\t\t} else {\n" +
            "\t\t\tthrow(\"Unmatched if with fi\");\n" +
            "\t\t}\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "/*__\t__\t_____\t____\t_____\t____\t\t__\t\n" +
            "__\t____\t____\t__\t \n" +
            " (\t\\/\t)(\t_\t)(_\t_)(\t_\t)(\t\n" +
            "_ \\\t(\t)(\t)(_\t_)(_\t_)(\t)\t\n" +
            "\t)\t\t(\t)(_)(\t )(\t )(_)(\t)\t \n" +
            "/\t )(__)(\t )(\t _)(_\t)(__ \n" +
            " (_/\\/\\_)(_____) (__) (_____)(_)\\_)\t(______) (__) \n" +
            "(____)(____)\n" +
            "*/\n" +
            "function forAllMotors(operation) {\n" +
            "\toperation(left_back);\n" +
            "\toperation(left_front);\n" +
            "\toperation(right_back);\n" +
            "\toperation(right_front);\n" +
            "}\n" +
            "\n" +
            "function resetMotors() {\n" +
            "\tarm.setPower(0);\n" +
            "\tforAllMotors(function(self) {\n" +
            "\t\t\n" +
            "self.setMode(hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER);\n" +
            "\t});\n" +
            "\tresetting = true;\n" +
            "}\n" +
            "\n" +
            "function zero_motors() {\n" +
            "\tforAllMotors(function(m) {\n" +
            "\t\tm.setPower(0);\n" +
            "\t});\n" +
            "}\n" +
            "\n" +
            "/*___\t ___\t____\t____\t____\t____\t\t__\t\n" +
            "__\t____\t____\t__\t\t___ \n" +
            " / __) / __)(\t_ \\(_\t_)(\t_ \\(_\t_)\t(\t)(\t\n" +
            ")(_\t_)(_\t_)(\t)\t/ __)\n" +
            " \\__ \\( (__\t)\t / _)(_\t)___/\t)(\t\t )(__)(\t \n" +
            ")(\t _)(_\t)(__ \\__ \\\n" +
            " (___/ \\___)(_)\\_)(____)(__)\t (__)\t (______) (__) \n" +
            "(____)(____)(___/\n" +
            "*/\n" +
            "function next() {\n" +
            "\tcommandIndex++;\n" +
            "}\n" +
            "\n" +
            "function not(thing) {\n" +
            "\treturn function() {\n" +
            "\t\tvar part = thing();\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar ret = !part();\n" +
            "\t\t\treturn ret;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function run_once(tag, command) {\n" +
            "\trun_once_db[tag] = false;\n" +
            "\treturn function() {\n" +
            "\t\tif(run_once_db[tag] == false) {\n" +
            "\t\t\trun_once_db[tag] = true;\n" +
            "\t\t\tvar ret = command();\n" +
            "\t\t\tif(typeof ret !== 'undefined') {\n" +
            "\t\t\t\treturn ret;\n" +
            "\t\t\t}\n" +
            "\t\t} else {\n" +
            "\t\t\tnext();\n" +
            "\t\t}\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function or(thing1, thing2) {\n" +
            "\treturn function() {\n" +
            "\t\tvar p1 = thing1();\n" +
            "\t\tvar p2 = thing2();\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn p1() || p2();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function less(thing1, thing2) {\n" +
            "\treturn function() {\n" +
            "\t\tvar p1 = thing1();\n" +
            "\t\tvar p2 = thing2();\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn p1() < p2();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function greater(thing1, thing2) {\n" +
            "\treturn function() {\n" +
            "\t\tvar p1 = thing1();\n" +
            "\t\tvar p2 = thing2();\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn p1() < p2();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function and(thing1, thing2) {\n" +
            "\treturn function() {\n" +
            "\t\tvar p1 = thing1();\n" +
            "\t\tvar p2 = thing2();\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn p1() && p2();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function make_predicate(thing) {\n" +
            "\treturn function() {\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn thing();\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function output(str) {\n" +
            "\treturn function() {\n" +
            "\t\tserver.print(str);\n" +
            "\t\tnext();\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function jmp(ind) {\n" +
            "\tcommandIndex = ind;\n" +
            "}\n" +
            "\n" +
            "function times(num) {\n" +
            "\treturn function() {\n" +
            "\t\tvar ctr = 0;\n" +
            "\t\treturn function() {\n" +
            "\t\t\treturn ctr++ < num;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function _while(str, condition, run_first_time) {\n" +
            "\tif(typeof run_first_time == 'undefined') {\n" +
            "\t\trun_first_time = false;\n" +
            "\t}\n" +
            "\tvar ret = function loop_anon() {\n" +
            "\t\tret.check = condition();\n" +
            "\t\tif(run_first_time) {\n" +
            "\t\t\tnext();\n" +
            "\t\t} else {\n" +
            "\t\t\tif(ret.check() === true) {\n" +
            "\t\t\t\tnext();\n" +
            "\t\t\t} else {\n" +
            "\t\t\t\t\n" +
            "jmp(commands[commandIndex].jumpindex);\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\tret.loopname = str;\n" +
            "\treturn ret;\n" +
            "}\n" +
            "\n" +
            "function _if(str, condition) {\n" +
            "\tvar ret = function if_anon() {\n" +
            "\t\tret.check = condition();\n" +
            "\t\tif(ret.check() === true) {\n" +
            "\t\t\tnext();\n" +
            "\t\t} else {\n" +
            "\t\t\tjmp(commands[commandIndex].jumpindex);\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\tret.ifname = str;\n" +
            "\treturn ret;\n" +
            "}\n" +
            "\n" +
            "function _else(str) {\n" +
            "\tvar ret = function else_anon() {\n" +
            "\t\tjmp(commands[commandIndex].jumpindex);\n" +
            "\t};\n" +
            "\tret.elsename = str;\n" +
            "\treturn ret;\n" +
            "}\n" +
            "\n" +
            "function _fi(str) {\n" +
            "\tvar ret = function fi_anon() {\n" +
            "\t\tnext();\n" +
            "\t};\n" +
            "\tret.finame = str;\n" +
            "\treturn ret;\n" +
            "}\n" +
            "\n" +
            "function _done(str) {\n" +
            "\tvar ret = function end_anon() {\n" +
            "\t\tvar loopbegin = \n" +
            "commands[commandIndex].jumpindex;\n" +
            "\t\tvar checker = commands[loopbegin].check;\n" +
            "\t\tif(checker !== undefined) {\n" +
            "\t\t\tif(!checker()) {\n" +
            "\t\t\t\tnext();\n" +
            "\t\t\t} else {\n" +
            "\t\t\t\tjmp(loopbegin+1);\n" +
            "\t\t\t}\n" +
            "\t\t} else {\n" +
            "\t\t\tjmp(loopbegin);\n" +
            "\t\t}\n" +
            "\t};\n" +
            "\tret.loopname = str;\n" +
            "\treturn ret;\n" +
            "}\n" +
            "\n" +
            "function run_predicate(predicate) {\n" +
            "\treturn predicate()();\n" +
            "}\n" +
            "\n" +
            "function wait_until(predicate) {\n" +
            "\treturn function() {\n" +
            "\t\tvar p = predicate();\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar ret = p();\n" +
            "\t\t\tif(ret) next();\n" +
            "\t\t\treturn ret;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "function reset_motors() {\n" +
            "\treturn function() {\n" +
            "\t\tforAllMotors(function(self) {\n" +
            "\t\t\t\n" +
            "self.setMode(hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER);\n" +
            "\t\t});\n" +
            "\t\treturn function() {\n" +
            "\t\t\tvar ret = \n" +
            "left_back.getCurrentPosition() == 0;\n" +
            "\t\t\tif(ret) {\n" +
            "\t\t\t\tnext();\n" +
            "\t\t\t}\n" +
            "\t\t\treturn ret;\n" +
            "\t\t};\n" +
            "\t};\n" +
            "}\n" +
            "\n" +
            "//FOR THE CONSOLE\n" +
            "function t(power) {\n" +
            "\tforAllMotors(function(m) {\n" +
            "\t\tm.setPower(power);\n" +
            "\t});\n" +
            "}\n" +
            "\n" +
            "function s(direction) {\n" +
            "\tvar ptr = direction;\n" +
            "\tforAllMotors(function(m) {\n" +
            "\t\tm.setPower(ptr); ptr=-ptr;\n" +
            "\t});\n" +
            "}\n" +
            "\n" +
            "function f(power) {\n" +
            "\tvar counter = 0;\n" +
            "\tvar ptr = power;\n" +
            "\tforAllMotors(function(m) {\n" +
            "\t\tm.setPower(ptr);\n" +
            "\t\tif(++counter == 2) {\n" +
            "\t\t\tptr = -ptr;\n" +
            "\t\t}\n" +
            "\t});\n" +
            "}\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n";
}
