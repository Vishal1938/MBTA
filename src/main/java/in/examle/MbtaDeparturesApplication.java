package in.examle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class MbtaDeparturesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MbtaDeparturesApplication.class, args);
        getNextDepartures();
    }

    private static void getNextDepartures() {
        String apiUrl = "https://api-v3.mbta.com/predictions?filter[stop]=place-pktrm&filter[route_type]=0,1";
        RestTemplate restTemplate = new RestTemplate();
        MbtaApiResponse response = restTemplate.getForObject(apiUrl, MbtaApiResponse.class);

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("Current Time: " + currentTime.format(formatter) + "\n");

        Map<String, List<Train>> departures = new HashMap<>();
        if (response != null && response.getData() != null) {
            for (Prediction prediction : response.getData()) {
                Attributes attributes = prediction.getAttributes();
                String destination = attributes.getHeadsign();
                LocalDateTime departureTime = LocalDateTime.parse(attributes.getDepartureTime(), formatter);
                long minutesUntilDeparture = attributes.getDepartureSeconds() / 60;

                String lineName = prediction.getRelationships().getRoute().getData().getId();
                departures.computeIfAbsent(lineName, k -> new ArrayList<>())
                        .add(new Train(destination, departureTime, minutesUntilDeparture));
            }
        }

        departures.forEach((line, trains) -> {
            System.out.println("----" + line + "----");
            trains.stream()
                    .filter(train -> train.getMinutesUntilDeparture() >= 0)
                    .sorted(Comparator.comparing(Train::getDepartureTime))
                    .limit(10)
                    .forEach(train -> System.out.println(
                            train.getDestination() + ": Departing in " + train.getMinutesUntilDeparture() + " minutes"));
            System.out.println();
        });
    }
}

class MbtaApiResponse {
    private List<Prediction> data;

    public List<Prediction> getData() {
        return data;
    }

    public void setData(List<Prediction> data) {
        this.data = data;
    }
}

class Prediction {
    private Attributes attributes;
    private Relationships relationships;

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Relationships getRelationships() {
        return relationships;
    }

    public void setRelationships(Relationships relationships) {
        this.relationships = relationships;
    }
}

class Attributes {
    private String headsign;
    private String departureTime;
    private long departureSeconds;

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public long getDepartureSeconds() {
        return departureSeconds;
    }

    public void setDepartureSeconds(long departureSeconds) {
        this.departureSeconds = departureSeconds;
    }
}

class Relationships {
    private Route route;

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}

class Route {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}

class Data {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

class Train {
    private String destination;
    private LocalDateTime departureTime;
    private long minutesUntilDeparture;

    public Train(String destination, LocalDateTime departureTime, long minutesUntilDeparture) {
        this.destination = destination;
        this.departureTime = departureTime;
        this.minutesUntilDeparture = minutesUntilDeparture;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public long getMinutesUntilDeparture() {
        return minutesUntilDeparture;
    }
}

