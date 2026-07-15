package br.com.multi_tenant_studycase.auth.services;

import br.com.multi_tenant_studycase.auth.request.LoginRequest;
import br.com.multi_tenant_studycase.auth.responses.LoginResponse;
import org.springframework.stereotype.Service;

public interface AuthenticationService {
    LoginResponse login (final LoginRequest loginRequest);
}
