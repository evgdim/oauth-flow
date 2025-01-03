package com.github.evgdim.oauth.controller;

import com.github.evgdim.oauth.Constants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Optional;

@RestController()
@RequestMapping("/profile")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProfileController {
    private HttpClient client = HttpClient.newHttpClient();
    @GetMapping
    public String getProfileInfo(HttpServletRequest request) throws IOException, InterruptedException {
        //TODO duplicate code with filter
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> accessTokenCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_ACCESS_TOKEN)).findFirst();
        Cookie cookie = accessTokenCookie.get();

        HttpRequest profileReq = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v1/userinfo?alt=json"))
                .GET()
                .header("Authorization", "Bearer "+cookie.getValue())
                .build();
        return client.send(profileReq, HttpResponse.BodyHandlers.ofString()).body();
    }
}
