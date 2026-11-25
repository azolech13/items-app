package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.com.betacom.itemsapp.domain.InvalidCredentialsException;
import pl.com.betacom.itemsapp.domain.ItemAccessDeniedException;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemVersionConflictException;
import pl.com.betacom.itemsapp.domain.LoginTakenException;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validationFailed(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed");
        problem.setProperty(
                "errors",
                exception.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .toList());
        return problem;
    }

    @ExceptionHandler(LoginTakenException.class)
    ProblemDetail loginTaken(LoginTakenException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail invalidCredentials(InvalidCredentialsException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    ProblemDetail itemNotFound(ItemNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ItemAccessDeniedException.class)
    ProblemDetail itemAccessDenied(ItemAccessDeniedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(ItemVersionConflictException.class)
    ProblemDetail itemVersionConflict(ItemVersionConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setProperty("version", exception.currentVersion());
        return problem;
    }
}
