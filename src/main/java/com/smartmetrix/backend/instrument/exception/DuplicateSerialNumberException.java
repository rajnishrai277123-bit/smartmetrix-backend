package com.smartmetrix.backend.instrument.exception;

public class DuplicateSerialNumberException extends RuntimeException {

    public DuplicateSerialNumberException(String message) {
        super(message);
    }
}