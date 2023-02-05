package com.ederco.libraryapi.model.repository;

import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.entity.Loan;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static com.ederco.libraryapi.model.repository.BookRepositoryTest.createNewBook;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest //Para testes de integração
public class LoanRepositoryTest {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro.")
    public void existsByBookAndNotReturnedTest() {
        //cenário
        Loan loan = createAndPersistLoan(LocalDate.now());
        Book book = loan.getBook();

        //execução
        boolean exists = loanRepository.existsByBookAndNotReturned(book);

        Assertions.assertThat(exists).isTrue();

    }
    @Test
    @DisplayName(" Deve buscar empréstimo pelo isbn do livro ou customer ")
    public void findByBookIsbnOrCustomerTest(){
        //cenário
        Loan loan = createAndPersistLoan(LocalDate.now());

        Page<Loan> result = loanRepository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 10));

        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent()).contains(loan);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);

    }
    @Test
    @DisplayName("Deve obter empréstimos cuja data empréstimo for menor ou igual a trê dias atrás e não retornados")
    public void findByLoanDateLessThanAndNotReturnedTest(){
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).hasSize(1).contains(loan);
    }
    @Test
    @DisplayName("Deve retornar vazio quando não houver empréstimos atrasados")
    public void notFindByLoanDateLessThanAndNotReturnedTest(){
        Loan loan = createAndPersistLoan(LocalDate.now());

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).isEmpty();
    }

    public Loan createAndPersistLoan(LocalDate loanDate){
        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("Fulano")
                .loanDate(loanDate)
                .build();
        entityManager.persist(loan);

        return loan;
    }


}
