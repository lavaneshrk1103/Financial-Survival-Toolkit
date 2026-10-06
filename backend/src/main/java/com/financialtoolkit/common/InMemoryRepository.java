package com.financialtoolkit.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {
    private final Map<ID, T> store = new ConcurrentHashMap<>();

    @Override
    public T save(T item) {
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean existsById(ID id) {
        return store.containsKey(id);
    }

    @Override
    public void deleteById(ID id) {
        store.remove(id);
    }
}
