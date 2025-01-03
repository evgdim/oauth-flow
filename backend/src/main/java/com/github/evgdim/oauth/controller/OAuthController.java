package com.github.evgdim.oauth.controller;

import com.github.evgdim.oauth.AuthService;
import com.github.evgdim.oauth.Constants;
import com.github.evgdim.oauth.TokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;


@RestController
@RequestMapping("/oauth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OAuthController {
    private final Logger logger = LoggerFactory.getLogger(OAuthController.class);

    private final  String clientId;
    private final  String clientSecret;
    private final String redirectUri;
    private final String scope;
    private final String frontendLocation;
    private final AuthService authService;

    public OAuthController(
            @Value("${oauth.clientId}")
            String clientId,
            @Value("${oauth.clientSecret}")
            String clientSecret,
            @Value("${oauth.redirectUri}")
            String redirectUri,
            @Value("${oauth.scope}")
            String scope,
            @Value("${oauth.frontend.location}")
            String frontendLocation, AuthService authService) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.frontendLocation = frontendLocation;
        this.authService = authService;
    }

    @GetMapping
    public String urls() {
        return new StringBuilder()
                .append("https://accounts.google.com/o/oauth2/auth")
                .append("?client_id=").append(clientId)
                .append("&redirect_uri=").append(redirectUri)
                .append("&scope=").append(scope)
                .append("&response_type=").append("code")
                .toString();
    }

    @GetMapping("/code")
    public void code(@RequestParam("code") String code, HttpServletResponse response) throws IOException, InterruptedException {
        TokenResponse token = authService.getTokenFromCode(code);

        Cookie accessTokenCookie = buildCookie(Constants.COOKIE_ACCESS_TOKEN, token.accessToken(), 3600);
        response.addCookie(accessTokenCookie);
        Cookie idToken = buildCookie(Constants.COOKIE_ID_TOKEN, token.idToken(), 6*3600);
        response.addCookie(idToken);

        response.sendRedirect(frontendLocation);
    }



    private static Cookie buildCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(false);// Use HTTPS for production
        return cookie;
    }

    @GetMapping("/read-cookie")
    public String readHttpOnlyCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return "";
    }
}

