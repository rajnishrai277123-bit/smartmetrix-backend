package com.smartmetrix.backend.test.exception;


public class TestRecordNotFoundException extends RuntimeException {

    public TestRecordNotFoundException(String message) {
        super(message);
    }
}