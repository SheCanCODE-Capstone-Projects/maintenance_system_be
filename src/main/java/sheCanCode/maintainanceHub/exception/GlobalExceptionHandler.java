package sheCanCode.maintainanceHub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage();
        Map<String, String> body = new HashMap<>();
        if ("USER_NOT_FOUND".equals(msg)) {
            body.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        if ("EMAIL_EXISTS".equals(msg)) {
            body.put("error", "Email already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        if ("WRONG_PASSWORD".equals(msg)) {
            body.put("error", "Current password is incorrect");
            return ResponseEntity.badRequest().body(body);
        }
        body.put("error", "An unexpected error occurred");
        return ResponseEntity.internalServerError().body(body);
    }
}
