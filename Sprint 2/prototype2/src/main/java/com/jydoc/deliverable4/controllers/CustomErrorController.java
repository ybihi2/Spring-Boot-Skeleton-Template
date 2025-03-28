package com.jydoc.deliverable4.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error status
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (status != null) {
            httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
        }

        // Add error information to model
        model.addAttribute("status", httpStatus.value());
        model.addAttribute("error", httpStatus.getReasonPhrase());
        model.addAttribute("message", getErrorMessage(exception));
        model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        model.addAttribute("timestamp", new Date());

        return "error";
    }

    private String getErrorMessage(Object exception) {
        if (exception != null) {
            Throwable ex = (Throwable) exception;
            // Customize message for specific errors
            if (ex.getMessage().contains("Field 'authority' doesn't have a default value")) {
                return "Database error: Required role configuration is missing. Please contact support.";
            }
            return ex.getMessage();
        }
        return "Unknown error occurred";
    }
}