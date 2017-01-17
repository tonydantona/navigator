package com.tonydantona.navigator.datatypes;

import java.lang.reflect.Field;

public class Address implements Comparable<Address> {
    private String stNumber;
    private String stPrefix;
    private String stName;
    private String stSuffix;
    private String stType;
    private String city;
    private String state;
    private String postalCode;

    // constructor
    public Address() {
        super();
    }

    // public methods
    //<editor-fold desc="getters and setters">
    public String getStNumber() {
        return stNumber;
    }

    public void setStNumber(String stNumber) {
        this.stNumber = stNumber;
    }

    public String getStPrefix() {
        return stPrefix;
    }

    public void setStPrefix(String stPrefix) {
        this.stPrefix = stPrefix;
    }

    public String getStName() {
        return stName;
    }

    public void setStName(String stName) {
        this.stName = stName;
    }

    public String getStSuffix() {
        return stSuffix;
    }

    public void setStSuffix(String stSuffix) {
        this.stSuffix = stSuffix;
    }

    public String getStType() {
        return stType;
    }

    public void setStType(String stType) {
        this.stType = stType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    //</editor-fold>

    public String getShortString() {
        StringBuilder address = new StringBuilder();
        if(stNumber != null) {
            address.append(stNumber);
        }
        if(stPrefix != null) {
            address.append(" " + stPrefix);
        }
        if(stName != null) {
            address.append(" " + stName);
        }
        if(stSuffix != null) {
            address.append(" " + stSuffix);
        }
        if(stType != null) {
            address.append(" " + stType);
        }
        if(city != null) {
            address.append(", " + city);
        }
        
        return address.toString();
    }

    // compareTo for multiple stops at same address (e.g. ODO 7 and ODO 106 at same address later in the day)
    @Override
    public int compareTo(Address address) {
        // TO-DO: implementation

        return 1;
    }
}
