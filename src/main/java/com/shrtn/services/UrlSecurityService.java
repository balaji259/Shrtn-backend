package com.shrtn.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.net.URI;
import java.util.Map;

@Service
public class UrlSecurityService {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isSafeUrl(String urlToCheck) {
        if (urlToCheck == null || urlToCheck.trim().isEmpty()) {
            return true;
        }

        // 1. Heuristic Scan (Offline checks)
        if (containsSuspiciousPatterns(urlToCheck)) {
            return false;
        }

        // 2. Online threat intelligence check (URLhaus API)
        try {
            String urlhausApi = "https://urlhaus-api.abuse.ch/v1/url/";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("url", urlToCheck);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(urlhausApi, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> body = response.getBody();
                String status = (String) body.get("query_status");
                if ("ok".equals(status)) {
                    // Match found in URLhaus database -> Malware/suspicious site!
                    String urlStatus = (String) body.get("url_status");
                    if ("online".equals(urlStatus) || "offline".equals(urlStatus)) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            // Log warning but don't block the link creation in case URLhaus is offline
            System.err.println("URLhaus security scan failed: " + e.getMessage());
        }

        return true;
    }

    private boolean containsSuspiciousPatterns(String url) {
        String lowerUrl = url.toLowerCase();

        // Check if hostname is a raw IP address
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null && host.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                return true; 
            }
        } catch (Exception e) {
            // Ignore parse errors, let standard validation handle it
        }

        // Suspected phishing keywords in domain / path segments
        String[] suspiciousKeywords = {
            "secure-login", "bank-update", "paypal-security", "verify-account", 
            "signin-verification", "billing-support", "update-credentials", "signin-amazon",
            "netbanking-login", "apple-id-verify"
        };
        for (String keyword : suspiciousKeywords) {
            if (lowerUrl.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}
