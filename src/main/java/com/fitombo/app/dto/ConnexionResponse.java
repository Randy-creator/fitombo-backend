package com.fitombo.app.dto;

public record ConnexionResponse(String accessToken, String tokenType, long expiresIn) {
    @Override
    public String toString() {
        return "ConnexionResponse[tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
