package br.com.multi_tenant_studycase.services;

import br.com.multi_tenant_studycase.common.PageResponse;
import br.com.multi_tenant_studycase.response.StockMvtResponse;

import java.util.List;

public interface BasicService <I, O>{
    void create (final I request);

    void update (final String id, final I request);

    PageResponse<O> findALl(final int page, final int size);

    O findById(final String id);

    void delete (final String id);
}