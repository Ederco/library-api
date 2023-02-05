package com.ederco.libraryapi.service;

import com.ederco.libraryapi.api.dto.LoanFilterDTO;
import com.ederco.libraryapi.exception.BusinessException;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.entity.Loan;
import com.ederco.libraryapi.model.repository.LoanRepository;
import com.ederco.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

//    private LoanRepository repository;
    @MockBean
    LoanRepository repository;

//    private LoanService service;
    LoanService service;
    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest(){
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";


        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .loanDate(LocalDate.now())
                .customer(customer)
                .book(book)
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = service.save(savingLoan);

        Assertions.assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        Assertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        Assertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
        Assertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());

    }
    @Test
    @DisplayName("Deve lançar um erro ao tentar salvar um livro já emprestado")
    public void loanedBookSaveTest(){
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);


        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        Assertions.assertThat(exception).isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        Mockito.verify(repository, never()).save(savingLoan);
    }
    @Test
    @DisplayName("Deve obter as infornações de um empréstimo pelo id")
    public void getLoanDetailsTest(){
        //cenário
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        //execução
        Optional<Loan> result = service.getById(id);

        //verificação
        Assertions.assertThat(result.isPresent()).isTrue();
        AbstractLongAssert<?> equalTo = Assertions.assertThat(result.get().getId()).isEqualTo(id);
        Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify(repository).findById(id);

    }
    @Test
    @DisplayName("Deve atualizar um empréstimo.")
    public void updateLoanTest(){
        //cenário
        Loan loan = createLoan();
        loan.setId(1l);
        loan.setReturned(true);

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        Assertions.assertThat(updatedLoan.getReturned()).isTrue();
        verify(repository).save(loan);

    }
    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoansTest() {
        //cenario
        LoanFilterDTO loanFilterDTO =LoanFilterDTO.builder()
                .customer("Fulano")
                .isbn("321")
                .build();


        Loan loan = createLoan();
        loan.setId(1l);

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> lista = Arrays.asList(loan);
        //O PageImpl vai receber 3 parâmetros:
        //Page<Book> page = new PageImpl<Book>(list, pageRequest,1);
        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
        Mockito.when(repository.findByBookIsbnOrCustomer(Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execução
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        //verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    public static Loan createLoan(){
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }
}
