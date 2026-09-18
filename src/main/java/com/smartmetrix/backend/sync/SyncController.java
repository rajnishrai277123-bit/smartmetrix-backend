package com.smartmetrix.backend.sync;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(
            SyncService syncService) {

        this.syncService =
                syncService;
    }

    @PostMapping("/test-records")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'ROLE_INSPECTOR', " +
                    "'ROLE_ADMIN')")
    public SyncTestRecordResponse syncTestRecords(
            @RequestBody SyncTestRecordRequest request) {

        return syncService.syncTestRecords(
                request
        );
    }
}