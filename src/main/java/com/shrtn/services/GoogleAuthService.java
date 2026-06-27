package com.shrtn.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> verifyToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new RuntimeException("Invalid token response from Google");
            }
            if (response.containsKey("error")) {
                throw new RuntimeException("Google token verification failed: " + response.get("error_description"));
            }

            // Validate client ID (aud)
            String aud = (String) response.get("aud");
            if (googleClientId == null || googleClientId.trim().isEmpty() || "your-google-client-id-here.apps.googleusercontent.com".equals(googleClientId)) {
                throw new RuntimeException("Google Client ID is not configured on the backend. Please check your .env / environment variables.");
            }
            if (!googleClientId.equals(aud)) {
                throw new RuntimeException("Token audience does not match backend Client ID");
            }

            // Verify email_verified is true
            Object emailVerifiedObj = response.get("email_verified");
            boolean emailVerified = false;
            if (emailVerifiedObj instanceof Boolean) {
                emailVerified = (Boolean) emailVerifiedObj;
            } else if (emailVerifiedObj instanceof String) {
                emailVerified = Boolean.parseBoolean((String) emailVerifiedObj);
            }

            if (!emailVerified) {
                throw new RuntimeException("Google email is not verified");
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }
}
