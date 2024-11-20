package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.NearbyPlaceDto;
import com.backend.KKUN_Booking.response.NearbyPlaceResultResponseContainer;
import com.backend.KKUN_Booking.service.NearbyPlaceService;
import com.backend.KKUN_Booking.util.LocationUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
public class NearbyPlaceServiceImpl implements NearbyPlaceService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${OPENCAGE_API_KEY}")
    private String apiKey;

    @Value("${OPENCAGE_BASE_URL}")
    private String baseUrl;

    private final ObjectMapper objectMapper;
    private Set<CategoryType> categoriesWithPlaces;

    private enum CategoryType {
        AMENITY, TOURISM, LEISURE, HISTORIC, PLACE, NATURAL, HEALTHCARE
    }

    @Autowired
    public NearbyPlaceServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.categoriesWithPlaces = EnumSet.noneOf(CategoryType.class);
    }

    @Override
    public List<NearbyPlaceDto> findNearbyNotablePlaces(String address) {
        categoriesWithPlaces.clear();
        double[] coordinates = geocodeAddress(address).orElseThrow(() ->
                new RuntimeException("Unable to geocode the address: " + address));
        List<NearbyPlaceDto> places = findNearbyNotablePlaces(coordinates[0], coordinates[1]);
        validateCategoryRepresentation();
        return places;
    }

    private Optional<double[]> geocodeAddress(String address) {
        String url = baseUrl + "/geocode/v1/json?q=" + address + "&key=" + apiKey;
        try {
            String result = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(result);
            if (jsonNode.has("results") && jsonNode.get("results").size() > 0) {
                JsonNode firstResult = jsonNode.get("results").get(0).get("geometry");
                return Optional.of(new double[]{firstResult.get("lat").asDouble(), firstResult.get("lng").asDouble()});
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing geocoding result: " + e.getMessage());
        }
        return Optional.empty();
    }

    private void validateCategoryRepresentation() {
        List<CategoryType> requiredCategories = List.of(
                CategoryType.AMENITY, CategoryType.TOURISM, CategoryType.LEISURE,
                CategoryType.HISTORIC, CategoryType.HEALTHCARE, CategoryType.NATURAL);

        String missingCategories = requiredCategories.stream()
                .filter(type -> !categoriesWithPlaces.contains(type))
                .map(Enum::name)
                .reduce((a, b) -> a + ", " + b).orElse("");

        if (!missingCategories.isEmpty()) {
            System.out.println("Warning: Missing places for categories: " + missingCategories);
        }
    }

    private List<NearbyPlaceDto> findNearbyNotablePlaces(double lat, double lon) {
        String query = createQuery(lat, lon);
        String url = "http://overpass-api.de/api/interpreter?data=" + query;
        NearbyPlaceResultResponseContainer response = restTemplate.getForObject(url, NearbyPlaceResultResponseContainer.class);

        if (response == null || response.elements == null) return Collections.emptyList();

        List<NearbyPlaceDto> tempPlaces = new ArrayList<>();
        Map<CategoryType, Integer> categoryCount = new EnumMap<>(CategoryType.class); // Tối ưu bằng EnumMap
        Set<String> addedAmenities = new HashSet<>(); // Sử dụng HashSet cho String
        Map<String, Integer> tourismTypeCount = new HashMap<>(); // Đếm số lượng từng loại trong tourism

        response.elements.forEach(result -> processPlace(result, lat, lon, tempPlaces, categoryCount, addedAmenities, tourismTypeCount));
        return filterAndSortPlaces(tempPlaces);
    }

    private String createQuery(double lat, double lon) {
        return String.format(
                "[out:json];(node(around:20000,%f,%f)[amenity~\"restaurant|hospital|cafe|bar|parking|cinema|fast_food|bicycle_rental|car_rental\"];"
                        + "node(around:20000,%f,%f)[tourism~\"museum|aquarium|theatre|attraction|gallery|theme_park\"];"
                        + "node(around:10000,%f,%f)[leisure~\"park|garden|playground|sports_centre|stadium|swimming_pool\"];"
                        + "node(around:10000,%f,%f)[historic~\"castle\"];"
                        + "node(around:10000,%f,%f)[place~\"square\"];"
                        + "node(around:10000,%f,%f)[natural~\"beach|cliff|spring\"];"
                        + "node(around:10000,%f,%f)[healthcare~\"hospital|clinic|pharmacy\"];);out;",
                lat, lon, lat, lon, lat, lon, lat, lon, lat, lon, lat, lon, lat, lon);
    }

    private void processPlace(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place,
                              double originLat, double originLon,
                              List<NearbyPlaceDto> tempPlaces,
                              Map<CategoryType, Integer> categoryCount,
                              Set<String> addedAmenities, // Sử dụng HashSet cho String
                              Map<String, Integer> tourismTypeCount) { // Đếm từng loại tourism
        double distance = LocationUtil.calculateDistance(originLat, originLon, place.lat, place.lon);
        if (distance > 15 || place.tags == null || place.tags.name == null) return;

        CategoryType categoryType = determineCategoryType(place);
        String category = determineCategory(place);

        if (categoryType == null || category == null) return;

        if (categoryType == CategoryType.AMENITY) {
            if (!addedAmenities.contains(place.tags.amenity)) {
                addPlace(tempPlaces, place, category, distance);
                addedAmenities.add(place.tags.amenity);
                categoryCount.merge(categoryType, 1, Integer::sum);
                categoriesWithPlaces.add(categoryType);
            }
        } else if (categoryType == CategoryType.TOURISM) {
            String tourismType = place.tags.tourism;
            if (tourismType != null && tourismTypeCount.getOrDefault(tourismType, 0) < 3) {
                addPlace(tempPlaces, place, category, distance);
                tourismTypeCount.merge(tourismType, 1, Integer::sum);
                categoryCount.merge(categoryType, 1, Integer::sum);
                categoriesWithPlaces.add(categoryType);
            }
        } else {
            if (categoryCount.getOrDefault(categoryType, 0) < 1) {
                addPlace(tempPlaces, place, category, distance);
                categoryCount.merge(categoryType, 1, Integer::sum);
                categoriesWithPlaces.add(categoryType);
            }
        }
    }

    private void addPlace(List<NearbyPlaceDto> tempPlaces, NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place, String category, double distance) {
        tempPlaces.add(new NearbyPlaceDto(place.tags.name, category, distance, place.tags));
    }

    private CategoryType determineCategoryType(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place) {
        if (place.tags.amenity != null) return CategoryType.AMENITY;
        if (place.tags.tourism != null) return CategoryType.TOURISM;
        if (place.tags.leisure != null) return CategoryType.LEISURE;
        if (place.tags.historic != null) return CategoryType.HISTORIC;
        if (place.tags.place != null) return CategoryType.PLACE;
        if (place.tags.natural != null) return CategoryType.NATURAL;
        if (place.tags.healthcare != null) return CategoryType.HEALTHCARE;
        return null;
    }

    private String determineCategory(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place) {
        if (place.tags.healthcare != null) return mapHealthcareCategory(place.tags.healthcare);
        if (place.tags.amenity != null) return mapAmenityCategory(place.tags.amenity);
        if (place.tags.tourism != null) return mapTourismCategory(place.tags.tourism);
        if (place.tags.leisure != null) return mapLeisureCategory(place.tags.leisure);
        if (place.tags.historic != null) return mapHistoricCategory(place.tags.historic);
        if (place.tags.place != null) return mapPlaceCategory(place.tags.place);
        if (place.tags.natural != null) return mapNaturalCategory(place.tags.natural);
        return null;
    }

    private List<NearbyPlaceDto> filterAndSortPlaces(List<NearbyPlaceDto> tempPlaces) {
        tempPlaces.sort(Comparator.comparingDouble(NearbyPlaceDto::getDistanceInKm));
        return tempPlaces.size() > 15 ? tempPlaces.subList(0, 15) : tempPlaces;
    }

    private String mapTourismCategory(String tourismType) {
        return switch (tourismType) {
            case "museum" -> "Museum";
            case "zoo" -> "Zoo";
            case "aquarium" -> "Aquarium";
            case "theatre" -> "Theatre";
            case "attraction" -> "Attraction";
            case "viewpoint" -> "Viewpoint";
            case "gallery" -> "Gallery";
            case "theme_park" -> "Theme Park";
            default -> null;
        };
    }

    private String mapAmenityCategory(String amenityType) {
        return switch (amenityType) {
            case "restaurant" -> "Restaurant";
            case "cafe" -> "Cafe";
            case "bar" -> "Bar";
            case "parking" -> "Parking Lot";
            case "cinema" -> "Cinema";
            case "fast_food" -> "Fast Food";
            case "bicycle_rental" -> "Bicycle Rental";
            case "car_rental" -> "Car Rental";
            case "hospital" -> "Hospital";
            default -> null;
        };
    }

    private String mapLeisureCategory(String leisureType) {
        return switch (leisureType) {
            case "park" -> "Park";
            case "garden" -> "Garden";
            case "playground" -> "Playground";
            case "sports_centre" -> "Sports Centre";
            case "stadium" -> "Stadium";
            case "swimming_pool" -> "Swimming Pool";
            default -> null;
        };
    }

    private String mapHistoricCategory(String historicType) {
        return switch (historicType) {
            case "castle" -> "Castle";
            case "monument" -> "Monument";
            case "ruins" -> "Ruins";
            default -> null;
        };
    }

    private String mapPlaceCategory(String placeType) {
        return switch (placeType) {
            case "square" -> "Square";
            default -> null;
        };
    }

    private String mapNaturalCategory(String naturalType) {
        return switch (naturalType) {
            case "beach" -> "Beach";
            case "cliff" -> "Cliff";
            case "spring" -> "Spring";
            case "cave_entrance" -> "Cave Entrance";
            default -> null;
        };
    }

    private String mapHealthcareCategory(String healthcareType) {
        return switch (healthcareType) {
            case "hospital" -> "Hospital";
            case "clinic" -> "Clinic";
            case "pharmacy" -> "Pharmacy";
            default -> null;
        };
    }
}
