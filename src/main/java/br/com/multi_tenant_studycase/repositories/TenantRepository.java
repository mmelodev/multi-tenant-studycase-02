package br.com.multi_tenant_studycase.repositories;

import br.com.multi_tenant_studycase.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    boolean existsByCompanyCode(String companyCode);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}
