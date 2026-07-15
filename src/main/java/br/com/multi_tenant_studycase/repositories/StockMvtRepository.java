package br.com.multi_tenant_studycase.repositories;

import br.com.multi_tenant_studycase.entities.StockMvt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMvtRepository extends JpaRepository<StockMvt, String> {
    Page<StockMvt> findAllByProductId(String productId, Pageable pageable);
}
