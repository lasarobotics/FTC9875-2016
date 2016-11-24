package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.RhinoException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Russell on 11/23/2016.
 */

@TeleOp(name = "js-server", group = "test")
public class JavascriptServer extends OpMode {
    private enum DataType {
        FULL_SCRIPT,
        ONE_LINE,
        MOTOR_POWER,
        NONE
    }
    private enum DataOutputType {
        LINE_OUTPUT,
        ERROR,
        NONE
    }
    private DataType[] dataTypeValues = DataType.values();
    private DataType intToDataType(int val) {
        if(val < 0) {
            return DataType.NONE;
        } else if(val > dataTypeValues.length) {
            return DataType.NONE;
        } else {
            return dataTypeValues[val];
        }
    }

    private void log(String s) {
        System.out.println(s);
    }

    private static volatile Thread otherThread;
    private static volatile OtherThread otherThreadObj;
    private static final double VERSION_NUMBER = 1.0;
    private static Object lock = new Object();
    private void deleteOtherThread() {
        synchronized(lock) {
            if (otherThread != null) {
                if (otherThread.isAlive()) {
                    otherThread.interrupt();
                    otherThreadObj.closeSocket();
                    try {
                        otherThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                otherThread = null;
            }
        }
    }
    private void createOtherThread() {
        synchronized(lock) {
            deleteOtherThread();
            otherThreadObj = new OtherThread();
            otherThread = new Thread(otherThreadObj);
            otherThread.start();
        }
    }

    private class OtherThread implements Runnable {
        private ServerSocket socket;
        private Socket clientSockets;
        private DataInputStream inFromClient; //no other threads should ever touch this
        private DataOutputStream outToClient; //ditto above
        private Object socketLock = new Object();

        @Override
        public void run() {
            Context cx = Context.enter();

            ScriptableObject scope = cx.initStandardObjects();
            ScriptableObject.putProperty(scope, "gamepad1", Context.javaToJS(gamepad1, scope));
            ScriptableObject.putProperty(scope, "hardwareMap", Context.javaToJS(hardwareMap, scope));

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    createServer();
                    outToClient.writeDouble(VERSION_NUMBER); //handshake
                    while(!Thread.currentThread().isInterrupted()) {
                        //server loop
                        DataType type = intToDataType(inFromClient.readInt());
                        if(type == DataType.NONE) {
                            log("Invalid data. Ending connection.");
                            break; //invalid data coming through
                        }
                        log("Type in: " + type);
                        String bufferStr = inFromClient.readUTF();
                        log("BufferStr in: " + bufferStr);
                        switch(type) {
                            case MOTOR_POWER:
                                double power = inFromClient.readDouble();
                                DcMotor motor = hardwareMap.dcMotor.get(bufferStr);
                                if(motor == null) {
                                    log("Motor not found: " + bufferStr);
                                } else {
                                    motor.setPower(power);
                                }
                                break;
                            case FULL_SCRIPT:
                                try {
                                    cx.evaluateString(scope, bufferStr, "<cmd>", 1, null);
                                } catch(RhinoException re) {
                                    writeErrorToClient(re);
                                    log(re.getMessage());
                                }
                                break;
                            case ONE_LINE:
                                try {
                                    String strOut = String.valueOf(
                                            cx.evaluateString(scope,
                                                    bufferStr,
                                                    "<cmd>",
                                                    1,
                                                    null));
                                    writeStringToClient(DataOutputType.LINE_OUTPUT, strOut);
                                } catch(RhinoException re) {
                                    writeErrorToClient(re);
                                    log(re.getMessage());
                                }
                                break;
                        }
                    }
                } catch(IOException ioe) {
                    //do nothing
                } finally {
                    closeSocket();
                }
            }

            Context.exit();
        }

        private void writeErrorToClient(RhinoException error) throws IOException {
            outToClient.writeInt(DataOutputType.ERROR.ordinal());
            outToClient.writeUTF(error.getMessage());
            outToClient.writeUTF(error.getScriptStackTrace());
            outToClient.writeInt(error.lineNumber());
            outToClient.writeInt(error.columnNumber());
        }

        private void writeStringToClient(DataOutputType type, String str) throws IOException {
            outToClient.writeInt(type.ordinal());
            outToClient.writeUTF(str);
        }

        public void closeSocket() {
            synchronized(socketLock) {
                if(socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch(IOException ioe2) {
                        //we're done
                    }
                }
            }
        }

        private void createServer() throws IOException {
            synchronized(socketLock) {
                socket = new ServerSocket(6789);
            }
            log("Created server. Waiting for connection...");
            Socket client = socket.accept();
            log("Found client.");
            clientSockets = client;
            inFromClient = new DataInputStream(client.getInputStream());
            outToClient = new DataOutputStream(client.getOutputStream());
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {
        createOtherThread();
    }

    @Override
    public void stop() {
        deleteOtherThread();
    }

    @Override
    public void loop() {
        try {
            Thread.sleep(500);
        } catch(InterruptedException ie) {
            log("Interrupted.");
        }
    }
}
