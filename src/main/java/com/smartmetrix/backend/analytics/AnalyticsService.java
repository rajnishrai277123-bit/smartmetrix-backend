package com.smartmetrix.backend.analytics;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final InspectionRepository inspectionRepository;
    private final TestRecordRepository testRecordRepository;

    public AnalyticsService(
            InspectionRepository inspectionRepository,
            TestRecordRepository testRecordRepository) {

        this.inspectionRepository =
                inspectionRepository;

        this.testRecordRepository =
                testRecordRepository;
    }

    public AnalyticsResponse getAnalytics() {

        // -----------------------------------
        // Inspection statistics
        // -----------------------------------

        List<Inspection> inspections =
                inspectionRepository.findAll();

        long totalInspections =
                inspections.size();

        long passInspections =
                inspections.stream()
                        .filter(inspection ->
                                Inspection.RESULT_PASS.equals(
                                        inspection.getOverallResult()))
                        .count();

        long failInspections =
                inspections.stream()
                        .filter(inspection ->
                                Inspection.RESULT_FAIL.equals(
                                        inspection.getOverallResult()))
                        .count();

        long pendingInspections =
                inspections.stream()
                        .filter(inspection ->
                                inspection.getOverallResult() == null
                                        || Inspection.RESULT_PENDING.equals(
                                        inspection.getOverallResult()))
                        .count();

        // -----------------------------------
        // Test record statistics
        // -----------------------------------

        List<TestRecord> testRecords =
                testRecordRepository.findAll();

        long totalTestRecords =
                testRecords.size();

        long passTestRecords =
                testRecords.stream()
                        .filter(testRecord ->
                                "PASS".equals(
                                        testRecord.getResult()))
                        .count();

        long failTestRecords =
                testRecords.stream()
                        .filter(testRecord ->
                                "FAIL".equals(
                                        testRecord.getResult()))
                        .count();

        // -----------------------------------
        // Percentages
        // -----------------------------------

        double inspectionPassPercentage =
                calculatePercentage(
                        passInspections,
                        totalInspections);

        double inspectionFailPercentage =
                calculatePercentage(
                        failInspections,
                        totalInspections);

        double testPassPercentage =
                calculatePercentage(
                        passTestRecords,
                        totalTestRecords);

        double testFailPercentage =
                calculatePercentage(
                        failTestRecords,
                        totalTestRecords);

        return new AnalyticsResponse(
                totalInspections,
                passInspections,
                failInspections,
                pendingInspections,
                totalTestRecords,
                passTestRecords,
                failTestRecords,
                inspectionPassPercentage,
                inspectionFailPercentage,
                testPassPercentage,
                testFailPercentage
        );
    }

    private double calculatePercentage(
            long value,
            long total) {

        if (total == 0) {
            return 0.0;
        }

        double percentage =
                ((double) value / total) * 100;

        return Math.round(
                percentage * 100.0
        ) / 100.0;
    }
}