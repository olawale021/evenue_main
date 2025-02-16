package com.example.evenue.service;

import com.example.evenue.models.events.EventModel;
import com.example.evenue.utils.RecommendationServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RecommendationService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EventService eventService;

    private final String BASE_URL = "http://127.0.0.1:6000/recommend";

    private List<RecommendationDto> mapResponseToDto(List<Map<String, Object>> response) {
        List<RecommendationDto> recommendationDtos = new ArrayList<>();
        for (Map<String, Object> item : response) {
            try {
                RecommendationDto dto = new RecommendationDto();
                dto.setEventId(parseEventId(item.get("event_id")));
                dto.setPredictedRating(parseDouble(item.get("hybrid_score")));
                dto.setEventName(parseString(item.get("event_name")));
                dto.setEventImage(parseString(item.get("event_image")));
                dto.setEventDate(parseDate(item.get("event_date")));
                dto.setLocation(parseString(item.get("location")));
                dto.setTicketPrice(parseDouble(item.get("ticket_price")));
                recommendationDtos.add(dto);
            } catch (Exception e) {
                log.error("Error mapping item to DTO: {}", item, e);
            }
        }
        return recommendationDtos;
    }

    private Long parseEventId(Object value) {
        if (value == null) return null;
        try {
            return value instanceof Number ? ((Number) value).longValue() :
                    Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse event ID: {}", value);
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) return 0.0;
        try {
            if (value instanceof Number) {
                double numValue = ((Number) value).doubleValue();
                return Double.isNaN(numValue) ? 0.0 : numValue;
            }
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double value: {}", value);
            return 0.0;
        }
    }

    private String parseString(Object value) {
        return value == null ? "" : value.toString();
    }

    private LocalDate parseDate(Object value) {
        if (value == null) return LocalDate.now();
        try {
            String dateStr = value.toString();

            // Check if the value is a timestamp (milliseconds since epoch)
            if (dateStr.matches("\\d+")) {
                long timestamp = Long.parseLong(dateStr);
                return Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            // Try parsing as ISO date
            try {
                return LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                // Try parsing with different formats
                DateTimeFormatter[] formatters = {
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                };

                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDate.parse(dateStr, formatter);
                    } catch (DateTimeParseException ex) {
                        continue;
                    }
                }

                throw new DateTimeParseException("Could not parse date with any format", dateStr, 0);
            }
        } catch (Exception e) {
            log.warn("Failed to parse date value: {}. Using current date instead.", value);
            return LocalDate.now();
        }
    }

    private <T> ResponseEntity<T> makeRequest(String endpoint, Long userId, int n,
                                              ParameterizedTypeReference<T> responseType) {
        try {
            String url = String.format("%s/%s?user_id=%d&n=%d", BASE_URL, endpoint, userId, n);
            return restTemplate.exchange(url, HttpMethod.GET, null, responseType);
        } catch (Exception e) {
            log.error("Error making request to recommendation service: {}", endpoint, e);
            throw new RecommendationServiceException("Failed to get recommendations", e);
        }
    }

    public List<RecommendationDto> getHybridRecommendations(Long userId, int n) {
        String url = BASE_URL + "/hybrid?user_id=" + userId + "&n=" + n;
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        // Process response to handle NaN values
        List<Map<String, Object>> processedResponse = handleNaNValues(response.getBody());
        return mapResponseToDto(processedResponse);
    }


    public List<RecommendationDto> getPopularRecommendations(Long userId, int n) {
        try {
            // Build URL - notice how we can still make the request even if userId is null
            String url = BASE_URL + "/popular?n=" + n;
            if (userId != null) {
                url += "&user_id=" + userId;
            }
            log.debug("Requesting popular events. User: {}, URL: {}", userId != null ? userId : "anonymous", url);

            // Make the request
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> processedResponse = handleNaNValues(response.getBody());
                return mapResponseToDto(processedResponse);
            } else {
                log.warn("Unexpected response for popular events. Status: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error fetching popular events for {}user", userId != null ? "user " + userId : "anonymous ", e);
            return new ArrayList<>();
        }
    }

    public List<RecommendationDto> getCategoryRecommendations(Long userId, int n) {
        String url = BASE_URL + "/category?user_id=" + userId + "&n=" + n;
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        // Process response to handle NaN values
        List<Map<String, Object>> processedResponse = handleNaNValues(response.getBody());
        return mapResponseToDto(processedResponse);
    }

    public List<RecommendationDto> getProfileRecommendations(Long userId, int n) {
        String url = BASE_URL + "/profile?user_id=" + userId + "&n=" + n;
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        // Process response to handle NaN values
        List<Map<String, Object>> processedResponse = handleNaNValues(response.getBody());
        return mapResponseToDto(processedResponse);
    }

    public List<RecommendationDto> getFriendsRecommendations(Long userId, int n) {
        String url = BASE_URL + "/friends?user_id=" + userId + "&n=" + n;
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        // Process response to handle NaN values
        List<Map<String, Object>> processedResponse = handleNaNValues(response.getBody());
        return mapResponseToDto(processedResponse);
    }

    private List<Map<String, Object>> handleNaNValues(List<Map<String, Object>> response) {
        for (Map<String, Object> entry : response) {
            entry.replaceAll((k, v) -> v instanceof Double && ((Double) v).isNaN() ? 0 : v);
        }
        return response;
    }

}
