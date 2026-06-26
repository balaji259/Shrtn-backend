package com.shrtn.services;

import com.shrtn.dto.ClickEventDTO;
import com.shrtn.dto.UrlMappingDTO;
import com.shrtn.models.ClickEvent;
import com.shrtn.models.UrlMapping;
import com.shrtn.models.User;
import com.shrtn.repository.ClickEventRepository;
import com.shrtn.repository.UrlMappingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UrlMappingService {

    private UrlMappingRepository urlMappingRepository;
    private ClickEventRepository clickEventRepository;
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UrlMappingDTO createShortUrl(String originalUrl, String customSlug, LocalDateTime expirationDate, Integer clickLimit, String password, boolean oneTime, User user){

        String shortUrl;
        if (customSlug != null && !customSlug.trim().isEmpty()) {
            customSlug = customSlug.trim();
            if (!customSlug.matches("^[a-zA-Z0-9\\-_]+$")) {
                throw new IllegalArgumentException("Custom slug must only contain letters, numbers, hyphens, and underscores");
            }
            if (urlMappingRepository.findByShortUrl(customSlug) != null) {
                throw new IllegalArgumentException("Custom slug is already in use");
            }
            shortUrl = customSlug;
        } else {
            shortUrl = generateUniqueShortUrl();
        }

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        urlMapping.setExpirationDate(expirationDate);
        urlMapping.setClickLimit(clickLimit);
        urlMapping.setOneTime(oneTime);
        if (password != null && !password.trim().isEmpty()) {
            urlMapping.setPassword(passwordEncoder.encode(password.trim()));
        }

        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);

        return convertToDTO(savedUrlMapping);

    }

    private UrlMappingDTO convertToDTO(UrlMapping urlMapping){
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setCreatedDate(urlMapping.getCreatedDate());
        urlMappingDTO.setExpirationDate(urlMapping.getExpirationDate());
        urlMappingDTO.setClickLimit(urlMapping.getClickLimit());
        urlMappingDTO.setOneTime(urlMapping.isOneTime());
        urlMappingDTO.setPasswordProtected(urlMapping.getPassword() != null && !urlMapping.getPassword().isEmpty());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDTO;
    }

    private String generateUniqueShortUrl() {
        String shortUrl;
        do {
            shortUrl = generateShortUrl();
        } while (urlMappingRepository.findByShortUrl(shortUrl) != null);
        return shortUrl;
    }

    private String generateShortUrl() {
        String Characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);
        for(int i=0;i<8;i++){
            shortUrl.append(Characters.charAt(random.nextInt(Characters.length())));
        }
        return shortUrl.toString();
    }

    public List<UrlMappingDTO> getUrlsByUser(User user) {

        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .toList();

    }

    public Map<String, Object> getUrlAnalyticsAndMetadata(String shortUrl, LocalDateTime start, LocalDateTime end) {

        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if(urlMapping != null){
            List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping,start,end);

            List<ClickEventDTO> clickDateCounts = clickEvents.stream()
                    .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> {
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey());
                        clickEventDTO.setCount(entry.getValue());
                        return clickEventDTO;
                    })
                    .collect(Collectors.toList());

            Map<String, Long> referrerCounts = clickEvents.stream()
                    .collect(Collectors.groupingBy(click -> click.getReferrer() != null ? click.getReferrer() : "Direct", Collectors.counting()));

            Map<String, Long> browserCounts = clickEvents.stream()
                    .collect(Collectors.groupingBy(click -> click.getBrowser() != null ? click.getBrowser() : "Unknown", Collectors.counting()));

            Map<String, Long> osCounts = clickEvents.stream()
                    .collect(Collectors.groupingBy(click -> click.getOperatingSystem() != null ? click.getOperatingSystem() : "Unknown", Collectors.counting()));

            Map<String, Long> deviceCounts = clickEvents.stream()
                    .collect(Collectors.groupingBy(click -> click.getDeviceType() != null ? click.getDeviceType() : "Desktop", Collectors.counting()));

            return Map.of(
                "clickEvents", clickDateCounts,
                "referrer", referrerCounts,
                "browser", browserCounts,
                "os", osCounts,
                "device", deviceCounts
            );
        }
        return null;

    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {

        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        if (urlMappings.isEmpty()) {
            return Map.of();
        }
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(urlMappings, start.atStartOfDay(), end.plusDays(1).atStartOfDay());

        return clickEvents.stream()
                .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()));


    }

    public UrlMapping getOriginalUrl(String shortUrl, String referrer, String userAgent) {

        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);

        if(urlMapping != null){
            if (urlMapping.getExpirationDate() != null && urlMapping.getExpirationDate().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("This link has expired");
            }
            if (urlMapping.getClickLimit() != null && urlMapping.getClickCount() >= urlMapping.getClickLimit()) {
                throw new IllegalStateException("This link has reached its click limit");
            }
            if (urlMapping.isOneTime() && urlMapping.getClickCount() >= 1) {
                throw new IllegalStateException("This one-time link has already been visited");
            }

            urlMapping.setClickCount(urlMapping.getClickCount() + 1);
            urlMappingRepository.save(urlMapping);

            //Record Click Event
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now());
            clickEvent.setUrlMapping(urlMapping);
            clickEvent.setReferrer(referrer != null && !referrer.trim().isEmpty() ? referrer : "Direct");
            clickEvent.setUserAgent(userAgent);
            clickEvent.setBrowser(parseBrowser(userAgent));
            clickEvent.setOperatingSystem(parseOS(userAgent));
            clickEvent.setDeviceType(parseDeviceType(userAgent));
            clickEventRepository.save(clickEvent);

        }

        return urlMapping;
    }

    public UrlMapping getUrlMapping(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl);
    }

    public String verifyAndPasswordRedirect(String shortUrl, String password, String referrer, String userAgent) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping == null) {
            throw new IllegalArgumentException("Short URL not found");
        }

        if (urlMapping.getPassword() == null || urlMapping.getPassword().isEmpty()) {
            throw new IllegalArgumentException("This link is not password protected");
        }

        if (password == null || !passwordEncoder.matches(password.trim(), urlMapping.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        if (urlMapping.getExpirationDate() != null && urlMapping.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This link has expired");
        }

        if (urlMapping.getClickLimit() != null && urlMapping.getClickCount() >= urlMapping.getClickLimit()) {
            throw new IllegalStateException("This link has reached its click limit");
        }

        if (urlMapping.isOneTime() && urlMapping.getClickCount() >= 1) {
            throw new IllegalStateException("This one-time link has already been visited");
        }

        urlMapping.setClickCount(urlMapping.getClickCount() + 1);
        urlMappingRepository.save(urlMapping);

        // Record Click Event
        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setClickDate(LocalDateTime.now());
        clickEvent.setUrlMapping(urlMapping);
        clickEvent.setReferrer(referrer != null && !referrer.trim().isEmpty() ? referrer : "Direct");
        clickEvent.setUserAgent(userAgent);
        clickEvent.setBrowser(parseBrowser(userAgent));
        clickEvent.setOperatingSystem(parseOS(userAgent));
        clickEvent.setDeviceType(parseDeviceType(userAgent));
        clickEventRepository.save(clickEvent);

        return urlMapping.getOriginalUrl();
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        String lower = ua.toLowerCase();
        if (lower.contains("edg")) return "Edge";
        if (lower.contains("opr") || lower.contains("opera")) return "Opera";
        if (lower.contains("chrome") && !lower.contains("chromium")) return "Chrome";
        if (lower.contains("safari") && lower.contains("chrome")) return "Chrome";
        if (lower.contains("safari")) return "Safari";
        if (lower.contains("firefox")) return "Firefox";
        return "Other";
    }

    private String parseOS(String ua) {
        if (ua == null) return "Unknown";
        String lower = ua.toLowerCase();
        if (lower.contains("windows")) return "Windows";
        if (lower.contains("macintosh") || lower.contains("mac os x")) return "macOS";
        if (lower.contains("android")) return "Android";
        if (lower.contains("iphone") || lower.contains("ipad")) return "iOS";
        if (lower.contains("linux")) return "Linux";
        return "Other";
    }

    private String parseDeviceType(String ua) {
        if (ua == null) return "Desktop";
        String lower = ua.toLowerCase();
        if (lower.contains("ipad") || (lower.contains("macintosh") && lower.contains("touch"))) return "Tablet";
        if (lower.contains("mobi") || lower.contains("iphone") || lower.contains("android")) return "Mobile";
        return "Desktop";
    }
}
