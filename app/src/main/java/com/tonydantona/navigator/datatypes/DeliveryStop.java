package com.tonydantona.navigator.datatypes;

import com.tonydantona.navigator.datatypes.Immutables.StopType;
import com.tonydantona.navigator.datatypes.Immutables.SvcBucket;

import java.util.ArrayList;
import java.util.List;

public class DeliveryStop extends NavStop {
    private double spLat;
    private double spLong;
    private SvcBucket svcBucket;
    private boolean resCommIndicator;
    private List<Consignee> consigneeList;
    private List<Consignee> plannedConsigneeList;

    // constructor
    public DeliveryStop() {
        super();
        setStopType(StopType.DELIVERY);
    }

    // abstract method implementation
    public int getUOWCount() {
        int totalPackageCount = 0;

        for(Consignee consignee : consigneeList) {
            for(DeliveryPackage deliveryPackage : consignee.getDeliveryPackageList()) {
                totalPackageCount++;
            }
        }
        for(Consignee consignee : plannedConsigneeList) {
            for(DeliveryPackage deliveryPackage : consignee.getDeliveryPackageList()) {
                totalPackageCount++;
            }
        }

        return totalPackageCount;
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

    public SvcBucket getSvcBucket() {
        return svcBucket;
    }

    public void setSvcBucket(SvcBucket svcBucket) {
        this.svcBucket = svcBucket;
    }

    public boolean isResCommIndicator() {
        return resCommIndicator;
    }

    public void setResCommIndicator(boolean resCommIndicator) {
        this.resCommIndicator = resCommIndicator;
    }

    public List<Consignee> getConsigneeList() {
        return consigneeList;
    }

    public void setConsigneeList(List<Consignee> consigneeList) {
        this.consigneeList = consigneeList;
    }

    public List<Consignee> getPlannedConsigneeList() {
        return plannedConsigneeList;
    }

    public void setPlannedConsigneeList(List<Consignee> plannedConsigneeList) {
        this.plannedConsigneeList = plannedConsigneeList;
    }
    //</editor-fold>

    public List<DeliveryPackage> getAllPackagesList() {
        List<DeliveryPackage> allPackages = new ArrayList<>();
        for(Consignee consignee : consigneeList) {
            for(DeliveryPackage deliveryPackage : consignee.getDeliveryPackageList()) {
                allPackages.add(deliveryPackage);
            }
        }
        for(Consignee consignee : plannedConsigneeList) {
            for(DeliveryPackage deliveryPackage : consignee.getDeliveryPackageList()) {
                allPackages.add(deliveryPackage);
            }
        }

        return allPackages;
    }
}