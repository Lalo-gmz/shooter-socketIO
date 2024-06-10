package com.mygdx.game.exceptions;

public class IdNotFoundException extends Exception{

    public IdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdNotFoundException(String message) {
        super(message);
    }
}
