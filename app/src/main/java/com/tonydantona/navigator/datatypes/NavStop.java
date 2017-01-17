package com.tonydantona.navigator.datatypes;

import com.tonydantona.navigator.datatypes.Immutables.StopType;

public abstract class NavStop {
    private StopType stopType;
    private int stopID;
    private double napLat;
    private double napLong;
    private Address address;
    private int ODO;
    private double odoESST;
    private double esst;
    private double lsst;
    private double svcTime;
    private boolean isCompleted;

    // constructor
    public NavStop() {
        super();
        stopID = 0;
        isCompleted = false;
    }

    // abstract methods
    public abstract int getUOWCount();

    // public methods
    //<editor-fold desc="getters and setters">
    public int getStopID() {
        return stopID;
    }

    public void setStopID(int stopID) {
        this.stopID = stopID;
    }

    public double getNAPLat() {
        return napLat;
    }

    public void setNAPLat(double napLat) {
        this.napLat = napLat;
    }

    public double getNAPLong() {
        return napLong;
    }

    public void setNAPLong(double napLong) {
        this.napLong = napLong;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getODO() {
        return ODO;
    }

    public void setODO(int ODO) {
        this.ODO = ODO;
    }

    public double getODOESST() {
        return odoESST;
    }

    public void setODOESST(double odoESST) {
        this.odoESST = odoESST;
    }

    public double getESST() {
        return esst;
    }

    public void setESST(double esst) {
        this.esst = esst;
    }

    public double getLSST() {
        return lsst;
    }

    public void setLSST(double lsst) {
        this.lsst = lsst;
    }

    public double getSvcTime() {
        return svcTime;
    }

    public void setSvcTime(double svcTime) {
        this.svcTime = svcTime;
    }

    public StopType getStopType() {
        return stopType;
    }

    public void setStopType(StopType stopType) {
        this.stopType = stopType;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    //</editor-fold>
}