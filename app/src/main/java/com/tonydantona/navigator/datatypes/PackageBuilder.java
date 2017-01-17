package com.tonydantona.navigator.datatypes;

import android.util.Log;

import com.tonydantona.navigator.datatypes.Immutables.StopType;

import java.util.List;

public class PackageBuilder {
    private NavRoute route;
    private int packageID;
    private int stopID;
    private Consignee plannedConsignee;
    private Consignee consignee;
    private String trackingNumber;
    private String rightSideHIN;

    // constructor
    public PackageBuilder(NavRoute route) {
        super();
        this.route = route;
        packageID = 0;
        plannedConsignee = null;
        consignee = null;
    }

    // public methods
    public void setResult() {
        NavStop stop = null;

        // get stopID stopType
        StopType stopType = null;

        boolean stopFound = false;
        for(NavStop routeStop : route.getAllStopList()) {
            if(routeStop.getStopID() == stopID) {
                stopFound = true;
                stop = routeStop;
                stopType = routeStop.getStopType();
            }
        }

        if(!stopFound) {
            Log.e("PackageBuilder", "StopID not found for package (" + packageID + ")");
            return;
        }

        // check plannedConsignee and consignee lists, and add package (and consignee if not present already)
        if(plannedConsignee != null) {
            List<Consignee> plannedConsigneeList = ((DeliveryStop) stop).getPlannedConsigneeList();
            checkDeliveryConsigneeList(plannedConsignee, plannedConsigneeList);
        }
        if(consignee != null) {
            List<Consignee> consigneeList = ((DeliveryStop) stop).getConsigneeList();
            checkDeliveryConsigneeList(consignee, consigneeList);
        }
    }

    public boolean isBuilt() {
        return (packageID == 0);
    }

    //<editor-fold desc="DeliveryPackage and PickupStop setters">
    public void setPackageID(int packageID) {
        this.packageID = packageID;
    }

    public void setStopID(int stopID) {
        this.stopID = stopID;
    }

    public void setPlannedConsignee(String plannedConsigneeStr) {
        plannedConsignee = new Consignee(plannedConsigneeStr);
    }

    public void setConsignee(String consigneeStr) {
        consignee = new Consignee(consigneeStr);
    }
    //</editor-fold>

    //<editor-fold desc="DeliveryPackage setters">
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setRightSideHIN(String rightSideHIN) {
        this.rightSideHIN = rightSideHIN;
    }
    //</editor-fold>

    // private methods
    private void checkDeliveryConsigneeList(Consignee consignee, List<Consignee> consigneeList) {
        DeliveryPackage deliveryPackage = new DeliveryPackage();
        deliveryPackage.setTrackingNum(trackingNumber);
        deliveryPackage.setRightSideHIN(rightSideHIN);
        
        boolean consigneeFound = false;
        for(Consignee routeConsignee : consigneeList) {
            if(consignee.getName().equalsIgnoreCase(routeConsignee.getName())) {
                routeConsignee.getDeliveryPackageList().add(deliveryPackage);
                consigneeFound = true;
            }
        }
        if(!consigneeFound) {
            consignee.getDeliveryPackageList().add(deliveryPackage);
            consigneeList.add(consignee);
        }
    }
}
