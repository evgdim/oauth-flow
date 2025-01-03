package com.github.evgdim.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(@JsonProperty("access_token") String accessToken,
                     @JsonProperty("expires_in") Long expiresIn,
                     @JsonProperty("scope") String scope,
                     @JsonProperty("token_type") String tokenType,
                     @JsonProperty("id_token") String idToken) {}