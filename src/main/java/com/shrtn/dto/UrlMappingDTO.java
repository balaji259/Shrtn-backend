package com.shrtn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlMappingDTO {

    private Long id;
    private String originalUrl;
    private String shortUrl;
    private int clickCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime expirationDate;

    private java.lang.Integer clickLimit;
    private boolean oneTime;
    private boolean isPasswordProtected;
    private String username;

}
