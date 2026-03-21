package org.example.zenvybackend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageResolver messageResolver;

    public GlobalExceptionHandler(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {

        log.warn("Bad request: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.bad_request", "BAD_REQUEST"))
                .message(messageResolver.resolveExceptionMessage(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(messageResolver.get("error.forbidden", "FORBIDDEN"))
                .message(messageResolver.resolveExceptionMessage(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.bad_request", "BAD_REQUEST"))
                .message(messageResolver.get("error.message.data_constraint", "Request violates a data constraint"))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(messageResolver.get("error.not_found", "NOT_FOUND"))
                .message(messageResolver.resolveExceptionMessage(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFound(
            NoHandlerFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("URL not found: {}", ex.getRequestURL());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(messageResolver.get("error.not_found", "NOT_FOUND"))
                .message(messageResolver.get("error.message.url_not_found", "URL is not found"))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Unexpected Error: ", ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(messageResolver.get("error.internal_server_error", "INTERNAL_SERVER_ERROR"))
                .message(messageResolver.get("error.message.internal", "An error occurred within the service"))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ){

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.validation", "VALIDATION_ERROR"))
                .message(messageResolver.get("error.message.validation", "Validation failed"))
                .details(details)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ){

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(messageResolver.get("error.forbidden", "FORBIDDEN"))
                .message(messageResolver.get("error.message.access_denied", "You do not have permission to access this resource"))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            Exception ex,
            HttpServletRequest request
    ){

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(messageResolver.get("error.method_not_allowed", "METHOD_NOT_ALLOWED"))
                .message(messageResolver.get("error.message.method_not_allowed", "Wrong HTTP method used to access the API"))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.bad_request", "BAD_REQUEST"))
                .message(ex.getMessage().contains("Unrecognized field")
                        ? messageResolver.get("error.message.invalid_field_request_body", "Invalid field in request body")
                        : messageResolver.get("error.message.invalid_request_body", "Invalid request body"))
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.bad_request", "BAD_REQUEST"))
                .message(messageResolver.get("error.message.invalid_parameter_type", "Invalid parameter type (UUID expected)"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex,
            HttpServletRequest request
    ){

        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getMessage())
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.validation", "VALIDATION_ERROR"))
                .message(messageResolver.get("error.message.validation", "Validation failed"))
                .details(details)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {

        List<String> details = ex.getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.validation", "VALIDATION_ERROR"))
                .message(messageResolver.get("error.message.validation", "Validation failed"))
                .details(details)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.bad_request", "BAD_REQUEST"))
                .message(messageResolver.get(
                        "error.message.missing_parameter",
                        ex.getParameterName() + " parameter is required",
                        ex.getParameterName()
                ))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestPart(
            MissingServletRequestPartException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.bad_request", "BAD_REQUEST"))
                .message(messageResolver.get(
                        "error.message.missing_part",
                        ex.getRequestPartName() + " part is required",
                        ex.getRequestPartName()
                ))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResponse> handleMultipartException(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(messageResolver.get("error.bad_request", "BAD_REQUEST"))
                .message(messageResolver.get("error.message.invalid_multipart", "Invalid multipart request"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }


}
