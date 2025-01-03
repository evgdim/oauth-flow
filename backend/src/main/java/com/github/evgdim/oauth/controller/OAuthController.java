package com.github.evgdim.oauth.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.evgdim.oauth.Constants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


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
    private final RestTemplate restTemplate;
    private final ObjectMapper jackson;

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
            String frontendLocation,
            ObjectMapper jackson) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.restTemplate = new RestTemplate();
        this.frontendLocation = frontendLocation;
        this.jackson = jackson;
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
        TokenResponse token = getTokenJava(code);

        Cookie accessTokenCookie = buildCookie(Constants.COOKIE_ACCESS_TOKEN, token.accessToken(), 3600);
        response.addCookie(accessTokenCookie);
        Cookie refreshTokenCookie = buildCookie(Constants.COOKIE_REFRESH_TOKEN, token.accessToken(), 6*3600);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect(frontendLocation);
    }

    //TODO should be extracted in service class
    private TokenResponse getTokenJava(String code) throws IOException, InterruptedException {
        String requestBody = "client_id=113294925702-gqfvecvk5b4ndual6brpkc1hircvjhu5.apps.googleusercontent.com" +
                "&client_secret=BBjk6PL6d0GWAgFppPNMQuWY" +
                "&grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=http://localhost:8080/oauth/code";

        HttpClient client = HttpClient.newHttpClient();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.google.com/o/oauth2/token")) // Endpoint URL
                .header("Content-Type", "application/x-www-form-urlencoded") // Set Content-Type
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody)) // Add the request body
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return jackson.readValue(response.body(), TokenResponse.class);
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

record TokenResponse(@JsonProperty("access_token") String accessToken,
                     @JsonProperty("expires_in") Long expiresIn,
@JsonProperty("scope") String scope,
                             @JsonProperty("token_type") String tokenType,
                     @JsonProperty("id_token") String idToken) {}