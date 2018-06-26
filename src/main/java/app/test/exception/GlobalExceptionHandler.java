package app.test.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ResponseStatus(value=HttpStatus.BAD_GATEWAY, reason="Connection limit was exceeded.") // return 502 error
    @ExceptionHandler(BadGatewayException.class)
    public void handleException() {
		System.out.println("Connection limit was exceeded.");
    }
	
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Internal Server Error.") // return 500 error
    @ExceptionHandler(InternalServerException.class)
    public void handleInternalException() {
		System.out.println("Aspect join point exception.");
    }
}
