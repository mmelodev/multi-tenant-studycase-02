package br.com.multi_tenant_studycase.services;

import java.util.List;

public interface BasicService <I, O>{
    void create (final I request);

    void update (final String id, final I request);

    List<O> findALl();

    O findById(final String id);

    void delete (final String id);
}