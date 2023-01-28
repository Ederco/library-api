package com.ederco.libraryapi.service.impl;

import com.ederco.libraryapi.exception.BusinessException;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.repository.BookRepository;
import com.ederco.libraryapi.service.BookService;
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
            throw new IllegalArgumentException("Book id can't be null")
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
}
