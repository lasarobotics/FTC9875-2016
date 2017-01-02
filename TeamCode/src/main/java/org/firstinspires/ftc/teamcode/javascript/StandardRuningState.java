package org.firstinspires.ftc.teamcode.javascript;

/**
 * Created by Russell on 12/31/2016.
 */

public class StandardRuningState implements RunningState {
    private ThreadSafeData<Boolean> state = new ThreadSafeData<>();

    @Override
    public void start() {
        state.setValue(true);
    }

    @Override
    public void stop() {
        state.setValue(false);
    }

    @Override
    public boolean getState() {
        return state.getValue();
    }
}
