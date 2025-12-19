package com.example.backend.dto;

public class AuthErrorResponse {
    private String errorType;
    private String field;
    private String message;
    
    // Constructors
    public AuthErrorResponse() {}
    
    public AuthErrorResponse(String errorType, String message) {
        this.errorType = errorType;
        this.message = message;
    }
    
    public AuthErrorResponse(String errorType, String field, String message) {
        this.errorType = errorType;
        this.field = field;
        this.message = message;
    }
    
    // Getters and Setters
    public String getErrorType() {
        return errorType;
    }
    
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}





