package com.avinash.syncopyproject.Model;

public class History {

    private String clip;
    private boolean history;
    private long time;

    public History() {
    }

    public History(String clip, boolean history, long time) {
        this.clip = clip;
        this.history = history;
        this.time = time;
    }

    public String getClip() {
        return clip;
    }

    public void setClip(String clip) {
        this.clip = clip;
    }

    public boolean isHistory() {
        return history;
    }

    public void setHistory(boolean history) {
        this.history = history;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
