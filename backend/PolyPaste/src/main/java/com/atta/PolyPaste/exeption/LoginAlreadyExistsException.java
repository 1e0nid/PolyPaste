package com.atta.PolyPaste.exeption;

public class LoginAlreadyExistsException extends RuntimeException {
    public LoginAlreadyExistsException(String message) {
        super(message);
    }
}