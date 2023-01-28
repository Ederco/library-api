package com.ederco.libraryapi.service;

import com.ederco.libraryapi.model.entity.Book;

import java.util.Optional;

public interface BookService {

    Book save(Book any);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);
    //**Após criar o método é necessário acertar a classe de
    // implementação , no caso BookServiceImpl
}
