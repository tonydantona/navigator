package com.tonydantona.navigator.datatypes;

import com.tonydantona.navigator.datatypes.Immutables.StopType;

public class EOWStop extends NavStop {
    // constructor
    public EOWStop() {
        super();
        setStopType(StopType.EOW);
    }

    // abstract method implementation
    public int getUOWCount() {
        return 1;
    }
}