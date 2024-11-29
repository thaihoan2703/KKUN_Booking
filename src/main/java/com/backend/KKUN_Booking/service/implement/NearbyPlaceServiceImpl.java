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
        AMENITY, TOURISM, LEISURE, HISTORIC, PLACE, NATURAL
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
                CategoryType.HISTORIC, CategoryType.NATURAL);

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
        Map<CategoryType, Integer> categoryCount = new EnumMap<>(CategoryType.class);
        Set<String> addedAmenities = new HashSet<>();
        Map<String, Integer> tourismTypeCount = new HashMap<>();

        for (NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse result : response.elements) {
            processPlace(result, lat, lon, tempPlaces, categoryCount, addedAmenities, tourismTypeCount);
        }

        return filterAndSortPlaces(tempPlaces);
    }

    private void processPlace(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place,
                              double originLat, double originLon,
                              List<NearbyPlaceDto> tempPlaces,
                              Map<CategoryType, Integer> categoryCount,
                              Set<String> addedAmenities,
                              Map<String, Integer> tourismTypeCount) {
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

    // Các phương thức còn lại giữ nguyên như trong mã gốc
    // (createQuery, determineCategory, addPlace, mapXxxCategory, etc.)
    private static final class QueryConfig {
        private static final List<QueryCategory> CATEGORIES = List.of(
                new QueryCategory("amenity", 10000,
                        "restaurant|hospital|cafe|bar|parking|cinema|fast_food|bicycle_rental|car_rental"),
                new QueryCategory("tourism", 20000,
                        "museum|aquarium|theatre|attraction|gallery|theme_park"),
                new QueryCategory("leisure", 10000,
                        "park|garden|playground|sports_centre|stadium|swimming_pool"),
                new QueryCategory("historic", 10000, "castle"),
                new QueryCategory("place", 10000, "square"),
                new QueryCategory("natural", 10000, "beach|cliff|spring")

        );

        private record QueryCategory(String type, int radius, String tags) {}
    }

    private String createQuery(double lat, double lon) {
        StringBuilder queryBuilder = new StringBuilder("[out:json];(");

        for (QueryConfig.QueryCategory category : QueryConfig.CATEGORIES) {
            queryBuilder.append(String.format(
                    "node(around:%d,%f,%f)[%s~\"%s\"];",
                    category.radius(), lat, lon, category.type(), category.tags()
            ));
        }

        queryBuilder.append(");out;");
        return queryBuilder.toString();
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

        return null;
    }

    private String determineCategory(NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse place) {
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


}