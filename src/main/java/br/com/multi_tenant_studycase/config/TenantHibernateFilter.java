package br.com.multi_tenant_studycase.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

//@Aspect
//@Component
public class TenantHibernateFilter {
    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* br.com.multi_tenant_studycase.services.*.*(..))") //adicionar caminho dos arquivos services onde estão as regras de negócio.
    public void activateFilter(){
        final String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            final Session session = entityManager.unwrap(Session.class);
            //ativando o filtro para injetar no tenantId
            session.enableFilter("tenantFilter")
                    .setParameter("tenantId", tenantId); //exatamente ao parametro :tenantId no AbstractEntity
        }
    }
}
