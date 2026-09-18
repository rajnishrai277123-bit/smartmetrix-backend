package com.smartmetrix.backend.sync;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;
import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;
import com.smartmetrix.backend.test.TestRecordService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {

    private final InspectionRepository inspectionRepository;
    private final TestRecordService testRecordService;
    private final TestRecordRepository testRecordRepository;

    public SyncService(
            InspectionRepository inspectionRepository,
            TestRecordService testRecordService,
            TestRecordRepository testRecordRepository) {

        this.inspectionRepository = inspectionRepository;
        this.testRecordService = testRecordService;
        this.testRecordRepository = testRecordRepository;
    }

    public SyncTestRecordResponse syncTestRecords(
            SyncTestRecordRequest request) {

        if (request == null ||
                request.getRecords() == null) {

            throw new IllegalArgumentException(
                    "Records are required");
        }

        List<String> syncedIds =
                new ArrayList<>();

        List<String> failedIds =
                new ArrayList<>();

        int total =
                request.getRecords().size();

        for (SyncTestRecordRequest.OfflineTestRecord offlineRecord
                : request.getRecords()) {

            String clientRecordId =
                    offlineRecord != null
                            ? offlineRecord.getClientRecordId()
                            : null;

            try {

                validateRecord(offlineRecord);

                /*
                 * IDEMPOTENCY CHECK
                 *
                 * If this offline record was already synced,
                 * do not create another TestRecord.
                 */
                if (testRecordRepository
                        .findByClientRecordId(clientRecordId)
                        .isPresent()) {

                    syncedIds.add(clientRecordId);

                    continue;
                }

                Inspection inspection =
                        inspectionRepository
                                .findById(
                                        offlineRecord.getInspectionId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Inspection not found: "
                                                        + offlineRecord.getInspectionId()
                                        ));

                TestRecord testRecord =
                        new TestRecord();

                testRecord.setClientRecordId(
                        clientRecordId);

                testRecord.setInspectionId(
                        inspection.getId());

                testRecord.setTestType(
                        offlineRecord.getTestType());

                testRecord.setReferenceWeight(
                        offlineRecord.getReferenceWeight());

                testRecord.setObservedWeight(
                        offlineRecord.getObservedWeight());

                testRecord.setTemperature(
                        offlineRecord.getTemperature());

                testRecord.setHumidity(
                        offlineRecord.getHumidity());

                testRecord.setVibration(
                        offlineRecord.getVibration());

                testRecord.setTestStage(
                        offlineRecord.getTestStage());

                /*
                 * OIML calculation is handled by
                 * TestRecordService.
                 */

                testRecordService.createTestRecord(
                        testRecord);

                syncedIds.add(clientRecordId);

            } catch (Exception e) {

                if (clientRecordId != null) {
                    failedIds.add(clientRecordId);
                }
            }
        }

        return new SyncTestRecordResponse(
                total,
                syncedIds.size(),
                failedIds.size(),
                syncedIds,
                failedIds
        );
    }

    private void validateRecord(
            SyncTestRecordRequest.OfflineTestRecord record) {

        if (record == null) {
            throw new IllegalArgumentException(
                    "Record cannot be null");
        }

        if (record.getClientRecordId() == null ||
                record.getClientRecordId().isBlank()) {

            throw new IllegalArgumentException(
                    "Client record ID is required");
        }

        if (record.getInspectionId() == null) {

            throw new IllegalArgumentException(
                    "Inspection ID is required");
        }

        if (record.getTestType() == null ||
                record.getTestType().isBlank()) {

            throw new IllegalArgumentException(
                    "Test type is required");
        }

        if (record.getReferenceWeight() == null ||
                record.getReferenceWeight() < 0) {

            throw new IllegalArgumentException(
                    "Reference weight must be zero or greater");
        }

        if (record.getObservedWeight() == null ||
                record.getObservedWeight() < 0) {

            throw new IllegalArgumentException(
                    "Observed weight must be zero or greater");
        }
    }
}