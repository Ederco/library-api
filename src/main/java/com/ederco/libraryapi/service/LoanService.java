package com.ederco.libraryapi.service;

import com.ederco.libraryapi.api.dto.LoanFilterDTO;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);
    Optional<Loan> getById(Long id);
    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getALLLateLoans();

}
