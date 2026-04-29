package com.example.aiservice;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://127.0.0.1:5000";

    public AiServiceClient() {
        this.restTemplate = createRestTemplate();
    }

    //  Create RestTemplate with 10s timeout
    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds
        factory.setReadTimeout(10000);    // 10 seconds
        return new RestTemplate(factory);
    }

    //  Common headers
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ===============================
    // 🔹 POST /test
    // ===============================
    public Map<String, Object> callTest(String text) {
        try {
            String url = BASE_URL + "/test";

            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, getHeaders());

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            return response.getBody();

        } catch (RestClientException e) {
            System.out.println("Error calling /test: " + e.getMessage());
            return null;
        }
    }

    // ===============================
    // 🔹 POST /generate-report
    // ===============================
    public Map<String, Object> generateReport(String text) {
        try {
            String url = BASE_URL + "/generate-report";

            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, getHeaders());

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            return response.getBody();

        } catch (RestClientException e) {
            System.out.println("Error calling /generate-report: " + e.getMessage());
            return null;
        }
    }

    // ===============================
    //  GET /health
    // ===============================
    public Map<String, Object> healthCheck() {
        try {
            String url = BASE_URL + "/health";

            ResponseEntity<Map> response =
                    restTemplate.getForEntity(url, Map.class);

            return response.getBody();

        } catch (RestClientException e) {
            System.out.println("Error calling /health: " + e.getMessage());
            return null;
        }
    }

    // ===============================
    //  Optional: Generic POST method
    // ===============================
    public Map<String, Object> postRequest(String endpoint, Map<String, String> body) {
        try {
            String url = BASE_URL + endpoint;

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, getHeaders());

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            return response.getBody();

        } catch (RestClientException e) {
            System.out.println("Error calling " + endpoint + ": " + e.getMessage());
            return null;
        }
    }
}