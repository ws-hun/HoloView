package com.boilingpoint.news.exception;

import com.boilingpoint.news.common.Result;
import com.boilingpoint.news.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        log.warn("Business request failed: code={}, message={}",
                exception.getCode(), exception.getMessage());
        return Result.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException exception) {
        return validationFailure(exception);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException exception) {
        return validationFailure(exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        log.warn("Request constraint validation failed: message={}", message);
        return Result.failure(ResultCode.BAD_REQUEST,
                message.isBlank() ? ResultCode.BAD_REQUEST.getMessage() : message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        String message = exception.getName() + ": 参数格式不正确";
        log.warn("Request parameter type mismatch: parameter={}, value={}, requiredType={}",
                exception.getName(), exception.getValue(),
                exception.getRequiredType() == null ? "unknown" : exception.getRequiredType().getSimpleName());
        return Result.failure(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameterException(MissingServletRequestParameterException exception) {
        String message = exception.getParameterName() + ": 参数不能为空";
        log.warn("Required request parameter missing: parameter={}", exception.getParameterName());
        return Result.failure(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler({AsyncRequestNotUsableException.class, AsyncRequestTimeoutException.class})
    public void handleAsyncRequestException(Exception exception, HttpServletRequest request) {
        log.debug("Async request ended: method={}, path={}, reason={}",
                request.getMethod(), request.getRequestURI(), exception.getClass().getSimpleName());
    }

    private Result<Void> validationFailure(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .distinct()
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = ResultCode.BAD_REQUEST.getMessage();
        }
        log.warn("Request binding validation failed: message={}", message);
        return Result.failure(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), exception);
        return Result.failure(ResultCode.INTERNAL_SERVER_ERROR);
    }

    private String formatFieldError(FieldError fieldError) {
        if (fieldError.isBindingFailure() || (fieldError.getCodes() != null
                && java.util.Arrays.stream(fieldError.getCodes()).anyMatch(code -> code.startsWith("typeMismatch")))) {
            return fieldError.getField() + ": 参数格式不正确";
        }
        String defaultMessage = fieldError.getDefaultMessage();
        return fieldError.getField() + ": "
                + (defaultMessage == null ? ResultCode.BAD_REQUEST.getMessage() : defaultMessage);
    }
}
