package com.walt;

import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * This class represent a Walt delivery service that transfer deliveries by available drivers to customers
 * that make the reservation.
 */
@Service
public class WaltServiceImpl implements WaltService {

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    /**
     * This function create delivery for a restaurant customer and found available driver
     * @param customer - The given customer that create the reservation
     * @param restaurant - The given restaurant where the reservation was made
     * @param deliveryTime - The delivery time of the reservation.
     * @return Delivery
     */
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        if (checkInputParameters (customer,  restaurant,  deliveryTime)){
            if(checkSameCityCustomerAndRestaurant(customer,restaurant)){
                try{
                    Driver driver = findAvailableDriver(restaurant,  deliveryTime);
                    Delivery newDelivery= new Delivery(driver,restaurant,customer,deliveryTime);
                    deliveryRepository.save(newDelivery);
                    WaltApplication.getLog().info("The delivery was created successfully");
                    return newDelivery;
                } catch (RuntimeException e) {
                    WaltApplication.getLog().error("ERROR: no available driver!");
                    WaltApplication.getLog().info("The delivery was not created");
                    return null;
                }
            }
            WaltApplication.getLog().error("ERROR: The customer and the restaurant not in same city");
            WaltApplication.getLog().info("The delivery was not created");
        }
        WaltApplication.getLog().error("ERROR: Bad input params!");
        WaltApplication.getLog().info("The delivery was not created");
        return null;
    }

    /**
     * This function is looking for a driver that live in the same city of the given restaurant and he/shae
     * has no other delivery in the given time. If more than one driver is available assign it to the least busy driver
     * according to the driver history.
     * @param restaurant - The given restaurant where the reservation was made
     * @param deliveryTime - The delivery time of the reservation.
     * @return
     */
    public Driver findAvailableDriver(Restaurant restaurant, Date deliveryTime) {
        List<Driver> driversInTheCity= driverRepository.findAllDriversByCity(restaurant.getCity());
        List<Driver> availableDrivers= new ArrayList<>();
        for(Driver driver:driversInTheCity){
            List<Delivery> driverDeliveries =deliveryRepository.findAllDeliveryByDriverAndDeliveryTime(driver,new Timestamp(deliveryTime.getTime()));
            if (driverDeliveries.isEmpty())
                availableDrivers.add(driver);
        }
        if(availableDrivers.isEmpty())
            throw new RuntimeException("ERROR: no available driver for the delivery!");
        else if (availableDrivers.size()==1)
            return availableDrivers.get(0);
        else {
            availableDrivers.sort(Comparator.comparingInt(this::numOfDeliveriesPerDriver));
            return availableDrivers.get(0);
        }
    }

    /**
     *
     * @return - A list of the drivers names and the total distance of delivery order in descending order.
     */
    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepository.findTotalDistancePerDriver();
    }

    /**
     *
     * @param city - The given city
     * @return a list of the drivers name in the given city
     *  and the total distance of delivery order by in descending order.
     */
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return deliveryRepository.findTotalDistancePerDriverByCity(city);
    }

    /**
     * This function check if the given parameters for creating delivery are legal.
     * @param customer - The given customer
     * @param restaurant - The given restaurant
     * @param deliveryTime - The given delivery time
     * @return true if the given input parameters are OK or false if not.
     */
    public boolean checkInputParameters (Customer customer, Restaurant restaurant, Date deliveryTime){
        if(customer == null || restaurant== null || deliveryTime ==null){
            WaltApplication.getLog().error("ERROR: invalid input parameters!");
            return false;
        }
        return true;
    }

    /**
     * This function check if the given customer and the given restaurant located in the same city.
     * @param customer - The given customer
     * @param restaurant - The given restaurant
     * @return true if the given customer and the given restaurant located in the same city or false if not.
     */
    public boolean checkSameCityCustomerAndRestaurant (Customer customer, Restaurant restaurant) {
        if(!customer.getCity().getId().equals(restaurant.getCity().getId())){
            WaltApplication.getLog().error("ERROR: the restaurant and the customer are located in different cities");
            return false;
        }
        return true;
    }

    /**
     *
     * @param driver - The given driver
     * @return the number of deliveries for a given driver.
     */
    public int numOfDeliveriesPerDriver(Driver driver){
        return deliveryRepository.findDeliveryByDriver(driver).size();
    }

}
