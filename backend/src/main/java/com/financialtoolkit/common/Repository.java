package com.financialtoolkit.common;

import java.util.List;
import java.util.Optional;

public interface Repository<T extends Identifiable<ID>, ID> {
    T save(T item);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean existsById(ID id);

    void deleteById(ID id);
}
