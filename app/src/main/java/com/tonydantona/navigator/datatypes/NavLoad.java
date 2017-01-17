package com.tonydantona.navigator.datatypes;

import java.util.ArrayList;
import java.util.List;

public class NavLoad {
    private int loadID;
    private String loadName;
    private List<NavStop> stopList;

    // constructor
    public NavLoad() {
        super();
        loadID = 0;
        stopList = new ArrayList<>();
    }

    // public methods
    //<editor-fold desc="getters and setters">
    public int getLoadID() {
        return loadID;
    }

    public void setLoadID(int loadID) {
        this.loadID = loadID;
    }

    public String getLoadName() {
        return loadName;
    }

    public void setLoadName(String loadName) {
        this.loadName = loadName;
    }

    public List<NavStop> getStopList() {
        return stopList;
    }

    public void setStopList(List<NavStop> stopList) {
        this.stopList = stopList;
    }
    //</editor-fold>

    public int getUOWCount() {
        int totalUOWCount = 0;
        for(NavStop stop : stopList) {
            totalUOWCount += stop.getUOWCount();
        }
        return totalUOWCount;
    }
}