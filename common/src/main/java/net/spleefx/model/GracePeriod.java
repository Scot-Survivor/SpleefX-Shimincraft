package net.spleefx.model;

public class GracePeriod {

    private boolean enabled = false;
    private int time = 5;

    public boolean isEnabled() {
        return enabled && time > 0;
    }

    public int getTime() {
        return time;
    }
}
