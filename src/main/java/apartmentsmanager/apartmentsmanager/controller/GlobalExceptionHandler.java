package apartmentsmanager.apartmentsmanager.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception ex, Model model) {
        logger.error("Unhandled exception for {} {}",
            request != null ? request.getMethod() : "UNKNOWN",
            request != null ? request.getRequestURI() : "UNKNOWN",
            ex
        );
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorStack", toStackTrace(ex));
        return "error";
    }

    private static String toStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
