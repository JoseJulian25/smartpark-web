package com.parking.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 401 - Credenciales incorrectas (username o password inválidos)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", "Credenciales incorrectas", request);
    }

    // 403 - Token válido pero sin permisos suficientes (solo si llega al MVC layer)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Prohibido", "No tienes permisos para acceder a este recurso", request);
    }

    // 400 - Campos inválidos en el body (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", mensaje, request);
    }

    // 500 - Cualquier excepción no controlada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message,
                                                  HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                error,
                message,
                LocalDateTime.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
