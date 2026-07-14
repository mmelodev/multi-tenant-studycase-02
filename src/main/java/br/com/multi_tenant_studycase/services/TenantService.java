package br.com.multi_tenant_studycase.services;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.request.RegisterTenantRequest;
import br.com.multi_tenant_studycase.response.TenantResponse;

public interface TenantService {
    void registerTenant(final RegisterTenantRequest request);

    void approveTenant(final String tenantId);

    void activateTenant(final String tenantId);

    void deactivateTenant(final String tenantId);

    void suspendTenant(final String tenantId);

    PageResponse<TenantResponse> findAll(final int page, final int size);
}
