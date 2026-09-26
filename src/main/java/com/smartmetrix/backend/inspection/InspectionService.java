package com.smartmetrix.backend.inspection;

import com.smartmetrix.backend.approval.Approval;
import com.smartmetrix.backend.approval.ApprovalRepository;
import com.smartmetrix.backend.audit.AuditLogService;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import com.smartmetrix.backend.instrument.exception.InstrumentNotFoundException;
import com.smartmetrix.backend.inspection.exception.InspectionNotFoundException;
import com.smartmetrix.backend.test.EccentricityRecord;
import com.smartmetrix.backend.test.EccentricityRecordRepository;
import com.smartmetrix.backend.test.RepeatabilityRecord;
import com.smartmetrix.backend.test.RepeatabilityRecordRepository;
import com.smartmetrix.backend.test.RepeatabilityRecordService;
import com.smartmetrix.backend.test.RepeatabilitySummaryResponse;
import com.smartmetrix.backend.test.EccentricityRecordService;
import com.smartmetrix.backend.test.EccentricitySummaryResponse;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;
    private final ApprovalRepository approvalRepository;
    private final AuditLogService auditLogService;
    private final TestRecordRepository testRecordRepository;

    private final RepeatabilityRecordRepository repeatabilityRecordRepository;
    private final EccentricityRecordRepository eccentricityRecordRepository;

    private final RepeatabilityRecordService repeatabilityRecordService;
    private final EccentricityRecordService eccentricityRecordService;

    public InspectionService(
            InspectionRepository inspectionRepository,
            InstrumentRepository instrumentRepository,
            ApprovalRepository approvalRepository,
            AuditLogService auditLogService,
            TestRecordRepository testRecordRepository,
            RepeatabilityRecordRepository repeatabilityRecordRepository,
            EccentricityRecordRepository eccentricityRecordRepository,
            RepeatabilityRecordService repeatabilityRecordService,
            EccentricityRecordService eccentricityRecordService) {

        this.inspectionRepository = inspectionRepository;
        this.instrumentRepository = instrumentRepository;
        this.approvalRepository = approvalRepository;
        this.auditLogService = auditLogService;
        this.testRecordRepository = testRecordRepository;

        this.repeatabilityRecordRepository =
                repeatabilityRecordRepository;

        this.eccentricityRecordRepository =
                eccentricityRecordRepository;

        this.repeatabilityRecordService =
                repeatabilityRecordService;

        this.eccentricityRecordService =
                eccentricityRecordService;
    }

    public Inspection createInspection(Inspection inspection) {

        instrumentRepository.findById(
                        inspection.getInstrumentId())
                .orElseThrow(() ->
                        new InstrumentNotFoundException(
                                "Instrument not found"));

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Long currentUserId =
                (Long) authentication.getCredentials();
        inspection.setInspectorId(currentUserId);
        inspection.setStatus(Inspection.IN_PROGRESS);
        inspection.setOverallResult(Inspection.RESULT_PENDING);

        /*
         * Store the exact time when inspection is created.
         */
        inspection.setCreatedAt(LocalDateTime.now());

        /*
         * Completion time is still empty because
         * inspection has not completed yet.
         */
        inspection.setCompletedAt(null);
        Inspection savedInspection =
                inspectionRepository.save(inspection);

        auditLogService.createLog(
                currentUserId,
                "INSPECTION_CREATED",
                "INSPECTION",
                savedInspection.getId(),
                null,
                "Inspection created"
        );

        return savedInspection;
    }

    public List<Inspection> getAllInspections() {
        return inspectionRepository.findAll();
    }

    public Inspection getInspectionById(Long id) {

        return inspectionRepository.findById(id)
                .orElseThrow(() ->
                        new InspectionNotFoundException(
                                "Inspection not found"));
    }

    private String calculateOverallResult(Long inspectionId) {

        List<TestRecord> allTestRecords =
                testRecordRepository
                        .findByInspectionId(inspectionId);

        List<TestRecord> weighingRecords =
                allTestRecords.stream()
                        .filter(record ->
                                "WEIGHING_PERFORMANCE"
                                        .equalsIgnoreCase(
                                                record.getTestType()))
                        .toList();

        if (weighingRecords.isEmpty()) {
            return Inspection.RESULT_PENDING;
        }

        TestRecord latestWeighing =
                weighingRecords.stream()
                        .max(Comparator.comparing(
                                TestRecord::getId))
                        .orElse(null);

        if (latestWeighing == null) {
            return Inspection.RESULT_PENDING;
        }

        String weighingResult =
                latestWeighing.getResult();

        List<RepeatabilityRecord> repeatabilityRecords =
                repeatabilityRecordRepository
                        .findByInspectionId(inspectionId);

        if (repeatabilityRecords.isEmpty()) {
            return Inspection.RESULT_PENDING;
        }

        Long latestTestRunId =
                repeatabilityRecords.stream()
                        .filter(record ->
                                record.getTestRunId() != null)
                        .map(RepeatabilityRecord::getTestRunId)
                        .max(Long::compareTo)
                        .orElse(null);

        if (latestTestRunId == null) {
            return Inspection.RESULT_PENDING;
        }

        RepeatabilitySummaryResponse
                repeatabilitySummary;

        try {
            repeatabilitySummary =
                    repeatabilityRecordService
                            .getSummary(latestTestRunId);
        } catch (IllegalArgumentException ex) {
            return Inspection.RESULT_PENDING;
        }

        String repeatabilityResult =
                repeatabilitySummary.getResult();

        List<EccentricityRecord> eccentricityRecords =
                eccentricityRecordRepository
                        .findByInspectionId(inspectionId);

        if (eccentricityRecords.isEmpty()) {
            return Inspection.RESULT_PENDING;
        }

        if (eccentricityRecords.size() < 5) {
            return Inspection.RESULT_PENDING;
        }

        EccentricitySummaryResponse
                eccentricitySummary;

        try {
            eccentricitySummary =
                    eccentricityRecordService
                            .getSummary(inspectionId);
        } catch (IllegalArgumentException ex) {
            return Inspection.RESULT_PENDING;
        }

        String eccentricityResult =
                eccentricitySummary.getResult();

        if ("FAIL".equalsIgnoreCase(weighingResult)
                || "FAIL".equalsIgnoreCase(repeatabilityResult)
                || "FAIL".equalsIgnoreCase(eccentricityResult)) {

            return Inspection.RESULT_FAIL;
        }

        if (!"PASS".equalsIgnoreCase(weighingResult)
                || !"PASS".equalsIgnoreCase(repeatabilityResult)
                || !"PASS".equalsIgnoreCase(eccentricityResult)) {

            return Inspection.RESULT_PENDING;
        }

        return Inspection.RESULT_PASS;
    }

    public Inspection completeInspection(Long id) {

        Inspection inspection =
                inspectionRepository.findById(id)
                        .orElseThrow(() ->
                                new InspectionNotFoundException(
                                        "Inspection not found"));

        String oldStatus =
                inspection.getStatus();

        String overallResult =
                calculateOverallResult(id);

        if (Inspection.RESULT_PENDING.equals(
                overallResult)) {

            throw new IllegalStateException(
                    "All three tests must be completed before completing inspection");
        }

        inspection.setOverallResult(overallResult);
        inspection.setStatus(Inspection.COMPLETED);

        inspection.setCompletedAt(
                LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
        );
        Inspection savedInspection =
                inspectionRepository.save(inspection);

        auditLogService.createLog(
                inspection.getInspectorId(),
                "INSPECTION_COMPLETED",
                "INSPECTION",
                id,
                oldStatus,
                Inspection.COMPLETED
        );

        return savedInspection;
    }

    public Inspection submitInspection(Long id) {

        Inspection inspection =
                inspectionRepository.findById(id)
                        .orElseThrow(() ->
                                new InspectionNotFoundException(
                                        "Inspection not found"));

        if (!Inspection.COMPLETED.equals(
                inspection.getStatus())) {

            throw new IllegalStateException(
                    "Inspection must be completed before submission");
        }

        String overallResult =
                calculateOverallResult(id);

        if (Inspection.RESULT_PENDING.equals(
                overallResult)) {

            throw new IllegalStateException(
                    "All three tests must be completed before submission");
        }

        inspection.setOverallResult(overallResult);

        String oldStatus =
                inspection.getStatus();

        inspection.setStatus(Inspection.SUBMITTED);

        Inspection savedInspection =
                inspectionRepository.save(inspection);

        auditLogService.createLog(
                inspection.getInspectorId(),
                "INSPECTION_SUBMITTED",
                "INSPECTION",
                id,
                oldStatus,
                Inspection.SUBMITTED
        );

        return savedInspection;
    }

    public Inspection approveInspection(Long id) {

        Inspection inspection =
                inspectionRepository.findById(id)
                        .orElseThrow(() ->
                                new InspectionNotFoundException(
                                        "Inspection not found"));

        if (!Inspection.SUBMITTED.equals(
                inspection.getStatus())) {

            throw new IllegalStateException(
                    "Only submitted inspections can be approved");
        }

        if (!Inspection.RESULT_PASS.equals(
                inspection.getOverallResult())) {

            throw new IllegalStateException(
                    "Only inspections with PASS result can be approved");
        }

        String oldStatus =
                inspection.getStatus();

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Long currentUserId =
                (Long) authentication.getCredentials();

        inspection.setStatus(Inspection.APPROVED);

        Approval approval =
                new Approval();

        approval.setInspectionId(id);
        approval.setUserId(currentUserId);
        approval.setAction("APPROVED");
        approval.setRemarks(
                "Inspection approved successfully");

        approvalRepository.save(approval);

        Inspection savedInspection =
                inspectionRepository.save(inspection);

        auditLogService.createLog(
                currentUserId,
                "INSPECTION_APPROVED",
                "INSPECTION",
                id,
                oldStatus,
                Inspection.APPROVED
        );

        return savedInspection;
    }

    public Inspection controllerApproveInspection(Long id) {

        Inspection inspection =
                inspectionRepository.findById(id)
                        .orElseThrow(() ->
                                new InspectionNotFoundException(
                                        "Inspection not found"));

        // Senior Officer approval is required first
        if (!Inspection.APPROVED.equals(
                inspection.getStatus())) {

            throw new IllegalStateException(
                    "Inspection must be approved by Senior Officer first");
        }

        // Controller approval is allowed only for PASS inspections
        if (!Inspection.RESULT_PASS.equals(
                inspection.getOverallResult())) {

            throw new IllegalStateException(
                    "Only inspections with PASS result can be approved by Controller");
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Long currentUserId =
                (Long) authentication.getCredentials();

        String oldStatus =
                inspection.getStatus();

        inspection.setStatus(
                Inspection.CONTROLLER_APPROVED);

        Approval approval =
                new Approval();

        approval.setInspectionId(id);
        approval.setUserId(currentUserId);
        approval.setAction(
                "CONTROLLER_APPROVED");

        approval.setRemarks(
                "Inspection finally approved by Controller");

        approvalRepository.save(approval);

        Inspection savedInspection =
                inspectionRepository.save(inspection);

        auditLogService.createLog(
                currentUserId,
                "INSPECTION_CONTROLLER_APPROVED",
                "INSPECTION",
                id,
                oldStatus,
                Inspection.CONTROLLER_APPROVED
        );

        return savedInspection;
    }
}