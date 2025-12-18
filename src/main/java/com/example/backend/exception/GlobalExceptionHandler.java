package com.example.backend.exception;

import com.example.backend.dto.AuthErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValid(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Doğrulama hatası");
        body.put("status", 400);
        body.put("details", ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(HashMap::new, (m, fe) -> m.put(fe.getField(), fe.getDefaultMessage()), HashMap::putAll));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraint(ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Geçersiz istek");
        body.put("status", 400);
        body.put("details", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage(), "status", 400));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage(), "status", 404));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage(), "status", 409));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<AuthErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        AuthErrorResponse error = new AuthErrorResponse(
            "INVALID_EMAIL",
            "email",
            "Bu e-posta adresi bulunamadı."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        // BadCredentialsException genellikle şifre yanlış olduğunda fırlatılır
        // Frontend'in beklediği formata göre şifre alanında hata gösterilir
        AuthErrorResponse error = new AuthErrorResponse(
            "INVALID_PASSWORD",
            "password",
            "Şifre yanlış."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<AuthErrorResponse> handleDisabled(DisabledException ex) {
        AuthErrorResponse error = new AuthErrorResponse(
            "ACCOUNT_DISABLED",
            "email",
            "Hesabınız devre dışı bırakılmış."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthentication(AuthenticationException ex) {
        // Genel authentication exception'ları için
        // Spring Boot'un default exception formatını da desteklemek için
        // Hem AuthErrorResponse hem de Map formatını destekliyoruz
        // Frontend her iki formatı da handle edebilir
        
        // Önce AuthErrorResponse formatında döndürüyoruz (önerilen format)
        AuthErrorResponse error = new AuthErrorResponse(
            "INVALID_CREDENTIALS",
            "E-posta veya şifre hatalı."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception ex) {
        System.err.println("❌ Global exception handler: " + ex.getMessage());
        ex.printStackTrace();
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage() != null ? ex.getMessage() : "Bilinmeyen hata");
        body.put("status", 500);
        body.put("type", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
