package com.ederco.libraryapi.service.impl;

import com.ederco.libraryapi.exception.BusinessException;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.repository.BookRepository;
import com.ederco.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        //existsByIsbn teve que ser criado na interface
        if(repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("Isbn já cadastrado.");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(book == null || book.getId() == null){
            throw new IllegalArgumentException("Book id can't be null");
        }
        this.repository.delete(book);

    }

    @Override
    public Book update(Book book) {
        //Antes estava assim...
        //return null;
        //Agora ficará assim...
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book id can't be null");
        }
        return this.repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        //Ao rodar o teste de service filtrar , apresentou este erro porquê devido "return null"
        //java.lang.NullPointerException: Cannot invoke "org.springframework.data.domain.Page.getTotalElements()" because "result" is null
        //return null;
        Example<Book> example = Example.of(filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return repository.findAll(example , pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return null;
    }
}
