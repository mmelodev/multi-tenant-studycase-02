package br.com.multi_tenant_studycase.controller;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.response.TenantResponse;
import br.com.multi_tenant_studycase.services.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant", description = "Tenant API")
public class TenantController {

    private final TenantService service;

    @PostMapping("/approve/{tenant-id}")
    public ResponseEntity<Void> approveTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.service.approveTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/activate/{tenant-id}")
    public ResponseEntity<Void> activateTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.service.activateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/deactivate/{tenant-id}")
    public ResponseEntity<Void> deactivateTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.service.deactivateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/suspend/{tenant-id}")
    public ResponseEntity<Void> suspendTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.service.suspendTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<TenantResponse>> findAllTenants(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(this.service.findAll(page, size));
    }
}
