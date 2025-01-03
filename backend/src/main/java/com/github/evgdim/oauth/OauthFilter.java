package com.github.evgdim.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class OauthFilter extends OncePerRequestFilter {
    private HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper jackson;

    public OauthFilter(ObjectMapper jackson) {
        this.jackson = jackson;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getServletPath().startsWith("/oauth")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        Optional<Cookie> accessTokenCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_ACCESS_TOKEN)).findFirst();
        if(accessTokenCookie.isPresent()){
            Cookie cookie = accessTokenCookie.get();
            TokenValidationResponse tokenValidationResponse = validateAccessToken(cookie.getValue());
            if(tokenValidationResponse != null) {//TODO needs better validation of the response
                filterChain.doFilter(request, response);
                return;
            };
        } else {
            try {
                response.setStatus(403);
                response.getWriter().write("Invalid credentials");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //TODO extract to utils class
    private TokenValidationResponse validateAccessToken(String accessToken) throws JsonProcessingException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + accessToken))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return jackson.readValue(response.body(), TokenValidationResponse.class);
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