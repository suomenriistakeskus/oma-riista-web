package fi.riista.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class KeepaliveController {
    @RequestMapping(value = "/api/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping(HttpSession session) {
        session.setAttribute("ts", System.currentTimeMillis());
        return "ok";
    }
}
