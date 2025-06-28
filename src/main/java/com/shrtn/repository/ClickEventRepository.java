package com.shrtn.repository;

import com.shrtn.models.ClickEvent;
import com.shrtn.models.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository extends JpaRepository <ClickEvent,Long> {

    List<ClickEvent> findByUrlMappingAndClickDateBetween(UrlMapping mapping, LocalDateTime startDate, LocalDateTime endDate);
    List<ClickEvent> findByUrlMappingInAndClickDateBetween(List<UrlMapping> mappings, LocalDateTime startDate, LocalDateTime endDate);

}

