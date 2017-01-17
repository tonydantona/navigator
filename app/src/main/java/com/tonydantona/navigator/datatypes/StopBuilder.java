package com.tonydantona.navigator.datatypes;

import android.util.Log;

import com.tonydantona.navigator.datatypes.Immutables.StopType;
import com.tonydantona.navigator.datatypes.Immutables.SvcBucket;

public class StopBuilder {
    private NavRoute route;
    private int loadID;
    private NavStop stop;
    
    // constructor
    public StopBuilder(NavRoute route) {
        super();
        this.route = route;
        stop = null;
    }

    // public methods
    public void setResult() {
        boolean loadIDFound = false;
        for(NavLoad load : route.getLoadList()) {
            if(load.getLoadID() == loadID) {
                loadIDFound = true;
                load.getStopList().add(stop);
            }
        }
        if(!loadIDFound) {
            Log.e("StopBuilder", "LoadName not found to add stop (" + stop.getStopID() + ")");
        }
    }

    public boolean isBuilt() {
        return (stop != null);
    }

    public void setStopType(StopType stopType) {
        switch(stopType) {
            case DELIVERY:
                stop = new DeliveryStop();
                break;
            case PICKUP:
                stop = new PickupStop();
                break;
            case EOW:
                stop = new EOWStop();
                break;
            case BREAK:
                stop = new BreakStop();
                break;
        }
        // initialize stop address
        stop.setAddress(new Address());
    }

    //<editor-fold desc="NavStop setters">
    public void setStopID(int stopID) {
        stop.setStopID(stopID);
    }

    public void setLoadID(int loadID) {
        this.loadID = loadID;
    }

    public void setODO(int odo) {
        stop.setODO(odo);
    }

    public void setNAPLat(double napLat) {
        stop.setNAPLat(napLat);
    }

    public void setNAPLong(double napLong) {
        stop.setNAPLong(napLong);
    }
    
    public void setStNumber(String stNumber) {
        stop.getAddress().setStNumber(stNumber);
    }
    
    public void setStPrefix(String stPrefix) {
        stop.getAddress().setStPrefix(stPrefix);
    }

    public void setStName(String stName) {
        stop.getAddress().setStName(stName);
    }

    public void setStSuffix(String stSuffix) {
        stop.getAddress().setStSuffix(stSuffix);
    }

    public void setStType(String stType) {
        stop.getAddress().setStType(stType);
    }

    public void setCity(String city) {
        stop.getAddress().setCity(city);
    }

    public void setState(String state) {
        stop.getAddress().setState(state);
    }

    public void setPostalCode(String postalCode) {
        stop.getAddress().setPostalCode(postalCode);
    }
    
    public void setODOESST(double odoESST) {
        stop.setODOESST(odoESST);
    }

    public void setESST(double ESST) {
        stop.setODOESST(ESST);
    }

    public void setLSST(double LSST) {
        stop.setLSST(LSST);
    }

    public void setSvcTime(double svcTime) {
        stop.setSvcTime(svcTime);
    }

    public void setIsCompleted(boolean isCompleted) {
        stop.setIsCompleted(isCompleted);
    }
    //</editor-fold>
    
    //<editor-fold desc="DeliveryStop and PickupStop setters">
    public void setSPLat(double spLat) {
        if(stop.getStopType() == StopType.DELIVERY) {
            ((DeliveryStop) stop).setSPLat(spLat);
        } else if(stop.getStopType() == StopType.PICKUP) {
            ((PickupStop) stop).setSPLat(spLat);
        }
    }
    
    public void setSPLong(double spLong) {
        if(stop.getStopType() == StopType.DELIVERY) {
            ((DeliveryStop) stop).setSPLong(spLong);
        } else if(stop.getStopType() == StopType.PICKUP) {
            ((PickupStop) stop).setSPLong(spLong);
        }
    }  
    //</editor-fold>

    //<editor-fold desc="DeliveryStop setters">
    public void setSvcBucket(SvcBucket svcBucket) {
        ((DeliveryStop) stop).setSvcBucket(svcBucket);
    }

    public void setResCommIndicator(boolean resCommIndicator) {
        ((DeliveryStop) stop).setResCommIndicator(resCommIndicator);
    }
    //</editor-fold>

    //<editor-fold desc="PickupStop setters">
    public void setPickupPoint(String pickupPoint) {
        ((PickupStop) stop).setPickupPoint(pickupPoint);
    }
    
    public void setShipperName(String shipperName) {
        ((PickupStop) stop).setShipperName(shipperName);
    }

    public void setShipperNumber(String shipperNumber) {
        ((PickupStop) stop).setShipperNumber(shipperNumber);
    }
    //</editor-fold>
}
