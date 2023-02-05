package com.ederco.libraryapi.api.resource;

import com.ederco.libraryapi.api.dto.BookDTO;
import com.ederco.libraryapi.api.dto.LoanDTO;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.entity.Loan;
import com.ederco.libraryapi.service.BookService;
import com.ederco.libraryapi.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;
    private final LoanService loanService;
    private final ModelMapper modelMapper;

//    Com o uso do @RequiredArgsConstructor não é necessário o uso do construtor ...
//    public BookController(BookService service, ModelMapper modelMapper, LoanService loanService) {
//        this.service = service;
//        this.modelMapper = modelMapper;
//        this.loanService = loanService;
//    }

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
    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest){
        Book filter = modelMapper.map(dto, Book.class);
        //**aqui havia apenas uma página de book "Page<Book>"
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent().stream()
                //retorna o "stream" de " BookDTO"
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        //Para criar uma página , passamos 3 parâmetros :
        //primeiro o conteúdo: "list"
        //segundo a página atual e quantos registros tem: "pageRequest"
        //terceiro o total de elementos :"result.getTotalElements()"
        return new PageImpl<BookDTO>(list, pageRequest,result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBOOK( @PathVariable Long id, Pageable pageable ){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        //Aqui estamos retornando uma consulta páginada de Loan , mas ...
        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        //precisamos converter para uma consulta de LoanDTO ...
        List<LoanDTO> list = result.getContent() //retorna a lista
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());

    }



}
