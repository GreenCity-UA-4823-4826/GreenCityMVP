package greencity.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController implements ErrorController {

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == null) {
            return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK.value(),
                "error", HttpStatus.OK.getReasonPhrase()));
        }

        int rawStatus;
        try {
            rawStatus = Integer.parseInt(statusCode.toString());
        } catch (NumberFormatException ex) {
            rawStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        HttpStatus status = HttpStatus.resolve(rawStatus);

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(Map.of(
            "status", status.value(),
            "error", status.getReasonPhrase()));
    }
}