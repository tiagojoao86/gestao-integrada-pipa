package br.com.grupopipa.gestaointegrada.config.security;

import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioDTO;
import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioService;
import br.com.grupopipa.gestaointegrada.config.security.dto.AuthorityDTO;
import br.com.grupopipa.gestaointegrada.config.security.dto.AuthRequest;
import br.com.grupopipa.gestaointegrada.config.security.dto.AuthResponse;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.R_AUTHENTICATE;
import static br.com.grupopipa.gestaointegrada.core.controller.Response.forbidden;
import static br.com.grupopipa.gestaointegrada.core.controller.Response.ok;

@RestController
@RequestMapping(R_AUTHENTICATE)
public class AuthenticationController {

    private AuthenticationService authenticationService;
    private UsuarioService usuarioService;
    private AuthenticationManager authenticationManager;
    private UserDetailsServiceImpl userDetailsService;

    public AuthenticationController(AuthenticationService authenticationService, 
            UsuarioService usuarioEntityBusiness,
            AuthenticationManager authenticationManager,
            UserDetailsServiceImpl userDetailsService) {
        this.authenticationService = authenticationService;
        this.usuarioService = usuarioEntityBusiness;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    public Response authenticate(
            @RequestBody AuthRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String accessToken = authenticationService.authenticate(authentication.getName(),
                authentication.getAuthorities());

        String refreshToken = authenticationService.generateRefreshToken(authentication.getName(),
                authentication.getAuthorities());

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/authenticate/refresh");
        response.addCookie(refreshCookie);

        UsuarioDTO userDTO = this.usuarioService.findUsuarioDTOByLogin(authentication.getName());
        List<AuthorityDTO> authorities = this.usuarioService.findAuthoritiesByLogin(authentication.getName());

        AuthResponse authResponse = new AuthResponse(accessToken, userDTO.getLogin(), userDTO.getNome(),
                authorities);

        return ok(authResponse);

    }

    @PostMapping("refresh")
    public Response refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !authenticationService.validateToken(refreshToken)) {
            return forbidden("Refresh token invalid or expired");
        }

        String username = authenticationService.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccessToken = authenticationService.authenticate(username, userDetails.getAuthorities());

        UsuarioDTO userDTO = this.usuarioService.findUsuarioDTOByLogin(username);
        List<AuthorityDTO> authorities = this.usuarioService.findAuthoritiesByLogin(username);

        AuthResponse authResponse = new AuthResponse(newAccessToken, userDTO.getLogin(), userDTO.getNome(),
                authorities);

        return ok(authResponse);
    }
}
