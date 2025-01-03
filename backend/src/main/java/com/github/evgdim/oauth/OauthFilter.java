package com.github.evgdim.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class OauthFilter extends OncePerRequestFilter {

    private final ObjectMapper jackson;
    private final AuthService authService;

    public OauthFilter(ObjectMapper jackson, AuthService authService) {
        this.jackson = jackson;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getServletPath().startsWith("/oauth")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        Optional<Cookie> accessTokenCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_ID_TOKEN)).findFirst();
        if(accessTokenCookie.isPresent()){
            Cookie cookie = accessTokenCookie.get();
            GoogleIdToken tokenValidationResponse = null;
            try {
                tokenValidationResponse = authService.validateIdToken(cookie.getValue());
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            if(tokenValidationResponse != null) {//TODO needs better validation of the response
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            try {
                response.setStatus(403);
                response.getWriter().write("Invalid credentials");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }




}

record TokenValidationResponse(@JsonProperty("issued_to") String issuedTo,
                     @JsonProperty("audience") String audience,
                     @JsonProperty("user_id") String userId,
                     @JsonProperty("scope") String scope,
                     @JsonProperty("expires_in") Long expiresIn,
                               @JsonProperty("email") String email,
                               @JsonProperty("verified_email") String verifiedEmail,
                               @JsonProperty("access_type") String accessType) {}


//{
//        "issued_to" : "113294925702-gqfvecvk5b4ndual6brpkc1hircvjhu5.apps.googleusercontent.com",
//        "audience" : "113294925702-gqfvecvk5b4ndual6brpkc1hircvjhu5.apps.googleusercontent.com",
//        "user_id" : "108782800331268674911",
//        "scope" : "https://www.googleapis.com/auth/userinfo.email openid",
//        "expires_in" : 3585,
//        "email" : "evgeny.d.dimitrov@gmail.com",
//        "verified_email" : true,
//        "access_type" : "offline"
//        }