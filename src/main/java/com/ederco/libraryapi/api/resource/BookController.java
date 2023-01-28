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
