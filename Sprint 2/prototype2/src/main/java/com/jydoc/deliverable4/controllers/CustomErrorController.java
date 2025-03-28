package com.jydoc.deliverable4.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Custom error controller to handle application errors and display user-friendly error pages.
 * Implements Spring Boot's {@link ErrorController} interface to override default error handling.
 */
@Controller
public class CustomErrorController implements ErrorController {

    private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred";
    private static final String DB_VALIDATION_ERROR = "Database error: Required role configuration is missing. Please contact support.";

    /**
     * Handles all error requests and prepares error information for display.
     *
     * @param request The HTTP request containing error attributes
     * @param model The model to populate with error details
     * @return The error view template name
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        HttpStatus httpStatus = resolveHttpStatus(request);
        Throwable exception = resolveException(request);

        model.addAttribute("status", httpStatus.value());
        model.addAttribute("error", httpStatus.getReasonPhrase());
        model.addAttribute("message", getSafeErrorMessage(exception));
        model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        model.addAttribute("timestamp", Instant.now());

        return "error";
    }

    /**
     * Resolves the HTTP status from the request attributes.
     */
    private HttpStatus resolveHttpStatus(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                .map(status -> HttpStatus.valueOf(Integer.parseInt(status.toString())))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Resolves the exception from the request attributes.
     */
    private Throwable resolveException(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION))
                .map(Throwable.class::cast)
                .orElse(null);
    }

    /**
     * Provides a safe error message for display, handling nulls and sensitive information.
     */
    private String getSafeErrorMessage(Throwable exception) {
        if (exception == null) {
            return DEFAULT_ERROR_MESSAGE;
        }

        String message = exception.getMessage();
        if (message == null) {
            return DEFAULT_ERROR_MESSAGE;
        }

        return message.contains("Field 'authority' doesn't have a default value")
                ? DB_VALIDATION_ERROR
                : sanitizeMessage(message);
    }

    /**
     * Sanitizes error messages to prevent exposing sensitive information.
     */
    private String sanitizeMessage(String message) {
        // Basic sanitization - extend this for your specific security requirements
        return message.replaceAll("(?i)password|secret|key|token", "[REDACTED]");
    }
}