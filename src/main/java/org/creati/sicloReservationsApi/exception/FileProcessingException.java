package org.creati.sicloReservationsApi.exception;

public class FileProcessingException extends RuntimeException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
