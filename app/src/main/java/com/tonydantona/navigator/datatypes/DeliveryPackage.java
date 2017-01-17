package com.tonydantona.navigator.datatypes;

import com.tonydantona.navigator.datatypes.Immutables.PackageStatus;

public class DeliveryPackage {
    private int packageID;
    private String trackingNum;
    private String rightSideHIN;
    private PackageStatus packageStatus;

    // constructor
    public DeliveryPackage() {
        super();
        packageID = 0;
        packageStatus = PackageStatus.PENDING;
    }

    // public methods
    //<editor-fold desc="getters and setters">
    public int getPackageID() {
        return packageID;
    }

    public void setPackageID(int packageID) {
        this.packageID = packageID;
    }

    public PackageStatus getPackageStatus() {
        return packageStatus;
    }

    public void setPackageStatus(PackageStatus packageStatus) {
        this.packageStatus = packageStatus;
    }

    public String getTrackingNum() {
        return trackingNum;
    }

    public void setTrackingNum(String trackingNum) {
        this.trackingNum = trackingNum;
    }

    public String getRightSideHIN() {
        return rightSideHIN;
    }

    public void setRightSideHIN(String rightSideHIN) {
        this.rightSideHIN = rightSideHIN;
    }
    //</editor-fold>
}
