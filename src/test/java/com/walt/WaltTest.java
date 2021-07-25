package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics(){

        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

    /**
     * --------------------------- createOrderAndAssignDriver Tests ----------------------------------
     */

    /**
     * This test check if delivery is not created if a null input parameter is given.
     * The data that returned is NULL.
     */
    @Test
    public void createOrderAndAssignDriver_test_num_1(){
        Restaurant restaurant = restaurantRepository.findByName("cafe");
        assertNull(waltService.createOrderAndAssignDriver(null,restaurant,new Date()));
    }

    /**
     * This test check if delivery is not created if the given customer and the given restaurant are not in
     * the same city.
     * The data that returned is NULL.
     */
    @Test
    public void createOrderAndAssignDriver_test_num_2(){
        Customer customer= customerRepository.findByName("Mozart");
        Restaurant restaurant=restaurantRepository.findByName("cafe");
        assertNull(waltService.createOrderAndAssignDriver(customer,restaurant,new Date()));
    }

    /**
     * This test check if delivery is not created in case that there are not available drivers in in the given
     * delivery time at the same city that the restaurant is located.
     * The data that returned is NULL.
     */
    @Test
    public void createOrderAndAssignDriver_test_num_3() {
        Customer customer = customerRepository.findByName("Mozart");
        Restaurant restaurant = restaurantRepository.findByName("meat");
        Date date=new Date();
        //there are only 3 drivers in jerusalem
        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer, restaurant, date);
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer, restaurant, date);
        Delivery delivery3 = waltService.createOrderAndAssignDriver(customer, restaurant, date);
        assertNull(waltService.createOrderAndAssignDriver(customer, restaurant, date));
    }

    /**
     * This test check if a new delivery is created if all the given input parameters are valid and if
     * there is a available driver that can take the delivery.
     */
    @Test
    public void createOrderAndAssignDriver_test_num_4(){
        Customer customer= customerRepository.findByName("Mozart");
        Restaurant restaurant=restaurantRepository.findByName("meat");
        Delivery delivery = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        assertNotNull(delivery);
        assertEquals(delivery.getCustomer().getName(),customer.getName());
        assertEquals(delivery.getCustomer().getCity(),customer.getCity());
        assertEquals(delivery.getCustomer().getId(),customer.getId());
        assertEquals(delivery.getRestaurant().getName(),restaurant.getName());
        assertEquals(delivery.getRestaurant().getId(),restaurant.getId());
    }

    /**
     * This test check if all the deliveries created are with distance between 0-20 km.
     */
    @Test
    public void createOrderAndAssignDriver_test_num_5(){
        Customer customer= customerRepository.findByName("Mozart");
        Restaurant restaurant=restaurantRepository.findByName("meat");
        Delivery delivery1=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery2=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery3=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        assertTrue(delivery1.getDistance()<20);
        assertTrue(delivery2.getDistance()<20);
        assertTrue(delivery3.getDistance()<20);
    }


    /**
     * --------------------------- getDriverRankReport Tests ----------------------------------
     */

    /**
     * This test check if the data that retrieved from the method getDriverRankReport is valid and correct.
     */
    @Test
    public void getDriverRankReport_test(){
        Customer customer= customerRepository.findByName("Mozart");
        Restaurant restaurant=restaurantRepository.findByName("meat");
        Delivery delivery1=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery2=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery3=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        List<DriverDistance> driversTotalDistances = waltService.getDriverRankReport();
        assertEquals(driversTotalDistances.size(),3);
        String driverName = driversTotalDistances.get(0).getDriver().getName();
        Long driverCityID = driversTotalDistances.get(0).getDriver().getCity().getId();
        //The distance is random so I will check if the driver with the highest distance is one of
        // the drivers from Jerusalem (because the restaurant of the test located in Jerusalem)
        assertTrue(driverName.equals("Robert")||driverName.equals("David")||driverName.equals("Neta"));
        assertEquals(driverCityID,restaurant.getCity().getId());
    }

    /**
     * --------------------------- getDriverRankReportByCity Tests ----------------------------------
     */

    /**
     * This test check if the data that retrieved from the method getDriverRankReportByCity is valid and correct.
     */
    @Test
    public void getDriverRankReportByCity_test(){
        Customer customer= customerRepository.findByName("Rachmaninoff");
        Restaurant restaurant=restaurantRepository.findByName("chinese");
        City city=cityRepository.findByName("Tel-Aviv");
        Delivery delivery1=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery2=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery3=waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        List<DriverDistance> driversTotalDistancesByCity = waltService.getDriverRankReportByCity(city);
        assertEquals(driversTotalDistancesByCity.size(),3);
        String driverName = driversTotalDistancesByCity.get(0).getDriver().getName();
        Long driverCityID = driversTotalDistancesByCity.get(0).getDriver().getCity().getId();
        //The distance is random so I will check if the driver with the highest distance by city is one of
        // the drivers from Tel-Aviv (because the restaurant of the test located in Tel-Aviv)
        assertTrue(driverName.equals("Mary")||driverName.equals("Patricia")||driverName.equals("Daniel"));
        assertEquals(driverCityID,restaurant.getCity().getId());

    }

}
