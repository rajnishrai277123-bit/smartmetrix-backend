package com.smartmetrix.backend.certificate;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.smartmetrix.backend.analytics.AdvancedAnalyticsResponse;
import com.smartmetrix.backend.analytics.AdvancedAnalyticsService;
import com.smartmetrix.backend.instrument.Instrument;
import com.smartmetrix.backend.instrument.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ResearchReportPdfService {

    private final AdvancedAnalyticsService analyticsService;
    private final InstrumentRepository instrumentRepository;

    public ResearchReportPdfService(
            AdvancedAnalyticsService analyticsService,
            InstrumentRepository instrumentRepository) {

        this.analyticsService = analyticsService;
        this.instrumentRepository = instrumentRepository;
    }

    public byte[] generateResearchReport(
            Long instrumentId) {

        Instrument instrument =
                instrumentRepository
                        .findById(instrumentId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Instrument not found"));

        AdvancedAnalyticsResponse data =
                analyticsService
                        .analyzeInstrument(instrumentId);

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4);

            PdfWriter.getInstance(
                    document,
                    output
            );

            document.open();

            Font titleFont =
                    new Font(
                            Font.HELVETICA,
                            22,
                            Font.BOLD
                    );

            Font headingFont =
                    new Font(
                            Font.HELVETICA,
                            14,
                            Font.BOLD
                    );

            Font normalFont =
                    new Font(
                            Font.HELVETICA,
                            10,
                            Font.NORMAL
                    );

            // --------------------------------
            // Title
            // --------------------------------

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
                            "Research Analytics Report",
                            headingFont
                    );

            subtitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(subtitle);

            document.add(
                    new Paragraph(" ")
            );

            // --------------------------------
            // Instrument
            // --------------------------------

            document.add(
                    new Paragraph(
                            "Instrument Information",
                            headingFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Instrument ID: "
                                    + instrument.getId(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Manufacturer: "
                                    + instrument.getManufacturer(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Model: "
                                    + instrument.getModel(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Serial Number: "
                                    + instrument.getSerialNumber(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Instrument Class: "
                                    + instrument.getInstrumentClass(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Capacity: "
                                    + instrument.getCapacity(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Scale Interval (e): "
                                    + instrument.getScaleInterval(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            // --------------------------------
            // Health
            // --------------------------------

            document.add(
                    new Paragraph(
                            "1. Instrument Health Assessment",
                            headingFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Health Score: "
                                    + data.getHealthScore()
                                    + " / 100",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Health Status: "
                                    + data.getHealthStatus(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Total Tests: "
                                    + data.getTotalTests(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Failed Tests: "
                                    + data.getFailedTests(),
                            normalFont
                    )
            );

            // --------------------------------
            // Environment
            // --------------------------------

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "2. Environmental Impact Assessment",
                            headingFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Environment Impact Score: "
                                    + data.getEnvironmentImpactScore()
                                    + " / 100",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Impact Level: "
                                    + data.getEnvironmentImpactLevel(),
                            normalFont
                    )
            );

            // --------------------------------
            // Trend
            // --------------------------------

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "3. Measurement Trend Analysis",
                            headingFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Average Absolute Error: "
                                    + data.getAverageError(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Maximum Absolute Error: "
                                    + data.getMaximumAbsoluteError(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Dated measurements available: "
                                    + data.getTrend().size(),
                            normalFont
                    )
            );

            for (
                    AdvancedAnalyticsResponse.TrendPoint point
                    : data.getTrend()) {

                document.add(
                        new Paragraph(
                                point.getDate()
                                        + " | Error: "
                                        + point.getError()
                                        + " | Result: "
                                        + point.getResult(),
                                normalFont
                        )
                );
            }

            // --------------------------------
            // Recalibration
            // --------------------------------

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "4. Recalibration Prediction",
                            headingFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Prediction: "
                                    + data.getRecalibrationPrediction(),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "Recommendation: "
                                    + data.getRecalibrationRecommendation(),
                            normalFont
                    )
            );

            // --------------------------------
            // Research Method
            // --------------------------------

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "5. Research Methodology",
                            headingFont
                    )
            );

            document.add(
                    new Paragraph(
                            "SmartMetrix combines historical weighing "
                                    + "measurements, measurement error analysis, "
                                    + "environmental observations and rule-based "
                                    + "risk indicators to generate an explainable "
                                    + "research-oriented instrument assessment.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(
                            "The analytical scores are experimental research "
                                    + "metrics and must not be interpreted as "
                                    + "official OIML certification limits.",
                            normalFont
                    )
            );

            // --------------------------------
            // Conclusion
            // --------------------------------

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "6. Conclusion",
                            headingFont
                    )
            );

            document.add(
                    new Paragraph(
                            "The SmartMetrix prototype demonstrates how "
                                    + "digital inspection records can be combined "
                                    + "with explainable analytics to support "
                                    + "maintenance planning, historical trend "
                                    + "analysis and early identification of "
                                    + "potential measurement drift.",
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );

            Paragraph footer =
                    new Paragraph(
                            "SmartMetrix Research Prototype — "
                                    + "OIML R-76-oriented workflow",
                            normalFont
                    );

            footer.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(footer);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to generate research report",
                    e
            );
        }
    }
}