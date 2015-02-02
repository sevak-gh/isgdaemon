package com.infotech.isg.service;

/**
 * represents custom exception.
 *
 * @author Sevak Gharibian
 */
public class ISGException extends RuntimeException {

    public ISGException() {
    }

    public ISGException(String message) {
        super(message);
    }

    public ISGException(Throwable cause) {
        super(cause);
    }

    public ISGException(String message, Throwable cause) {
        super(message, cause);
    }
}
