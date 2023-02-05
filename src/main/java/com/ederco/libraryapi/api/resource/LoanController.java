package com.ederco.libraryapi.api.resource;

import com.ederco.libraryapi.api.dto.BookDTO;
import com.ederco.libraryapi.api.dto.LoanDTO;
import com.ederco.libraryapi.api.dto.LoanFilterDTO;
import com.ederco.libraryapi.api.dto.ReturnedLoanDTO;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.entity.Loan;
import com.ederco.libraryapi.service.BookService;
import com.ederco.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto){
        Book book = bookService.getBookByIsbn(dto.getIsbn())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,"Book not found for passed isbn"));
        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = loanService.save(entity);

        return entity.getId();

    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto){

//        Loan loan = loanService.getById(id).get();//depois que obtive o método ,
//                                                  //vou setar o loan com o que o dto retornou
        Loan loan = loanService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        loan.setReturned(dto.getReturned());

        loanService.update(loan);

    }
    @GetMapping
    public Page<LoanDTO> find( LoanFilterDTO loanFilterDTO, Pageable pageable){
        Page<Loan> result = loanService.find(loanFilterDTO, pageable);
        //return null; //Por enquanto , foi deixado assim , pois ainda tem outras implementações.
        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map(entity -> {

                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;

                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loans,pageable,result.getTotalElements());
    }
}
