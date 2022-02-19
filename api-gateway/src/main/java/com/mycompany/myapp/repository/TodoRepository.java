package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Todo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Todo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TodoRepository extends R2dbcRepository<Todo, Long>, TodoRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Todo> findAll();

    @Override
    Mono<Todo> findById(Long id);

    @Override
    <S extends Todo> Mono<S> save(S entity);
}

interface TodoRepositoryInternal {
    <S extends Todo> Mono<S> insert(S entity);
    <S extends Todo> Mono<S> save(S entity);
    Mono<Integer> update(Todo entity);

    Flux<Todo> findAll();
    Mono<Todo> findById(Long id);
    Flux<Todo> findAllBy(Pageable pageable);
    Flux<Todo> findAllBy(Pageable pageable, Criteria criteria);
}
