package com.url.controllers;

import com.url.dto.UrlMappingDtO;
import com.url.models.User;
import com.url.services.UrlMappingService;
import com.url.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {

    private UrlMappingService urlMappingService;
    private UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDtO> createShortUrl(@RequestBody Map<String,String> request, Principal principal){

            String originalUrl = request.get("originalUrl");
            System.out.println(originalUrl);
            User user = userService.findByEmail(principal.getName());
            System.out.println("User ->");
            System.out.println(user);
            UrlMappingDtO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
            return ResponseEntity.ok(urlMappingDTO);


    }



}
