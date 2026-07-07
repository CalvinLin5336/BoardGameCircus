package com.turing.exception;

/**
 * Custom runtime exception to handle Turing Machine game application issues.
 */
public class GameException extends RuntimeException {
    
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
