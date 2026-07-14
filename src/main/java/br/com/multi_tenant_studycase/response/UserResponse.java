package br.com.multi_tenant_studycase.response;

import br.com.multi_tenant_studycase.entities.UserRole;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
}
