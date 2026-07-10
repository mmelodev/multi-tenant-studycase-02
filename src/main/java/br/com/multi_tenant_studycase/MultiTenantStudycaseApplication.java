package br.com.multi_tenant_studycase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MultiTenantStudycaseApplication {
	public static void main(String[] args) {
		SpringApplication.run(MultiTenantStudycaseApplication.class, args);
	}
}
