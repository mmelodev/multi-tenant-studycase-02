package br.com.multi_tenant_studycase.repositories;

import br.com.multi_tenant_studycase.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByReferenceIgnoreCase(String reference);
}
