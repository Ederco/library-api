package com.ederco.libraryapi.model.repository;

import com.ederco.libraryapi.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

//    Desta forma , irá fazer uma busca "fetch" de livros ,mas
//    não usaremos esta estratégia:
//    Book findBooksFetchLoans();
}
