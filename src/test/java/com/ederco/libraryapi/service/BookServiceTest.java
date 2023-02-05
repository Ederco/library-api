package com.ederco.libraryapi.service;

import com.ederco.libraryapi.exception.BusinessException;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.repository.BookRepository;
import com.ederco.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;
    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    private static Book createValidBook() {
        return Book.builder()
                .isbn("123")
                .author("Fulano")
                .title("As Aventuras")
                .build();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBooKTest() {
        //cenário
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(Book.builder()
                .id(1L)
                .isbn("123")
                .title("As aventuras")
                .author("Fulano")
                .build());
        //execução
        Book savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado.")
    public void shouldnotSavedbookWithDuplicatedISBN() {
        //cenário
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        //execucao
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //verificações
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");
        //verificando que após o erro o livro não deve ser salvo:
        Mockito.verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Deve obter um livro por Id")
    public void getByIdTest() {
        //cenário
        Long id = 1l;

        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        //execução
        Optional<Book> foundBook = service.getById(id);

        //verificações
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base")
    public void bookNotFoundByIdTest() {
        //cenário
        Long id = 1l;
        //Deve retornar vazio , então não precisará do livro
        //Book book = createValidBook();
        //book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        //execução
        Optional<Book> book = service.getById(id);

        //verificações
        assertThat(book.isPresent()).isFalse();
//        assertThat(foundBook.get().getId()).isEqualTo(id);
//        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
//        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
//        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {
        //cenário
        Book book = Book.builder().id(1l).build();

        //execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        //verificação
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro existente")
    public void deleteInvalidBookTest() {

        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() {
        //cenário
        long id = 1l;

        //livro a atualizar
        Book updatingBook = Book.builder().id(id).build();

        //simulação
        Book updatedBook = createValidBook();
        updatedBook.setId(id);

        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

        //execução
        Book book = service.update(updatingBook);

        //verificação
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro existente")
    public void updateInvalidBookTest() {

        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest() {
        //cenario
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> lista = Arrays.asList(book);
        //O PageImpl vai receber 3 parâmetros:
        //Page<Book> page = new PageImpl<Book>(list, pageRequest,1);
        Page<Book> page = new PageImpl<>(lista, pageRequest, 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execução
        Page<Book> result = service.find(book, pageRequest);

        //verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbnTest() {
        //cenario
        String isbn = "1230";
        Mockito.when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder()
                .id(1L)
                .isbn(isbn)
                .build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        Mockito.verify(repository).findByIsbn(isbn);

    }

}
