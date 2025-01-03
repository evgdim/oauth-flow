package com.github.evgdim.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class AuthService {
    private HttpClient client = HttpClient.newHttpClient();
    private final GoogleIdTokenVerifier verifier;
    private final ObjectMapper jackson;


    public AuthService(@Value("${oauth.clientId}") String applicationClientId, ObjectMapper jackson) {
        this.jackson = jackson;
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(applicationClientId))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();
    }

    public TokenResponse getTokenFromCode(String code) throws IOException, InterruptedException {
        String requestBody = "client_id=113294925702-gqfvecvk5b4ndual6brpkc1hircvjhu5.apps.googleusercontent.com" +
                "&client_secret=BBjk6PL6d0GWAgFppPNMQuWY" +
                "&grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=http://localhost:8080/oauth/code";

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.google.com/o/oauth2/token")) // Endpoint URL
                .header("Content-Type", "application/x-www-form-urlencoded") // Set Content-Type
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody)) // Add the request body
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return jackson.readValue(response.body(), TokenResponse.class);
    }

    public TokenValidationResponse validateAccessToken(String accessToken) throws JsonProcessingException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + accessToken))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) throw new RuntimeException("invalid token");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return jackson.readValue(response.body(), TokenValidationResponse.class);
    }

    public GoogleIdToken validateIdToken(String token) throws GeneralSecurityException, IOException {
        return verifier.verify(token);
    }
}

