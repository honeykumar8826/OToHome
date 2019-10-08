package com.travel.cab.service.modal;

public class MyOrderDetailModel {
    private String pickup_location;
    private String drop_location;
    private String distance_home_office;
    private String vehicle_type;
    private String service_type;
    private String service_days;
    private String service_starting_date;
    private String coming_time;
    private String going_time;
    private String order_number;
    private String service_fare;

    public MyOrderDetailModel() {

    }

    public String getService_fare() {
        return service_fare;
    }

    public void setService_fare(String service_fare) {
        this.service_fare = service_fare;
    }

    public String getService_starting_date() {
        return service_starting_date;
    }

    public void setService_starting_date(String service_starting_date) {
        this.service_starting_date = service_starting_date;
    }


    public String getOrder_number() {
        return order_number;
    }

    public void setOrder_number(String order_number) {
        this.order_number = order_number;
    }

    public String getPickup_location() {
        return pickup_location;
    }

    public void setPickup_location(String pickup_location) {
        this.pickup_location = pickup_location;
    }

    public String getDrop_location() {
        return drop_location;
    }

    public void setDrop_location(String drop_location) {
        this.drop_location = drop_location;
    }

    public String getDistance_home_office() {
        return distance_home_office;
    }

    public void setDistance_home_office(String distance_home_office) {
        this.distance_home_office = distance_home_office;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public String getService_days() {
        return service_days;
    }

    public void setService_days(String service_days) {
        this.service_days = service_days;
    }


    public String getComing_time() {
        return coming_time;
    }

    public void setComing_time(String coming_time) {
        this.coming_time = coming_time;
    }

    public String getGoing_time() {
        return going_time;
    }

    public void setGoing_time(String going_time) {
        this.going_time = going_time;
    }
}

