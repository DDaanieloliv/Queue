package com.ddaaniel.queue.controller;

import com.ddaaniel.queue.domain.model.dto.AllDataResponseDto;
import com.ddaaniel.queue.service.BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/data")
public class DataExportController {

    private final BackupService queueService;

    public DataExportController(BackupService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/export")
    public ResponseEntity<AllDataResponseDto> exportAllData() {
        try {
            AllDataResponseDto data = queueService.getAllData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AllDataResponseDto(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        }
    }
}