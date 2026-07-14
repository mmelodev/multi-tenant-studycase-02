package br.com.multi_tenant_studycase.services.impl;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.entities.Tenant;
import br.com.multi_tenant_studycase.entities.TenantStatus;
import br.com.multi_tenant_studycase.entities.User;
import br.com.multi_tenant_studycase.entities.UserRole;
import br.com.multi_tenant_studycase.mapper.TenantMapper;
import br.com.multi_tenant_studycase.repositories.TenantRepository;
import br.com.multi_tenant_studycase.repositories.UserRepository;
import br.com.multi_tenant_studycase.request.RegisterTenantRequest;
import br.com.multi_tenant_studycase.response.TenantResponse;
import br.com.multi_tenant_studycase.services.ProvisioningService;
import br.com.multi_tenant_studycase.services.TenantService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {
    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ProvisioningService provisioningService;

    @Override
    public void registerTenant(RegisterTenantRequest request) {
        // check if the tenant already exists by company code
        if (this.tenantRepository.existsByCompanyCode(request.getCompanyCode())) {
            throw new RuntimeException("Tenant already exists");
        }

        // check if email already exits
        if (this.tenantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Tenant Email already exists");
        }

        // create tenant entity
        final Tenant tenant = this.tenantMapper.toEntity(request);
        tenant.setAdminPassword(this.passwordEncoder.encode(request.getAdminPassword()));
        tenant.setStatus(TenantStatus.PENDING);

        this.tenantRepository.save(tenant);
    }

    @Override
    public void approveTenant(String tenantId) {
        // check if tenant exists
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        // activate tenant
        tenant.setStatus(TenantStatus.ACTIVE);
        this.tenantRepository.save(tenant);

        try {
            // provision the schema for the tenant
            this.provisioningService.provisionTenant(tenant);
            // create initial admin user
            createInitialAdminUser(tenant);
        } catch (final Exception e) {
            rollbackTenantStatus(tenant);
        }
    }

    @Override
    public void activateTenant(String tenantId) {
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        if (tenant.getStatus() != TenantStatus.PENDING) {
            throw new RuntimeException("Tenant is not pending");
        }

        tenant.setStatus(TenantStatus.ACTIVE);
        this.tenantRepository.save(tenant);
    }

    @Override
    public void deactivateTenant(String tenantId) {
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new RuntimeException("Tenant is not pending");
        }

        tenant.setStatus(TenantStatus.INACTIVE);
        this.tenantRepository.save(tenant);
    }

    @Override
    public void suspendTenant(String tenantId) {
        final Tenant tenant = this.tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new RuntimeException("Tenant is not pending");
        }

        tenant.setStatus(TenantStatus.SUSPENDED);
        this.tenantRepository.save(tenant);
    }

    @Override
    public PageResponse<TenantResponse> findAll(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Tenant> tenants = this.tenantRepository.findAll(pageRequest);
        final Page<TenantResponse> tenantResponses = tenants.map(this.tenantMapper::toResponse);
        return PageResponse.of(tenantResponses);
    }

    private void rollbackTenantStatus(final Tenant tenant) {
        tenant.setStatus(TenantStatus.PENDING);
        this.tenantRepository.save(tenant);
    }

    private void createInitialAdminUser(final Tenant tenant) {
        // check if the user already exists
        if (this.userRepository.existsByUsername(tenant.getAdminUsername())) {
            throw new RuntimeException("User already exists");
        }

        final User adminUser = User.builder()
                .username(tenant.getAdminUsername())
                .email(tenant.getAdminEmail())
                .firstName(extractFirstName(tenant.getAdminFullName()))
                .lastName(extractLastName(tenant.getAdminFullName()))
                .password(tenant.getAdminPassword())
                .role(UserRole.ROLE_COMPANY_ADMIN)
                .tenant(tenant)
                .enabled(true)
                .build();
        this.userRepository.save(adminUser);
        log.info("Created initial admin user for tenant {}", tenant.getId());
    }

    private String extractFirstName(final String fullName) {
        return fullName.split(" ")[0];
    }

    private String extractLastName(final String fullName) {
        return fullName.split(" ").length > 1 ? fullName.split(" ")[1] : fullName;
    }
}
