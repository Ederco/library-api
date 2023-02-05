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

import java.util.Optional;

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
    public void returnTrueWhenIsbnExists() {
        //cenario
        String isbn = "123";
        //Podemos extrair este método , antes estava assim  ...
//        Book book = Book.builder()
//                .title("Aventuras")
//                .author("Fulano")
//                .isbn(isbn)
//                .build();
        //Depois ficou assim ...
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isTrue();
    }

    public static Book createNewBook(String isbn) {
        return Book.builder()
                .title("Aventuras")
                .author("Fulano")
                .isbn(isbn)
                .build();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado.")
    public void returnFalseWhenIsbnDoesntExists() {
        //cenario
        String isbn = "123";

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void findByIdTest() {
        //cenário
        Book book = createNewBook("123");
        entityManager.persist(book);

        //execução
        Optional<Book> foundBook = repository.findById(book.getId());

        //verificação
        assertThat(foundBook.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        //cenário
        Book book = createNewBook("123");

        //execução
        Book savedBook = repository.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
    }
    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){

        Book book = createNewBook("123");
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();

    }
}

