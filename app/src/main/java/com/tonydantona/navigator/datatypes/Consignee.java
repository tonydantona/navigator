package com.tonydantona.navigator.datatypes;

import java.util.List;

public class Consignee {
    private String name;
    private List<DeliveryPackage> deliveryPackageList;

    // constructor
    public Consignee(String name) {
        super();
        this.name = name;
    }

    // public methods
    //<editor-fold desc="getters and setters">
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DeliveryPackage> getDeliveryPackageList() {
        return deliveryPackageList;
    }

    public void setDeliveryPackageList(List<DeliveryPackage> deliveryPackageList) {
        this.deliveryPackageList = deliveryPackageList;
    }
    //</editor-fold>
}