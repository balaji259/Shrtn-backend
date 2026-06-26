package com.shrtn.controllers;

import com.shrtn.models.UrlMapping;
import com.shrtn.services.UrlMappingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlMappingService urlMappingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest request){
        try {
            String referrer = request.getHeader("Referer");
            String userAgent = request.getHeader("User-Agent");
            UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl, referrer, userAgent);
            if(urlMapping!=null){
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("Location", urlMapping.getOriginalUrl());
                return ResponseEntity.status(302).headers(httpHeaders).build();
            }else{
                HttpHeaders httpHeaders = new HttpHeaders();
                String errorUrl = frontendUrl + "/error?message=" + UriUtils.encode("This short link does not exist or has been deleted", StandardCharsets.UTF_8);
                httpHeaders.add("Location", errorUrl);
                return ResponseEntity.status(302).headers(httpHeaders).build();
            }
        } catch (IllegalStateException e) {
            HttpHeaders httpHeaders = new HttpHeaders();
            String errorUrl = frontendUrl + "/error?message=" + UriUtils.encode(e.getMessage(), StandardCharsets.UTF_8);
            httpHeaders.add("Location", errorUrl);
            return ResponseEntity.status(302).headers(httpHeaders).build();
        }
    }

}
