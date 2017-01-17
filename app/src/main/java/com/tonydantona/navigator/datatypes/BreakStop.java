package com.tonydantona.navigator.datatypes;

import com.tonydantona.navigator.datatypes.Immutables;

public class BreakStop extends NavStop {
    // constructor
    public BreakStop() {
        super();
        setStopType(Immutables.StopType.BREAK);
    }

    // abstract method implementation
    public int getUOWCount() {
        return 0;
    }

    // public methods
    // sets navigation data equivalent to previous stop
    public void setBreakStop(NavStop prevStop) {
        setNAPLat(prevStop.getNAPLat());
        setNAPLong(prevStop.getNAPLong());
        setAddress(prevStop.getAddress());
    }
}