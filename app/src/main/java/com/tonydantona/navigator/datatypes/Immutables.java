package com.tonydantona.navigator.datatypes;

public class Immutables {
    public enum PackageStatus {
        PENDING,
        COMPLETE,
        NOT_FOUND,
        NOT_IN,
        DELETED
    }

    public enum ResCommIndicator {
        RES,
        COMM
    }

    public enum StopStatus {

    }

    public enum StopType {
        DELIVERY,
        PICKUP,
        EOW,
        BREAK
    }

    public enum SvcBucket {
        STANDARD,
        PREMIUM,
        SAVER
    }
}
