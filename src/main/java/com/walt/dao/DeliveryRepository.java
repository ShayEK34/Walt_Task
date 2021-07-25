package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.Delivery;
import com.walt.model.DriverDistance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {

    List<Delivery> findDeliveryByDriver(Driver driver);

    List<Delivery> findAllDeliveryByDriverAndDeliveryTime( Driver driver, Date deliveryTime);

    @Query("select d.driver AS driver, sum(d.distance) AS totalDistance from Delivery d group by driver order by totalDistance desc")
    List<DriverDistance> findTotalDistancePerDriver();

    @Query("select d.driver AS driver, sum(d.distance) AS totalDistance from Delivery d where "+"d.driver.city =:city group by driver order by totalDistance desc")
    List<DriverDistance> findTotalDistancePerDriverByCity(@Param("city")City city);

}


