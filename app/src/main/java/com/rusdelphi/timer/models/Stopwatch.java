package com.rusdelphi.timer.models;

import java.io.Serializable;

/**
 * Created by Nikita on 12.05.2017.
 */

public class Stopwatch implements Serializable {
    private int id;
    private String name;
    private long currentPeriod;
    private long startTime;
    private long timeDelay;
    private String type;
    private boolean paused = false;
    private boolean running = false;

    public Stopwatch() {
    }

    public Stopwatch(String name, long currentPeriod, long startTime, String type, int counter) {
        this.name = name;
        this.currentPeriod = currentPeriod;
        this.startTime = startTime;
        this.type = type;
        this.id = counter;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCurrentPeriod(long currentPeriod) {
        this.currentPeriod = currentPeriod;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentPeriod() {
        return currentPeriod;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getTimeDelay() {
        return timeDelay;
    }

    public void setTimeDelay(long timeDelay) {
        this.timeDelay = timeDelay;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getId() {
        return id;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
