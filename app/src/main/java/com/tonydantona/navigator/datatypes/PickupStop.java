package com.tonydantona.navigator.datatypes;

import com.tonydantona.navigator.datatypes.Immutables.StopType;

public class PickupStop extends NavStop {
    private double spLat;
    private double spLong;
    private String pickupPoint;
    private String shipperName;
    private String shipperNumber;

    // constructor
    public PickupStop() {
        super();
        setStopType(StopType.PICKUP);
    }

    // abstract method implementation
    public int getUOWCount() {
        return 1;
    }

    // public methods
    //<editor-fold desc="getters and setters">
    public double getSPLat() {
        return spLat;
    }

    public void setSPLat(double spLat) {
        this.spLat = spLat;
    }

    public double getSPLong() {
        return spLong;
    }

    public void setSPLong(double spLong) {
        this.spLong = spLong;
    }

    public String getPickupPoint() {
        return pickupPoint;
    }

    public void setPickupPoint(String pickupPoint) {
        this.pickupPoint = pickupPoint;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public String getShipperNumber() {
        return shipperNumber;
    }

    public void setShipperNumber(String shipperNumber) {
        this.shipperNumber = shipperNumber;
    }
    //</editor-fold>
}
