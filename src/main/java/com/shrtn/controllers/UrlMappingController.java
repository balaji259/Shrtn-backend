package com.shrtn.controllers;

import com.shrtn.dto.ClickEventDTO;
import com.shrtn.dto.UrlMappingDTO;
import com.shrtn.models.ClickEvent;
import com.shrtn.models.User;
import com.shrtn.services.UrlMappingService;
import com.shrtn.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {

    private UrlMappingService urlMappingService;
    private UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createShortUrl(@RequestBody Map<String,Object> request, Principal principal){

        try {
            String originalUrl = (String) request.get("originalUrl");
            String customSlug = (String) request.get("customSlug");
            String expDateStr = (String) request.get("expirationDate");
            String clickLimitStr = (String) request.get("clickLimit");
            String password = (String) request.get("password");
            
            boolean oneTime = false;
            Object oneTimeVal = request.get("oneTime");
            if (oneTimeVal instanceof Boolean) {
                oneTime = (Boolean) oneTimeVal;
            } else if (oneTimeVal instanceof String) {
                oneTime = Boolean.parseBoolean((String) oneTimeVal);
            }

            LocalDateTime expirationDate = null;
            if (expDateStr != null && !expDateStr.trim().isEmpty()) {
                expirationDate = LocalDateTime.parse(expDateStr);
            }

            Integer clickLimit = null;
            if (clickLimitStr != null && !clickLimitStr.trim().isEmpty()) {
                clickLimit = Integer.parseInt(clickLimitStr);
            }

            User user = userService.findByEmail(principal.getName());
            UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, customSlug, expirationDate, clickLimit, password, oneTime, user);
            return ResponseEntity.ok(urlMappingDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

    }

    @PostMapping("/verify-password/{shortUrl}")
    public ResponseEntity<?> verifyPassword(@PathVariable String shortUrl, @RequestBody Map<String, String> request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            String password = request.get("password");
            String referrer = httpRequest.getHeader("Referer");
            String userAgent = httpRequest.getHeader("User-Agent");
            
            String originalUrl = urlMappingService.verifyAndPasswordRedirect(shortUrl, password, referrer, userAgent);
            return ResponseEntity.ok(Map.of("originalUrl", originalUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrls(Principal principal){
        User user = userService.findByEmail(principal.getName());
        List<UrlMappingDTO> urls = urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUrlAnalytics(@PathVariable String shortUrl, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        Map<String, Object> analytics = urlMappingService.getUrlAnalyticsAndMetadata(shortUrl,start,end);
        return ResponseEntity.ok(analytics);

    }

    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicksByDate(Principal principal,  @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        User user = userService.findByEmail(principal.getName());
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        Map<LocalDate , Long> totalClicks = urlMappingService.getTotalClicksByUserAndDate(user, start, end);
        return ResponseEntity.ok(totalClicks);

    }




}
