package com.fastfoodmanager.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorService {

    private final GoogleAuthenticator gAuth;

    public TwoFactorService() {
        this.gAuth = new GoogleAuthenticator();
    }

    public String generateNewSecret() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public String getQRCodeUrl(String appName, String userEmail, String secret) {
        return "https://api.qrserver.com/v1/create-qr-code/?data=" +
                "otpauth://totp/" + appName + ":" + userEmail +
                "?secret=" + secret + "&issuer=" + appName +
                "&size=200x200";
    }

    public boolean validateCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}