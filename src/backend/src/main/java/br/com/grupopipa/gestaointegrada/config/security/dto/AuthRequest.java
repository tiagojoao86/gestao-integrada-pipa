package br.com.grupopipa.gestaointegrada.config.security.dto;

import lombok.Getter;

@Getter
public class AuthRequest {
    private String username;
    private String password;
}
