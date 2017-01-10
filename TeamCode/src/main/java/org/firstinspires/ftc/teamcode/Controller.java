package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.HashMap;

public class Controller {
    private Gamepad g;

    // input tolerances
    private float stickTol = 0;
    private float triggerTol = 0;

    // button handling
    public enum Button {
        A, B, X, Y, DPAD_LEFT, DPAD_RIGHT, DPAD_DOWN, DPAD_UP, LEFT_BUMPER, RIGHT_BUMPER, LEFT_TRIGGER, RIGHT_TRIGGER
    }
    private class ButtonState {
        public boolean pressed = false;
        public boolean justPressed = false;
        public boolean justReleased = false;
        public boolean toggled = false;
    }

    private HashMap<Button, ButtonState> states = new HashMap<>();

    public Controller(Gamepad g) {
        this.g = g;
    }

    public void setStickTol(float stickTol) {
        this.stickTol = stickTol;
    }
    public void setTriggerTol(float triggerTol) {
        this.triggerTol = triggerTol;
    }

    // buttons
    public boolean a() {
        return g.a;
    }
    public boolean b() {
        return g.b;
    }
    public boolean x() {
        return g.x;
    }
    public boolean y() {
        return g.y;
    }

    // sticks
    public float left_stick_x() {
        return damp(stickTol, g.left_stick_x);
    }
    public float left_stick_y() {
        return damp(stickTol, g.left_stick_y);
    }
    public float right_stick_x() {
        return damp(stickTol, g.right_stick_x);
    }
    public float right_stick_y() {
        return damp(stickTol, g.right_stick_y);
    }

    // stick buttons
    public boolean left_stick_button() {
        return g.left_stick_button;
    }
    public boolean right_stick_button() {
        return g.right_stick_button;
    }

    // triggers
    public float left_trigger() {
        return damp(triggerTol, g.left_trigger);
    }
    public float right_trigger() {
        return damp(triggerTol, g.right_trigger);
    }

    // bumpers
    public boolean left_bumper() {
        return g.left_bumper;
    }
    public boolean right_bumper() {
        return g.right_bumper;
    }

    // dpad
    public boolean dpad_left() {
        return g.dpad_left;
    }
    public boolean dpad_right() {
        return g.dpad_right;
    }
    public boolean dpad_down() {
        return g.dpad_down;
    }
    public boolean dpad_up() {
        return g.dpad_up;
    }

    private float damp(float tol, float val) {
        return Math.abs(val) > tol ? val : 0;
    }

    // button handling
    public boolean toggled(Button button) {
        return states.get(button).toggled;
    }
    public boolean justPressed(Button button) {
        return states.get(button).justPressed;
    }
    public boolean justReleased(Button button) {
        return states.get(button).justReleased;
    }
    public boolean pressed(Button button) {
        return states.get(button).pressed;
    }

    // begin watching button
    public void register(Button button) {
        states.put(button, new ButtonState());
    }
    // get value of button
    private boolean button(Button button) {
        switch(button) {
            case A:
                return a();
            case B:
                return b();
            case X:
                return x();
            case Y:
                return y();
            case DPAD_LEFT:
                return dpad_left();
            case DPAD_RIGHT:
                return dpad_right();
            case DPAD_DOWN:
                return dpad_down();
            case DPAD_UP:
                return dpad_up();
            case LEFT_BUMPER:
                return left_bumper();
            case RIGHT_BUMPER:
                return right_bumper();
            case LEFT_TRIGGER:
                return left_trigger() > 0;
            case RIGHT_TRIGGER:
                return right_trigger() > 0;
            default:
                return false;
        }
    }
    // update state up button
    private void update(Button button) {
        ButtonState state = states.get(button);
        state.justPressed = false;
        state.justReleased = false;
        boolean pressed = button(button);

        if(state.pressed != pressed) {
            if(pressed) {
                state.justPressed = true;
                state.toggled = !state.toggled;
            } else {
                state.justReleased = true;
            }
        }
        state.pressed = pressed;
    }
    public void update() {
        for(Button button : states.keySet()) {
            update(button);
        }
    }
}
