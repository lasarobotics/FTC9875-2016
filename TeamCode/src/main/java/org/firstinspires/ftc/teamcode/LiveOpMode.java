package org.firstinspires.ftc.teamcode;

/**
 * Created by Russell on 11/20/2016.
 */

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.io.*;
import java.net.*;

import android.util.Log;
import java.lang.Thread;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.mozilla.javascript.*;

@TeleOp(name = "liveopmode", group = "test")
public class LiveOpMode extends OpMode {
    Socket clientSocket;
    DataOutputStream outToServer;
    Thread thread; //this thread always waits for incoming data from server
    private enum ServerState {
        UNINITIALIZED,
        INITIALIZED
    }
    private volatile ServerState serverState; //only allow modification by variable 'thread'
    //except for stop method

    @Override
    public void init() {
        serverState = ServerState.UNINITIALIZED;
        try {
            clientSocket = new Socket("192.168.1.78", 6789);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            Log.e("Exception", ioe.toString());
        }
    }

    private class ServerInputThread implements Runnable {
        @Override
        public void run() {
            try {
                DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
                while(!Thread.currentThread().isInterrupted()) {
                    //read in command string
                    int bytes = inFromServer.readInt();
                    byte[] data = new byte[bytes];
                    inFromServer.read(data);
                    String value = new String(data, "UTF-8");
                    if(value.equals("init")) {
                        //set serverstate to initialized
                        serverState = ServerState.INITIALIZED;
                    } else if(value.equals("program")) {
                        //read in program data
                        bytes = inFromServer.readInt();
                        data = new byte[bytes];
                        inFromServer.read(data);
                        value = new String(data, "UTF-8");
                    } else if(value.equals("stop")) {
                        //call program STOP

                    } else if(value.equals("start")) {
                        //call program START

                    }
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void loop() {

    }

    @Override
    public void stop() {
        if(thread != null) {
            thread.interrupt();
        }
        if(clientSocket != null && clientSocket.isClosed() == false) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverState = serverState.UNINITIALIZED;
    }
}
