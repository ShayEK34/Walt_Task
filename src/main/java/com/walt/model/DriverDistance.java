package com.walt.model;

public interface DriverDistance {
    Driver getDriver();
    //Long getTotalDistance();

    // I change the type to double for the method getTotalDistance because in the delivery class the type of distance
    // is double and if in the interface the type of the method is long I get a result of the distance sum not as
    // double type.
    Double getTotalDistance();
}
