package com.smartmetrix.backend.inspection.exception;

public class InspectionNotFoundException extends RuntimeException {

    public InspectionNotFoundException(String message) {
        super(message);
    }
}