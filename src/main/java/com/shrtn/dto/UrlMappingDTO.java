package com.shrtn.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlMappingDTO {

    private Long id;
    private String originalUrl;
    private String shortUrl;
    private int clickCount;
    private LocalDateTime createdDate;
    private LocalDateTime expirationDate;
    private Integer clickLimit;
    private boolean oneTime;
    private boolean isPasswordProtected;
    private String username;

}
