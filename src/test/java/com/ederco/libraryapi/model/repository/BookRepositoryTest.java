package com.ederco.libraryapi.model.repository;

import com.ederco.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    //Este "EntityManager" pertence ao JPA(Java Persistense API) e
    // é utilizado dentro das implementações do JPARepositiry para
    // executar as operações da base de dados , ele que está a frente
    // junto ao banco de dados fazendo estas operações.
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado.")
    public void returnTrueWhenIsbnExists(){
        //cenario
        String isbn = "1213213";
        Book book = Book.builder()
                .title("Aventuras")
                .author("Fulano")
                .isbn(isbn)
                .build();
        entityManager.persist(book);

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isTrue();
    }
    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado.")
    public void returnFalseWhenIsbnDoesntExists(){
        //cenario
        String isbn = "1213213";

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isFalse();
    }

}
