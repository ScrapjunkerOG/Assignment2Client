
package com.example.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class FlightClient {

	private static final String BASE_URL =
	        "http://localhost:8080/FlightApp/api/flights";

    private static Client client;
    private static WebTarget target;

    public static void main(String[] args) {

        client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);
        target = client.target(BASE_URL);

        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n===== FLIGHT CLIENT =====");
            System.out.println("1. Add flight");
            System.out.println("2. Search flight by ID");
            System.out.println("3. Search flights by date");
            System.out.println("4. Search flights by price");
            System.out.println("5. Search flights by city");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addFlight(scanner);
                    break;
                case 2:
                    searchById(scanner);
                    break;
                case 3:
                    searchByDate(scanner);
                    break;
                case 4:
                    searchByPrice(scanner);
                    break;
                case 5:
                    searchByCity(scanner);
                    break;
                case 0:
                    System.out.println("Client closed.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 0);

        client.close();
        scanner.close();
    }

    // Add flight using POST
    private static void addFlight(Scanner scanner) {

        System.out.print("Flight ID: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        System.out.print("Time: ");
        String time = scanner.nextLine();

        System.out.print("Origin: ");
        String origin = scanner.nextLine();

        System.out.print("Destination: ");
        String destination = scanner.nextLine();

        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine());

        Flight flight = new Flight(
                id, date, time, origin, destination, price
        );

        try (Response response = target.request()
                .post(Entity.entity(flight, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() == 201) {
                System.out.println("Flight added successfully.");
            } else {
                System.out.println("Could not add flight. HTTP status: "
                        + response.getStatus());
            }
        }
    }

    // Search by ID using GET
    private static void searchById(Scanner scanner) {

        System.out.print("Enter flight ID: ");
        int id = Integer.parseInt(scanner.nextLine());

        try (Response response = target
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_XML)
                .get()) {

            if (response.getStatus() == 200) {
                Flight flight = response.readEntity(Flight.class);
                printFlight(flight);
            } else if (response.getStatus() == 404) {
                noFlightsFound();
            } else {
                System.out.println("Request failed. HTTP status: "
                        + response.getStatus());
            }
        }
    }

    // Search by date using GET
    private static void searchByDate(Scanner scanner) {

        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        try (Response response = target
                .path("date")
                .path(date)
                .request(MediaType.APPLICATION_XML)
                .get()) {

            if (response.getStatus() == 200) {

                FlightList result = response.readEntity(FlightList.class);

                if (result != null && hasFlights(result.getFlights())) {
                    for (Flight flight : result.getFlights()) {
                        printFlight(flight);
                    }
                } else if (result == null) {
                    noFlightsFound();
                }

            } else {
                System.out.println("Request failed. HTTP status: "
                        + response.getStatus());
            }
        }
    }

    // Search by price using GET
    private static void searchByPrice(Scanner scanner) {

        System.out.print("Minimum price: ");
        double min = Double.parseDouble(scanner.nextLine());

        System.out.print("Maximum price: ");
        double max = Double.parseDouble(scanner.nextLine());

        try (Response response = target
                .path("price")
                .queryParam("min", min)
                .queryParam("max", max)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {

                HashMap<Integer, Flight> flights =
                        response.readEntity(
                                new GenericType<HashMap<Integer, Flight>>() {}
                        );

                hasFlights(new ArrayList<>(flights.values()));

                if (flights != null && !flights.isEmpty()) {
                    for (Flight flight : flights.values()) {
                        printFlight(flight);
                    }
                }

            } else {
                System.out.println("Request failed. HTTP status: "
                        + response.getStatus());
            }
        }
    }

    // Search by city using GET
    private static void searchByCity(Scanner scanner) {

        System.out.print("Enter city: ");
        String city = scanner.nextLine();

        try (Response response = target
                .path("city")
                .path(city)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {

                HashMap<Integer, Flight> flights =
                        response.readEntity(
                                new GenericType<HashMap<Integer, Flight>>() {}
                        );

                if (flights != null &&
                        hasFlights(new ArrayList<>(flights.values()))) {
                    for (Flight flight : flights.values()) {
                        printFlight(flight);
                    }
                }

            } else {
                System.out.println("Request failed. HTTP status: "
                        + response.getStatus());
            }
        }
    }

    // Reusable empty-result check
    private static boolean hasFlights(List<Flight> flights) {

        if (flights == null || flights.isEmpty()) {
            noFlightsFound();
            return false;
        }

        return true;
    }

    private static void noFlightsFound() {
        System.out.println("\nNo flights found. Returning to menu...");
    }

    // Display flight information
    private static void printFlight(Flight flight) {

        System.out.println("\n----------------------");
        System.out.println("ID: " + flight.getId());
        System.out.println("Date: " + flight.getDate());
        System.out.println("Time: " + flight.getTime());
        System.out.println("Origin: " + flight.getOrigin());
        System.out.println("Destination: " + flight.getDestination());
        System.out.println("Price: " + flight.getPrice());
        System.out.println("----------------------");
    }
}
