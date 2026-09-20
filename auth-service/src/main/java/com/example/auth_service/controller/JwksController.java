package com.example.auth_service.controller;

import com.example.auth_service.security.RsaKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "JWKS", description = "Public JSON Web Key Set for RS256 access-token verification")
public class JwksController {

    private final RsaKeyService rsaKeyService;

    public JwksController(RsaKeyService rsaKeyService) {
        this.rsaKeyService = rsaKeyService;
    }

    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "JWKS")
    public Map<String, Object> jwks() {
        return rsaKeyService.publicJwkSet().toJSONObject();
    }
}
