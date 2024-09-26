package com.phatle.demo.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleOAuth2TokenResponse {
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private String scope;
    private String token_type;
    private String id_token;
}
