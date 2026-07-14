package br.com.multi_tenant_studycase.auth.services;

import br.com.multi_tenant_studycase.auth.request.LoginRequest;
import br.com.multi_tenant_studycase.auth.responses.LoginResponse;
import br.com.multi_tenant_studycase.entities.User;
import br.com.multi_tenant_studycase.security.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        public LoginResponse login (final LoginRequest request) {
            final Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            final User user = (User) authentication.getPrincipal();

            final String token = this.jwtTokenService.generateAccessToken(user.getTenantId(),
                    user.getId(),
                    user.getRole()
                            .name());
            final String tokenType = "Bearer";

            return LoginResponse.builder()
                    .accessToken(token)
                    .tokenType(tokenType)
                    .build();
        }
    }
}
