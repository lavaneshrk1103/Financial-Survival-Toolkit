package com.financialtoolkit.common;

public abstract class ToolkitException extends RuntimeException {
    protected ToolkitException(String message) {
        super(message);
    }
}
