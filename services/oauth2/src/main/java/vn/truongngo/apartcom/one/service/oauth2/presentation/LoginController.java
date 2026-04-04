package vn.truongngo.apartcom.one.service.oauth2.presentation;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class LoginController {

    private final String googleAuthUri;

    public LoginController(@Value("${spring.security.oauth2.authorizationserver.issuer}") String issuerUri) {
        this.googleAuthUri = issuerUri + "/oauth2/authorization/google";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("googleAuthUri", googleAuthUri);
        return "login";
    }

    /**
     * Pre-auth hint — lưu deviceHash vào HTTP session trước khi redirect sang Google OAuth2.
     * Session tồn tại xuyên suốt OAuth2 redirect flow nên DeviceAwareAuthenticationSuccessHandler
     * có thể đọc lại sau khi Google callback về.
     */
    @PostMapping("/login/device-hint")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deviceHint(@RequestBody Map<String, String> body, HttpSession session) {
        String deviceHash = body.get("deviceHash");
        if (deviceHash != null && !deviceHash.isBlank()) {
            session.setAttribute("pre_auth_device_hash", deviceHash);
        }
    }
}
