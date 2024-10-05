package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.NearbyPlaceDto;
import com.backend.KKUN_Booking.response.NearbyPlaceResultResponseContainer;
import com.backend.KKUN_Booking.util.LocationUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
public class NearbyPlaceServiceImpl implements NearbyPlaceService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org";
    private final ObjectMapper objectMapper;

    @Autowired
    public NearbyPlaceServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<NearbyPlaceDto> findNearbyNotablePlaces(String address) {
        double[] coordinates = geocodeAddress(address);
        if (coordinates == null) {
            throw new RuntimeException("Unable to geocode the address: " + address);
        }
        return findNearbyNotablePlaces(coordinates[0], coordinates[1]);
    }

    private double[] geocodeAddress(String address) {
        String url = NOMINATIM_API + "/search?format=json&q=" + address;
        try {
            String result = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(result);
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstResult = jsonNode.get(0);
                return new double[]{firstResult.get("lat").asDouble(), firstResult.get("lon").asDouble()};
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing geocoding result: " + e.getMessage());
        }
        throw new RuntimeException("Address not found");
    }

    private List<NearbyPlaceDto> findNearbyNotablePlaces(double lat, double lon) {
        String query = String.format(
                "[out:json];(node(around:10000,%f,%f)[amenity~'museum|tourism|historic|bar|playground|hospital|cinema|gym|marketplace'];);out;",
                lat, lon
        );
        String url = "http://overpass-api.de/api/interpreter?data=" + query;
        NearbyPlaceResultResponseContainer response = restTemplate.getForObject(url, NearbyPlaceResultResponseContainer.class);

        if (response == null || response.elements == null) {
            return Collections.emptyList(); // Return empty list if no valid response
        }

        List<NearbyPlaceDto> nearbyPlaces = new ArrayList<>();
        List<NearbyPlaceDto> tempPlaces = new ArrayList<>();

        for (NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse result : response.elements) {
            if (result.tags == null || result.tags.amenity == null || result.tags.name == null) {
                continue; // Skip if no name or amenity
            }
            processPlace(result, lat, lon, tempPlaces);
        }

        return filterAndSortPlaces(tempPlaces);
    }

    private List<NearbyPlaceDto> filterAndSortPlaces(List<NearbyPlaceDto> tempPlaces) {
        // Define priority tags for sorting
        List<String> tagsToSortBy = Arrays.asList("tourism", "cuisine");

        // Sort places by specified tags and distance
        tempPlaces.sort((place1, place2) -> {
            for (String tagName : tagsToSortBy) {
                int comparison = compareTags(place1.getTags(), place2.getTags(), tagName);
                if (comparison != 0) return comparison;
            }
            return Double.compare(place1.getDistanceInKm(), place2.getDistanceInKm());
        });

        // Filter to get unique categories and limit to 10 places
        Set<String> addedCategories = new HashSet<>();
        List<NearbyPlaceDto> nearbyPlaces = new ArrayList<>();

        for (NearbyPlaceDto place : tempPlaces) {
            if (nearbyPlaces.size() >= 10) break; // Stop after adding 10 places
            if (addedCategories.add(place.getCategory())) {
                nearbyPlaces.add(place); // Add place only if the category hasn't been added yet
            }
        }

        return nearbyPlaces;
    }

    private void processPlace(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place, double originLat, double originLon, List<NearbyPlaceDto> tempPlaces) {
        double distance = LocationUtil.calculateDistance(originLat, originLon, place.lat, place.lon);
        if (distance > 10) return; // Only process places within 10 km

        String category = determineCategory(place);
        if (category == null) return; // Skip if no valid category

        String placeName = place.tags.name != null ? place.tags.name : "Unnamed Place"; // Safe check for place name

        if (!placeName.isEmpty()) {
            tempPlaces.add(new NearbyPlaceDto(placeName, category, distance, place.tags));
        }
    }

    private int compareTags(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse.Tags tags1, NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse.Tags tags2, String tagName) {
        String value1 = getTagValue(tags1, tagName);
        String value2 = getTagValue(tags2, tagName);
        return value1 != null ? (value2 != null ? value1.compareTo(value2) : -1) : (value2 != null ? 1 : 0);
    }

    private String getTagValue(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse.Tags tags, String tagName) {
        switch (tagName) {
            case "tourism":
                return tags.tourism;
            case "cuisine":
                return tags.cuisine;
            default:
                return null;
        }
    }

    private String determineCategory(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place) {
        if (place.tags == null || place.tags.amenity == null) return null;

        switch (place.tags.amenity) {
            case "museum":
                return "Museum";
            case "tourism":
                return "Tourism";
            case "historic":
                return "Historic Site";
            case "cafe":
                return "Cafe";
            case "bar":
                return "Bar";
            case "playground":
                return "Playground";
            case "hospital":
                return "Hospital";
            case "restaurant":
                return "Restaurant";
            case "school":
                return "School";
            case "pharmacy":
                return "Pharmacy";
            case "supermarket":
                return "Supermarket";
            case "bank":
                return "Bank";
            case "bus_station":
                return "Bus Station";
            case "train_station":
                return "Train Station";
            case "toilets":
                return "Public Toilet";
            case "parking":
                return "Parking Lot";
            case "post_office":
                return "Post Office";
            case "cinema":
                return "Cinema";
            case "hotel":
                return "Hotel";
            case "library":
                return "Library";
            case "gym":
                return "Gym";
            case "marketplace":
                return "Marketplace";
            default:
                return null;
        }
    }
}
