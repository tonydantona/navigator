package com.tonydantona.navigator.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NavRoute {
    private int routeID;
    private String countryCode;
    private String bldgMnemonic;
    private String slic;
    private String routeName;
    private Date deliveryDate;
    private double schStartTime;
    private double odoMiles;
    private double odoTime;
    private double breakESST;
    private double breakLSST;
    private double breakDuration;
    private List<NavLoad> loadList;

    // constructor
    public NavRoute() {
        super();
        routeID = 0;
        loadList = new ArrayList<>();
    }

    // public methods
    //<editor-fold desc="getters and setters">
    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getBldgMnemonic() {
        return bldgMnemonic;
    }

    public void setBldgMnemonic(String bldgMnemonic) {
        this.bldgMnemonic = bldgMnemonic;
    }

    public String getSLIC() {
        return slic;
    }

    public void setSLIC(String slic) {
        this.slic = slic;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getSchStartTime() {
        return schStartTime;
    }

    public void setSchStartTime(double schStartTime) {
        this.schStartTime = schStartTime;
    }

    public double getODOMiles() {
        return odoMiles;
    }

    public void setODOMiles(double odoMiles) {
        this.odoMiles = odoMiles;
    }

    public double getODOTime() {
        return odoTime;
    }

    public void setODOTime(double odoTime) {
        this.odoTime = odoTime;
    }

    public double getBreakESST() {
        return breakESST;
    }

    public void setBreakESST(double breakESST) {
        this.breakESST = breakESST;
    }

    public double getBreakLSST() {
        return breakLSST;
    }

    public void setBreakLSST(double breakLSST) {
        this.breakLSST = breakLSST;
    }

    public double getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(double breakDuration) {
        this.breakDuration = breakDuration;
    }

    public List<NavLoad> getLoadList() {
        return loadList;
    }

    public void setLoadList(List<NavLoad> loadList) {
        this.loadList = loadList;
    }
    //</editor-fold>

    public void sortByOdo() {
        for(NavLoad load : loadList) {
            Collections.sort(load.getStopList(), new Comparator<NavStop>() {
                @Override
                public int compare(NavStop lhs, NavStop rhs) {
                    return lhs.getODO() - rhs.getODO();
                }
            });
        }

        Collections.sort(loadList, new Comparator<NavLoad>() {
            @Override
            public int compare(NavLoad lhs, NavLoad rhs) {
                int lhsODO = lhs.getStopList().get(0).getODO();
                if(lhsODO < 0) {
                    lhs.getStopList().get(1).getODO();
                }
                int rhsODO = rhs.getStopList().get(0).getODO();
                if(rhsODO < 0) {
                    rhsODO = rhs.getStopList().get(1).getODO();
                }
                return lhsODO - rhsODO;
            }
        });
    }

    public List<NavStop> getAllStopList() {
        List<NavStop> allStopList = new ArrayList<>();
        for(NavLoad load : loadList) {
            for(NavStop stop : load.getStopList()) {
                allStopList.add(stop);
            }
        }

        return allStopList;
    }

    public int getUOWCount() {
        int totalUOWCount = 0;
        for(NavLoad load : loadList) {
            totalUOWCount += load.getUOWCount();
        }

        return totalUOWCount;
    }
}
