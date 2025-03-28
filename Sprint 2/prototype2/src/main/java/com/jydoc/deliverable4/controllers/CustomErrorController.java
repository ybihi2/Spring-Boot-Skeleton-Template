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
 * <p>
 * This controller implements Spring Boot's {@link ErrorController} interface to override
 * the default white-label error page behavior. It provides:
 * <ul>
 *   <li>Custom error page rendering with user-friendly messages</li>
 *   <li>HTTP status code resolution from request attributes</li>
 *   <li>Exception handling and message sanitization</li>
 *   <li>Security-conscious error information disclosure</li>
 * </ul>
 * </p>
 *
 * <p><strong>Security Note:</strong> All error messages are sanitized to prevent
 * exposure of sensitive information before being displayed to users.</p>
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Default error message displayed when no specific message is available.
     */
    private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred";

    /**
     * Specific error message for database validation failures.
     */
    private static final String DB_VALIDATION_ERROR =
            "Database error: Required role configuration is missing. Please contact support.";

    /**
     * Handles all error requests and prepares error information for display.
     * <p>
     * This method:
     * <ul>
     *   <li>Resolves the HTTP status code from the request</li>
     *   <li>Extracts any associated exception</li>
     *   <li>Sanitizes error messages for security</li>
     *   <li>Populates the model with error details for the view</li>
     *   <li>Returns the error view template</li>
     * </ul>
     * </p>
     *
     * @param request The HTTP request containing error attributes. Must not be null.
     * @param model The Spring MVC model to populate with error details. Automatically
     *              provided by Spring MVC.
     * @return The logical view name "error" which resolves to the error template
     *
     * @see jakarta.servlet.RequestDispatcher#ERROR_STATUS_CODE
     * @see jakarta.servlet.RequestDispatcher#ERROR_EXCEPTION
     * @see jakarta.servlet.RequestDispatcher#ERROR_REQUEST_URI
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
     * Resolves the HTTP status code from the request attributes.
     * <p>
     * Attempts to extract the status code from request attributes, falling back to
     * HTTP 500 (Internal Server Error) if not available.
     * </p>
     *
     * @param request The HTTP request containing error attributes
     * @return The resolved HttpStatus, never null
     * @throws NumberFormatException if the status code attribute contains
     *         a non-numeric value (should not occur in normal operation)
     */
    private HttpStatus resolveHttpStatus(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                .map(status -> HttpStatus.valueOf(Integer.parseInt(status.toString())))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Resolves the exception from the request attributes.
     * <p>
     * Extracts any exception associated with the error request, if available.
     * </p>
     *
     * @param request The HTTP request containing error attributes
     * @return The associated Throwable, or null if no exception is present
     */
    private Throwable resolveException(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION))
                .map(Throwable.class::cast)
                .orElse(null);
    }

    /**
     * Provides a safe error message for display.
     * <p>
     * This method ensures:
     * <ul>
     *   <li>Null exceptions return a default message</li>
     *   <li>Specific database errors return a standardized message</li>
     *   <li>All other messages are sanitized before display</li>
     * </ul>
     * </p>
     *
     * @param exception The exception to derive the message from, may be null
     * @return A safe, user-appropriate error message, never null
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
     * <p>
     * Replaces sensitive patterns (like passwords, tokens, etc.) with [REDACTED].
     * Extend this method to include additional sensitive patterns as needed.
     * </p>
     *
     * @param message The raw error message to sanitize
     * @return A sanitized version of the message safe for user display
     */
    private String sanitizeMessage(String message) {
        // Basic sanitization - extend this for your specific security requirements
        return message.replaceAll("(?i)password|secret|key|token", "[REDACTED]");
    }
}