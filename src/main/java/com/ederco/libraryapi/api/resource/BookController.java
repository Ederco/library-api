package com.ederco.libraryapi.api.resource;

import com.ederco.libraryapi.api.dto.BookDTO;
import com.ederco.libraryapi.api.exceptions.ApiErrors;
import com.ederco.libraryapi.exception.BusinessException;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.service.BookService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;

    public BookController(BookService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto){
//        Sem ModelMapper
//        Book entity = Book
//                .builder()
//                .author(dto.getAuthor())
//                .title(dto.getTitle())
//                .isbn(dto.getIsbn())
//                .build();

//        Com ModelMapper
        Book entity = modelMapper.map(dto, Book.class);
        entity = service.save(entity);

//        Sem ModelMapper
//        return BookDTO.builder()
//                .id(entity.getId())
//                .author(entity.getAuthor())
//                .title(entity.getTitle())
//                .isbn(entity.getIsbn())
//                .build();

//      Com ModelMapper
        return modelMapper.map(entity, BookDTO.class);

//  Ao utilizar o @RequestBody BookDTO dto , não precisamos mais fazer desta forma :
//        BookDTO dto = new BookDTO();
//        dto.setAuthor("Autor");
//        dto.setTitle("Meu Livro");
//        dto.setIsbn("1213212");
//        dto.setId(1L);
//        return dto;
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    //O @PathVariable reverencia a variável "{id}" do path que
    //constrói a URL injetando o PathVariable que tem o mesmo nome,
    //neste caso o id do tipo Long
    public BookDTO get(@PathVariable Long id){
        //O método "map" mapeia o resultado da consulta do "getById"
        return service
                .getById(id)
                .map( book -> modelMapper.map(book,BookDTO.class) )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        //O getById retorna um Optional por isso utilizamos o .get na sequência
        //Book book = service.getById(id).get(); "Esta linha apresentou o erro NoSuchElementException",
        //por isso  , em lugar do .get utilizamos o .orElseThrow
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
        //Neste momento será necessário criar o método delete na interface de service
    }
    @PutMapping("{id}")
    //Por default retorna status 200 OK..
    //@ResponseStatus(HttpStatus.OK)
    public BookDTO update( @PathVariable Long id , BookDTO dto){
        //Podemos usar como base o método delete...
        return service.getById(id).map(book -> {

            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            book = service.update(book);
            return modelMapper.map(book, BookDTO.class);

        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();

        return new ApiErrors(bindingResult) ;
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessException(BusinessException ex){
        return new ApiErrors(ex);
    }
}
