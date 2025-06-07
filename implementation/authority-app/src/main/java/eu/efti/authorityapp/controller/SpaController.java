package eu.efti.authorityapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class SpaController {

    @RequestMapping(value = {
            "/",
            "/{path:[^.]*}",
            "/{path:^(?!api|actuator).*}/**/{path:[^.]*}"
    })
    public String forwardToSpa(HttpServletRequest request, @PathVariable String path) {
        log.info("Forwarding request to SPA: {}", request.getRequestURI());
        return "forward:/index.html";
    }
}
