package com.jydoc.deliverable3;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.hibernate.validator.internal.engine.path.PathImpl;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = getCustomErrorMessage(error);
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
            String errorMessage = getCustomConstraintMessage(violation);
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    private String getCustomErrorMessage(org.springframework.validation.ObjectError error) {
        String errorCode = error.getCode();
        String defaultMessage = error.getDefaultMessage();
        String fieldName = error.getObjectName();

        if (error instanceof FieldError) {
            fieldName = ((FieldError) error).getField();
        }

        return switch (errorCode) {
            case "NotBlank", "NotEmpty" -> fieldName + " is required";
            case "Pattern" -> {
                if (fieldName.equals("password")) {
                    yield "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character";
                }
                yield fieldName + " has invalid format";
            }
            case "Size" -> {
                Object[] args = error.getArguments();
                if (args != null && args.length >= 3) {
                    yield fieldName + " length must be between " + args[2] + " and " + args[1];
                }
                yield defaultMessage;
            }
            default -> defaultMessage;
        };
    }

    private String getCustomConstraintMessage(jakarta.validation.ConstraintViolation<?> violation) {
        String fieldName = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String message = violation.getMessage();

        if (message.contains("jakarta.validation.constraints.NotBlank") ||
                message.contains("jakarta.validation.constraints.NotEmpty")) {
            return fieldName + " is required";
        } else if (message.contains("jakarta.validation.constraints.Pattern")) {
            if (fieldName.equals("password")) {
                return "Password must meet complexity requirements";
            }
            return fieldName + " has invalid format";
        } else if (message.contains("jakarta.validation.constraints.Size")) {
            return fieldName + " length is invalid";
        }

        return message;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        return new ResponseEntity<>("An error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}