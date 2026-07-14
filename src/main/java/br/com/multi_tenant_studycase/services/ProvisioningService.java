package br.com.multi_tenant_studycase.services;

import br.com.multi_tenant_studycase.entities.Tenant;

public interface ProvisioningService {
    void provisionTenant(final Tenant tenant);
}
