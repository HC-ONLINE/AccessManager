package com.autenticacion.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String username;
    private String rol;

    public LoginResponse(String token, String email, String username, String rol) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.rol = rol;
    }
}
