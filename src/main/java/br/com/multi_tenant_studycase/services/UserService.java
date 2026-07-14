package br.com.multi_tenant_studycase.services;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.request.UserRequest;
import br.com.multi_tenant_studycase.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void createUser(final UserRequest request);

    void updateUser(final String userId, final UserRequest request);

    void deleteUser(final String userId);

    UserResponse getUserById(final String userId);

    PageResponse<UserResponse> getAllUsers(final int page, final int size);

    void enableUser(final String userId);

    void disableUser(final String userId);
}
