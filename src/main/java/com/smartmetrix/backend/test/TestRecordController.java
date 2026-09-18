package com.smartmetrix.backend.test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-records")
public class TestRecordController {

    private final TestRecordService testRecordService;

    public TestRecordController(TestRecordService testRecordService) {
        this.testRecordService = testRecordService;
    }

    // Inspector creates test record
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public TestRecord createTestRecord(@RequestBody TestRecord testRecord) {
        return testRecordService.createTestRecord(testRecord);
    }

    // All authorized roles can view test records
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_SENIOR_OFFICER', 'ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public List<TestRecord> getAllTestRecords() {
        return testRecordService.getAllTestRecords();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_SENIOR_OFFICER', 'ROLE_CONTROLLER', 'ROLE_ADMIN')")
    public TestRecord getTestRecordById(@PathVariable Long id) {
        return testRecordService.getTestRecordById(id);
    }

    // Inspector can update test record
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public TestRecord updateTestRecord(
            @PathVariable Long id,
            @RequestBody TestRecord testRecord) {

        return testRecordService.updateTestRecord(id, testRecord);
    }

    // Only Inspector/Admin can delete test record
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_INSPECTOR', 'ROLE_ADMIN')")
    public void deleteTestRecord(@PathVariable Long id) {
        testRecordService.deleteTestRecord(id);
    }
}