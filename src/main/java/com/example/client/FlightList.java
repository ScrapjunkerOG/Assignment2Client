package com.example.client;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "flights")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlightList {

    private List<Flight> flights;

    public FlightList() {
    }

    public List<Flight> getFlights() {
        return flights;
    }
}