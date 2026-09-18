package com.smartmetrix.backend.certificate;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.smartmetrix.backend.environment.EnvironmentRecord;
import com.smartmetrix.backend.environment.EnvironmentRecordRepository;

import com.smartmetrix.backend.inspection.Inspection;
import com.smartmetrix.backend.inspection.InspectionRepository;

import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;

import com.smartmetrix.backend.test.EccentricityRecord;
import com.smartmetrix.backend.test.EccentricityRecordRepository;
import com.smartmetrix.backend.test.EccentricityRecordService;
import com.smartmetrix.backend.test.EccentricitySummaryResponse;
import com.smartmetrix.backend.test.EccentricityPositionResult;

import com.smartmetrix.backend.test.RepeatabilityRecord;
import com.smartmetrix.backend.test.RepeatabilityRecordRepository;
import com.smartmetrix.backend.test.RepeatabilityRecordService;
import com.smartmetrix.backend.test.RepeatabilitySummaryResponse;

import com.smartmetrix.backend.test.TestRecord;
import com.smartmetrix.backend.test.TestRecordRepository;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificatePdfService {

    private final CertificateRepository certificateRepository;
    private final InspectionRepository inspectionRepository;
    private final InstrumentRepository instrumentRepository;
    private final TestRecordRepository testRecordRepository;

    private final EccentricityRecordRepository eccentricityRecordRepository;
    private final EccentricityRecordService eccentricityRecordService;

    private final RepeatabilityRecordRepository repeatabilityRecordRepository;
    private final RepeatabilityRecordService repeatabilityRecordService;

    private final EnvironmentRecordRepository environmentRecordRepository;

    private final QrCodeService qrCodeService;

    public CertificatePdfService(
            CertificateRepository certificateRepository,
            InspectionRepository inspectionRepository,
            InstrumentRepository instrumentRepository,
            TestRecordRepository testRecordRepository,

            EccentricityRecordRepository eccentricityRecordRepository,
            EccentricityRecordService eccentricityRecordService,

            RepeatabilityRecordRepository repeatabilityRecordRepository,
            RepeatabilityRecordService repeatabilityRecordService,

            EnvironmentRecordRepository environmentRecordRepository,
            QrCodeService qrCodeService) {

        this.certificateRepository =
                certificateRepository;

        this.inspectionRepository =
                inspectionRepository;

        this.instrumentRepository =
                instrumentRepository;

        this.testRecordRepository =
                testRecordRepository;

        this.eccentricityRecordRepository =
                eccentricityRecordRepository;

        this.eccentricityRecordService =
                eccentricityRecordService;

        this.repeatabilityRecordRepository =
                repeatabilityRecordRepository;

        this.repeatabilityRecordService =
                repeatabilityRecordService;

        this.environmentRecordRepository =
                environmentRecordRepository;

        this.qrCodeService =
                qrCodeService;
    }

    public byte[] generatePdf(Long certificateId) {

        Certificate certificate =
                certificateRepository.findById(certificateId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Certificate not found"));

        Inspection inspection =
                inspectionRepository.findById(
                                certificate.getInspectionId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Inspection not found"));

        Instrument instrument =
                instrumentRepository.findById(
                                inspection.getInstrumentId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Instrument not found"));

        List<TestRecord> allTestRecords =
                testRecordRepository.findByInspectionId(
                        inspection.getId());

        List<TestRecord> weighingRecords =
                allTestRecords.stream()
                        .filter(record ->
                                "WEIGHING_PERFORMANCE"
                                        .equalsIgnoreCase(
                                                safe(record.getTestType())))
                        .sorted(
                                Comparator.comparing(
                                        TestRecord::getId,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder())
                                )
                        )
                        .collect(Collectors.toList());

        List<EccentricityRecord> eccentricityRecords =
                eccentricityRecordRepository.findByInspectionId(
                        inspection.getId());

        List<RepeatabilityRecord> repeatabilityRecords =
                repeatabilityRecordRepository.findByInspectionId(
                        inspection.getId());

        List<EnvironmentRecord> environmentRecords =
                environmentRecordRepository.findByInspectionId(
                        inspection.getId());

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(
                            PageSize.A4,
                            40,
                            40,
                            40,
                            40
                    );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            // -------------------------------------------------
            // FONTS
            // -------------------------------------------------

            Font titleFont =
                    new Font(
                            Font.HELVETICA,
                            24,
                            Font.BOLD
                    );

            Font subtitleFont =
                    new Font(
                            Font.HELVETICA,
                            14,
                            Font.BOLD
                    );

            Font headingFont =
                    new Font(
                            Font.HELVETICA,
                            13,
                            Font.BOLD
                    );

            Font subHeadingFont =
                    new Font(
                            Font.HELVETICA,
                            11,
                            Font.BOLD
                    );

            Font normalFont =
                    new Font(
                            Font.HELVETICA,
                            10,
                            Font.NORMAL
                    );

            Font smallFont =
                    new Font(
                            Font.HELVETICA,
                            8,
                            Font.NORMAL
                    );

            Font tableHeaderFont =
                    new Font(
                            Font.HELVETICA,
                            9,
                            Font.BOLD
                    );

            // -------------------------------------------------
            // TITLE
            // -------------------------------------------------

            Paragraph title =
                    new Paragraph(
                            "SMARTMETRIX",
                            titleFont
                    );

            title.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(title);

            Paragraph subtitle =
                    new Paragraph(
                            "NAWI Inspection Certificate",
                            subtitleFont
                    );

            subtitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(subtitle);

            Paragraph prototype =
                    new Paragraph(
                            "OIML R-76-oriented Research Prototype",
                            smallFont
                    );

            prototype.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(prototype);

            document.add(
                    new Paragraph(" ")
            );

            // -------------------------------------------------
            // CERTIFICATE INFORMATION
            // -------------------------------------------------

            addHeading(
                    document,
                    "Certificate Information",
                    headingFont
            );

            document.add(
                    new Paragraph(
                            "Certificate Number: "
                                    + certificate.getCertificateNumber(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Inspection ID: "
                                    + inspection.getId(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Certificate Status: "
                                    + certificate.getStatus(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            // -------------------------------------------------
            // INSTRUMENT DETAILS
            // -------------------------------------------------

            addHeading(
                    document,
                    "Instrument Details",
                    headingFont
            );

            PdfPTable instrumentTable =
                    createTwoColumnTable();

            addRow(
                    instrumentTable,
                    "Instrument ID",
                    String.valueOf(
                            instrument.getId()),
                    normalFont
            );

            addRow(
                    instrumentTable,
                    "Manufacturer",
                    instrument.getManufacturer(),
                    normalFont
            );

            addRow(
                    instrumentTable,
                    "Model",
                    instrument.getModel(),
                    normalFont
            );

            addRow(
                    instrumentTable,
                    "Serial Number",
                    instrument.getSerialNumber(),
                    normalFont
            );

            addRow(
                    instrumentTable,
                    "Instrument Class",
                    instrument.getInstrumentClass(),
                    normalFont
            );

            addRow(
                    instrumentTable,
                    "Capacity",
                    String.valueOf(
                            instrument.getCapacity()),
                    normalFont
            );

            addRow(
                    instrumentTable,
                    "Scale Interval (e)",
                    String.valueOf(
                            instrument.getScaleInterval()),
                    normalFont
            );

            document.add(instrumentTable);

            document.add(
                    new Paragraph(" ")
            );

            // -------------------------------------------------
            // INSPECTION RESULT
            // -------------------------------------------------

            addHeading(
                    document,
                    "Inspection Result",
                    headingFont
            );

            PdfPTable resultTable =
                    createTwoColumnTable();

            addRow(
                    resultTable,
                    "Overall Result",
                    inspection.getOverallResult(),
                    normalFont
            );

            addRow(
                    resultTable,
                    "Inspection Status",
                    inspection.getStatus(),
                    normalFont
            );

            addRow(
                    resultTable,
                    "Inspector ID",
                    String.valueOf(
                            inspection.getInspectorId()),
                    normalFont
            );

            document.add(resultTable);

            document.add(
                    new Paragraph(" ")
            );

            // -------------------------------------------------
            // WEIGHING PERFORMANCE
            // -------------------------------------------------

            if (!weighingRecords.isEmpty()) {

                addHeading(
                        document,
                        "Applicable Test Details - Weighing Performance",
                        headingFont
                );

                TestRecord finalRecord =
                        weighingRecords.get(
                                weighingRecords.size() - 1
                        );

                // -------------------------------------------------
                // DYNAMIC WEIGHING HEADING
                // -------------------------------------------------

                String weighingHeading =
                        "Weighing Performance Result";

                String testStage =
                        safe(finalRecord.getTestStage());

                if ("FINAL".equalsIgnoreCase(testStage)) {

                    weighingHeading =
                            "Final Weighing Result";

                } else if ("INITIAL".equalsIgnoreCase(testStage)) {

                    weighingHeading =
                            "Initial Weighing Result";
                }

                Paragraph finalHeading =
                        new Paragraph(
                                weighingHeading,
                                subHeadingFont
                        );

                finalHeading.setSpacingBefore(5);
                finalHeading.setSpacingAfter(5);

                document.add(finalHeading);

                PdfPTable finalTable =
                        new PdfPTable(2);

                finalTable.setWidthPercentage(100);

                finalTable.setWidths(
                        new float[]{35, 65}
                );

                addRow(
                        finalTable,
                        "Test Record ID",
                        String.valueOf(
                                finalRecord.getId()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Test Type",
                        safe(finalRecord.getTestType()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Reference Weight",
                        format(
                                finalRecord.getReferenceWeight()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Observed Weight",
                        format(
                                finalRecord.getObservedWeight()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Error",
                        format(
                                finalRecord.getError()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "MPE",
                        formatMpe(
                                finalRecord.getMpe()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Result",
                        safe(finalRecord.getResult()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Stage",
                        testStage,
                        normalFont
                );

                // -------------------------------------------------
                // WEIGHING PERFORMANCE TIMESTAMP
                // -------------------------------------------------

                addRow(
                        finalTable,
                        "Timestamp",
                        formatTimestamp(
                                finalRecord.getCreatedAt()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Temperature (°C)",
                        format(
                                finalRecord.getTemperature()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Humidity (%)",
                        format(
                                finalRecord.getHumidity()),
                        normalFont
                );

                addRow(
                        finalTable,
                        "Vibration",
                        format(
                                finalRecord.getVibration()),
                        normalFont
                );

                document.add(finalTable);

                document.add(
                        new Paragraph(" ")
                );

                // -------------------------------------------------
                // HISTORICAL ATTEMPTS
                // -------------------------------------------------

                if (weighingRecords.size() > 1) {

                    Paragraph historicalHeading =
                            new Paragraph(
                                    "Historical Attempts",
                                    subHeadingFont
                            );

                    historicalHeading.setSpacingBefore(5);
                    historicalHeading.setSpacingAfter(5);

                    document.add(
                            historicalHeading
                    );

                    PdfPTable historicalTable =
                            new PdfPTable(7);

                    historicalTable.setWidthPercentage(100);

                    addHeader(
                            historicalTable,
                            "Record ID",
                            tableHeaderFont
                    );

                    addHeader(
                            historicalTable,
                            "Reference",
                            tableHeaderFont
                    );

                    addHeader(
                            historicalTable,
                            "Observed",
                            tableHeaderFont
                    );

                    addHeader(
                            historicalTable,
                            "Error",
                            tableHeaderFont
                    );

                    addHeader(
                            historicalTable,
                            "Result",
                            tableHeaderFont
                    );

                    addHeader(
                            historicalTable,
                            "Stage",
                            tableHeaderFont
                    );

                    addHeader(
                            historicalTable,
                            "Timestamp",
                            tableHeaderFont
                    );

                    for (
                            int i = 0;
                            i < weighingRecords.size() - 1;
                            i++
                    ) {

                        TestRecord record =
                                weighingRecords.get(i);

                        addCell(
                                historicalTable,
                                String.valueOf(
                                        record.getId()),
                                smallFont
                        );

                        addCell(
                                historicalTable,
                                format(
                                        record.getReferenceWeight()),
                                smallFont
                        );

                        addCell(
                                historicalTable,
                                format(
                                        record.getObservedWeight()),
                                smallFont
                        );

                        addCell(
                                historicalTable,
                                format(
                                        record.getError()),
                                smallFont
                        );

                        addCell(
                                historicalTable,
                                safe(record.getResult()),
                                smallFont
                        );

                        addCell(
                                historicalTable,
                                safe(record.getTestStage()),
                                smallFont
                        );

                        addCell(
                                historicalTable,
                                formatTimestamp(
                                        record.getCreatedAt()),
                                smallFont
                        );
                    }

                    document.add(
                            historicalTable
                    );

                    document.add(
                            new Paragraph(" ")
                    );
                }

                // -------------------------------------------------
                // TEST ENVIRONMENT SUMMARY
                // -------------------------------------------------

                PdfPTable environmentFromTestTable =
                        createTwoColumnTable();

                addRow(
                        environmentFromTestTable,
                        "Final Test Humidity",
                        format(
                                finalRecord.getHumidity()),
                        normalFont
                );

                addRow(
                        environmentFromTestTable,
                        "Final Test Vibration",
                        format(
                                finalRecord.getVibration()),
                        normalFont
                );

                document.add(
                        environmentFromTestTable
                );

                document.add(
                        new Paragraph(" ")
                );
            }

            // -------------------------------------------------
            // ECCENTRICITY
            // -------------------------------------------------

            if (!eccentricityRecords.isEmpty()) {

                addHeading(
                        document,
                        "Applicable Test Details - Eccentricity",
                        headingFont
                );

                /*
                 * The existing EccentricityRecordService
                 * contains the actual PASS/FAIL rule
                 * and MPE calculation.
                 */
                EccentricitySummaryResponse
                        eccentricitySummary =
                        eccentricityRecordService.getSummary(
                                inspection.getId()
                        );

                PdfPTable table =
                        new PdfPTable(6);

                table.setWidthPercentage(100);

                addHeader(
                        table,
                        "Position",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Reference",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Observed",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Difference",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Result",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Timestamp",
                        tableHeaderFont
                );

                for (
                        EccentricityPositionResult position :
                        eccentricitySummary.getPositions()
                ) {

                    addCell(
                            table,
                            safe(position.getPosition()),
                            smallFont
                    );

                    addCell(
                            table,
                            format(
                                    position.getReferenceWeight()),
                            smallFont
                    );

                    addCell(
                            table,
                            format(
                                    position.getObservedWeight()),
                            smallFont
                    );

                    addCell(
                            table,
                            format(
                                    position.getError()),
                            smallFont
                    );

                    addCell(
                            table,
                            safe(position.getResult()),
                            smallFont
                    );

                    /*
                     * Find the actual EccentricityRecord
                     * for this position and use its createdAt.
                     */
                    String eccentricityTimestamp =
                            eccentricityRecords.stream()
                                    .filter(record ->
                                            safe(record.getPosition())
                                                    .equalsIgnoreCase(
                                                            safe(position.getPosition())))
                                    .map(record ->
                                            formatTimestamp(
                                                    record.getCreatedAt()))
                                    .findFirst()
                                    .orElse("-");

                    addCell(
                            table,
                            eccentricityTimestamp,
                            smallFont
                    );
                }

                document.add(table);

                document.add(
                        new Paragraph(" ")
                );

                PdfPTable summary =
                        createTwoColumnTable();

                addRow(
                        summary,
                        "Reference Weight",
                        format(
                                eccentricitySummary
                                        .getReferenceWeight()),
                        normalFont
                );

                addRow(
                        summary,
                        "MPE",
                        formatMpe(
                                eccentricitySummary.getMpe()),
                        normalFont
                );

                addRow(
                        summary,
                        "Maximum Difference",
                        format(
                                eccentricitySummary
                                        .getMaximumDifference()),
                        normalFont
                );

                addRow(
                        summary,
                        "Eccentricity Result",
                        safe(
                                eccentricitySummary.getResult()),
                        normalFont
                );

                document.add(summary);

                document.add(
                        new Paragraph(" ")
                );
            }

            // -------------------------------------------------
            // REPEATABILITY
            // -------------------------------------------------

            if (!repeatabilityRecords.isEmpty()) {

                addHeading(
                        document,
                        "Applicable Test Details - Repeatability",
                        headingFont
                );

                /*
                 * -------------------------------------------------
                 * FIND LATEST REPEATABILITY TEST RUN
                 * -------------------------------------------------
                 */

                Long latestTestRunId =
                        repeatabilityRecords.stream()
                                .filter(record ->
                                        record.getTestRunId() != null)
                                .map(
                                        RepeatabilityRecord::getTestRunId
                                )
                                .max(Long::compareTo)
                                .orElse(null);

                /*
                 * -------------------------------------------------
                 * REPEATABILITY READINGS TABLE
                 * -------------------------------------------------
                 */

                PdfPTable table =
                        new PdfPTable(4);

                table.setWidthPercentage(100);

                addHeader(
                        table,
                        "Reading Number",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Reference",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Observed",
                        tableHeaderFont
                );

                addHeader(
                        table,
                        "Timestamp",
                        tableHeaderFont
                );

                for (
                        RepeatabilityRecord record :
                        repeatabilityRecords
                ) {

                    /*
                     * If testRunId is available,
                     * show only the latest test run.
                     */
                    if (
                            latestTestRunId != null &&
                                    !latestTestRunId.equals(
                                            record.getTestRunId())
                    ) {

                        continue;
                    }

                    addCell(
                            table,
                            String.valueOf(
                                    record.getReadingNumber()),
                            smallFont
                    );

                    addCell(
                            table,
                            format(
                                    record.getReferenceWeight()),
                            smallFont
                    );

                    addCell(
                            table,
                            format(
                                    record.getObservedWeight()),
                            smallFont
                    );

                    addCell(
                            table,
                            formatTimestamp(
                                    record.getCreatedAt()),
                            smallFont
                    );
                }

                document.add(table);

                document.add(
                        new Paragraph(" ")
                );

                /*
                 * -------------------------------------------------
                 * REPEATABILITY SUMMARY
                 * -------------------------------------------------
                 */

                if (latestTestRunId != null) {

                    try {

                        RepeatabilitySummaryResponse
                                repeatabilitySummary =
                                repeatabilityRecordService
                                        .getSummary(
                                                latestTestRunId
                                        );

                        PdfPTable summary =
                                createTwoColumnTable();

                        addRow(
                                summary,
                                "Test Run ID",
                                String.valueOf(
                                        repeatabilitySummary
                                                .getTestRunId()),
                                normalFont
                        );

                        addRow(
                                summary,
                                "Reference Weight",
                                format(
                                        repeatabilitySummary
                                                .getReferenceWeight()),
                                normalFont
                        );

                        addRow(
                                summary,
                                "Average Observed Weight",
                                format(
                                        repeatabilitySummary
                                                .getAverage()),
                                normalFont
                        );

                        addRow(
                                summary,
                                "Average Error",
                                format(
                                        repeatabilitySummary
                                                .getAverageError()),
                                normalFont
                        );

                        addRow(
                                summary,
                                "Repeatability Range",
                                format(
                                        repeatabilitySummary
                                                .getRange()),
                                normalFont
                        );

                        addRow(
                                summary,
                                "Repeatability Result",
                                safe(
                                        repeatabilitySummary
                                                .getResult()),
                                normalFont
                        );

                        /*
                         * Add the actual timestamp of the
                         * latest repeatability test run.
                         */
                        String repeatabilityTimestamp =
                                repeatabilityRecords.stream()
                                        .filter(record ->
                                                latestTestRunId.equals(
                                                        record.getTestRunId()))
                                        .map(record ->
                                                record.getCreatedAt())
                                        .filter(timestamp ->
                                                timestamp != null)
                                        .min(Comparator.naturalOrder())
                                        .map(this::formatTimestamp)
                                        .orElse("-");

                        addRow(
                                summary,
                                "Test Timestamp",
                                repeatabilityTimestamp,
                                normalFont
                        );

                        document.add(summary);

                    } catch (IllegalArgumentException ex) {

                        /*
                         * Less than 5 readings or incomplete
                         * repeatability test.
                         *
                         * Do not invent PASS/FAIL.
                         */
                        PdfPTable summary =
                                createTwoColumnTable();

                        addRow(
                                summary,
                                "Repeatability Result",
                                "PENDING",
                                normalFont
                        );

                        document.add(summary);
                    }
                }

                document.add(
                        new Paragraph(" ")
                );
            }

            // -------------------------------------------------
            // ENVIRONMENT
            // -------------------------------------------------

            if (!environmentRecords.isEmpty()) {

                addHeading(
                        document,
                        "Environmental Conditions",
                        headingFont
                );

                EnvironmentRecord environment =
                        environmentRecords.get(
                                environmentRecords.size() - 1
                        );

                PdfPTable table =
                        createTwoColumnTable();

                addRow(
                        table,
                        "Temperature (°C)",
                        format(
                                environment.getTemperature()),
                        normalFont
                );

                addRow(
                        table,
                        "Humidity (%)",
                        format(
                                environment.getHumidity()),
                        normalFont
                );

                addRow(
                        table,
                        "Vibration",
                        format(
                                environment.getVibration()),
                        normalFont
                );

                addRow(
                        table,
                        "Source",
                        safe(environment.getSource()),
                        normalFont
                );

                addRow(
                        table,
                        "Environmental Status",
                        safe(environment.getStatus()),
                        normalFont
                );

                document.add(table);

                document.add(
                        new Paragraph(" ")
                );
            }

            // -------------------------------------------------
            // SHA-256
            // -------------------------------------------------

            addHeading(
                    document,
                    "Certificate Integrity",
                    headingFont
            );

            document.add(
                    new Paragraph(
                            "SHA-256 Hash:",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            safe(certificate.getHash()),
                            smallFont
                    )
            );

            if (
                    certificate.getSignature() != null &&
                            !certificate.getSignature().isBlank()
            ) {

                document.add(
                        new Paragraph(" ")
                );

                document.add(
                        new Paragraph(
                                "Digital Signature:",
                                normalFont
                        )
                );

                document.add(
                        new Paragraph(
                                certificate.getSignature(),
                                smallFont
                        )
                );
            }

            document.add(
                    new Paragraph(" ")
            );

            // -------------------------------------------------
            // QR CODE
            // -------------------------------------------------

            addHeading(
                    document,
                    "Certificate Verification QR",
                    headingFont
            );

            byte[] qrBytes =
                    qrCodeService.generateQrCode(
                            certificate.getCertificateNumber()
                    );

            Image qrImage =
                    Image.getInstance(qrBytes);

            qrImage.scaleToFit(
                    130,
                    130
            );

            qrImage.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(qrImage);

            Paragraph qrText =
                    new Paragraph(
                            "Scan to identify the certificate number",
                            smallFont
                    );

            qrText.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(qrText);

            document.add(
                    new Paragraph(" ")
            );

            // -------------------------------------------------
            // DISCLAIMER
            // -------------------------------------------------

            Paragraph disclaimer =
                    new Paragraph(
                            "SmartMetrix is a research prototype for "
                                    + "OIML R-76-oriented inspection, "
                                    + "analysis and digital certificate "
                                    + "workflow. This document does not "
                                    + "constitute an official legal "
                                    + "metrological certificate.",
                            smallFont
                    );

            disclaimer.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(disclaimer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to generate certificate PDF",
                    e
            );
        }
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private void addHeading(
            Document document,
            String text,
            Font font)
            throws DocumentException {

        Paragraph heading =
                new Paragraph(
                        text,
                        font
                );

        heading.setSpacingBefore(5);
        heading.setSpacingAfter(5);

        document.add(heading);
    }

    private PdfPTable createTwoColumnTable() {

        PdfPTable table =
                new PdfPTable(2);

        table.setWidthPercentage(100);

        table.setWidths(
                new float[]{35, 65}
        );

        return table;
    }

    private void addRow(
            PdfPTable table,
            String key,
            String value,
            Font font) {

        addCell(
                table,
                key,
                font
        );

        addCell(
                table,
                value,
                font
        );
    }

    private void addHeader(
            PdfPTable table,
            String text,
            Font font) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text,
                                font
                        )
                );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setPadding(5);

        table.addCell(cell);
    }

    private void addCell(
            PdfPTable table,
            String text,
            Font font) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text == null
                                        ? ""
                                        : text,
                                font
                        )
                );

        cell.setPadding(4);

        table.addCell(cell);
    }

    private String format(Double value) {

        if (value == null) {
            return "-";
        }

        return String.format(
                "%.5f",
                value
        );
    }

    private String formatMpe(Double value) {

        if (value == null) {
            return "-";
        }

        return "±" + String.format(
                "%.5f",
                Math.abs(value)
        );
    }

    private String formatTimestamp(
            java.time.LocalDateTime timestamp) {

        if (timestamp == null) {
            return "-";
        }

        return timestamp.toString()
                .replace("T", " ");
    }

    private String safe(String value) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return "-";
        }

        return value;
    }
}

