package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Todo;
import com.mycompany.myapp.repository.TodoRepository;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Todo}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class TodoResource {

    private final Logger log = LoggerFactory.getLogger(TodoResource.class);

    private static final String ENTITY_NAME = "todo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TodoRepository todoRepository;

    public TodoResource(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * {@code POST  /todos} : Create a new todo.
     *
     * @param todo the todo to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new todo, or with status {@code 400 (Bad Request)} if the todo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/todos")
    public Mono<ResponseEntity<Todo>> createTodo(@RequestBody Todo todo) throws URISyntaxException {
        log.debug("REST request to save Todo : {}", todo);
        if (todo.getId() != null) {
            throw new BadRequestAlertException("A new todo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return todoRepository
            .save(todo)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/todos/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /todos/:id} : Updates an existing todo.
     *
     * @param id the id of the todo to save.
     * @param todo the todo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated todo,
     * or with status {@code 400 (Bad Request)} if the todo is not valid,
     * or with status {@code 500 (Internal Server Error)} if the todo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/todos/{id}")
    public Mono<ResponseEntity<Todo>> updateTodo(@PathVariable(value = "id", required = false) final Long id, @RequestBody Todo todo)
        throws URISyntaxException {
        log.debug("REST request to update Todo : {}, {}", id, todo);
        if (todo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, todo.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return todoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return todoRepository
                    .save(todo)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /todos/:id} : Partial updates given fields of an existing todo, field will ignore if it is null
     *
     * @param id the id of the todo to save.
     * @param todo the todo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated todo,
     * or with status {@code 400 (Bad Request)} if the todo is not valid,
     * or with status {@code 404 (Not Found)} if the todo is not found,
     * or with status {@code 500 (Internal Server Error)} if the todo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/todos/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Todo>> partialUpdateTodo(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Todo todo
    ) throws URISyntaxException {
        log.debug("REST request to partial update Todo partially : {}, {}", id, todo);
        if (todo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, todo.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return todoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Todo> result = todoRepository
                    .findById(todo.getId())
                    .map(existingTodo -> {
                        if (todo.getItem() != null) {
                            existingTodo.setItem(todo.getItem());
                        }
                        if (todo.getDescription() != null) {
                            existingTodo.setDescription(todo.getDescription());
                        }
                        if (todo.getCompleted() != null) {
                            existingTodo.setCompleted(todo.getCompleted());
                        }

                        return existingTodo;
                    })
                    .flatMap(todoRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /todos} : get all the todos.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of todos in body.
     */
    @GetMapping("/todos")
    public Mono<List<Todo>> getAllTodos() {
        log.debug("REST request to get all Todos");
        return todoRepository.findAll().collectList();
    }

    /**
     * {@code GET  /todos} : get all the todos as a stream.
     * @return the {@link Flux} of todos.
     */
    @GetMapping(value = "/todos", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Todo> getAllTodosAsStream() {
        log.debug("REST request to get all Todos as a stream");
        return todoRepository.findAll();
    }

    /**
     * {@code GET  /todos/:id} : get the "id" todo.
     *
     * @param id the id of the todo to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the todo, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/todos/{id}")
    public Mono<ResponseEntity<Todo>> getTodo(@PathVariable Long id) {
        log.debug("REST request to get Todo : {}", id);
        Mono<Todo> todo = todoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(todo);
    }

    /**
     * {@code DELETE  /todos/:id} : delete the "id" todo.
     *
     * @param id the id of the todo to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/todos/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteTodo(@PathVariable Long id) {
        log.debug("REST request to delete Todo : {}", id);
        return todoRepository
            .deleteById(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                    .build()
            );
    }
}
