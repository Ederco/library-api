package com.ederco.libraryapi.api.resource;

import com.ederco.libraryapi.api.dto.BookDTO;
import com.ederco.libraryapi.exception.BusinessException;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBook();
        Book savedBook = Book
                .builder()
                .id(10L)
                .author("Arthur")
                .title("As Aventuras")
                .isbn("1213213")
                .build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(10L))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));

    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficiente para criação do livro.")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("deve lançar erro ao tentar cadastrar um livro comisbn já utilizado por outro")
    public void createBookWithDuplicateIsbn() throws Exception {

        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);
        String mensagemErro = "Isbn já cadastrado";
        BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException("Isbn já cadastrado"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(APPLICATION_JSON) //Como o método é um POST eu preciso passar um corpo (content).
                .accept(APPLICATION_JSON)
                .content(json); //Como o método é um POST eu preciso passar um corpo (content).
        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));

    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception {
        //cenário (given)
        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                //Quando o método é um GET eu NÂO preciso passar um corpo (content)
                .accept(APPLICATION_JSON);
        //Quando o método é um GET eu NÃO preciso passar um corpo (content)
        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procuraddo não existir")
    public void bookNotFoundTest() throws Exception {
        //cenário
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                //Quando o método é um GET eu NÂO preciso passar um corpo (content)
                .accept(APPLICATION_JSON);
        //Quando o método é um GET eu NÃO preciso passar um corpo (content)
        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {
        //cenário
        //Quando for deletar um livro , ele deve existir no servidor,
        //tenho que mockar o getById
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder()
                .id(1l)//Para deletar um livro no servidor só interessa o id
                .build()));
        //execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));
        //Quando o método é um DELETE eu NÂO preciso passar um corpo (content)
        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
    public void deleteInexistentBookTest() throws Exception {
        //cenário
        //Quando for deletar um livro , ele deve existir no servidor,
        //tenho que mockar o getById
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
        //execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));
        //Quando o método é um DELETE eu NÂO preciso passar um corpo (content)
        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    //Vai ser bem parecido com o delete...
    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        //cenário
        Long id = 1l;
        String json = new ObjectMapper().writeValueAsString(createNewBook());
        //Primeiramente verifica se o livro existe (getById) para atualizar o recurso no servidor
        //**Observe o código antes de introduzir uma váriável...
//        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder()
//                .id(1l)
//                .title("some title")
//                .author("some author")
//                .isbn("321")
//                .build()));
        //**Observe agora o código após introduzir uma váriável Book.builder...
        Book updatingBook = Book.builder()
                .id(1l)
                .title("some title")
                .author("some author")
                .isbn("1213213")
                .build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));
        //Temos que adicionar o objeto porquê por default o Mock retorna como "null"
        Book updatedBook = Book.builder()
                .id(id)
                .author("Arthur")
                .title("As Aventuras")
                .isbn("1213213")
                .build();
        BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);
        //execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON);
        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value("1213213"));

    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
    public void updateInexistentBookTest() throws Exception {
        //cenário
        String json = new ObjectMapper().writeValueAsString(createNewBook());
        //não vamos precisar de um liro , pois mockaremos como vazio "empty()"
//        Book updatingBook = Book.builder()
//                .id(1l)
//                .title("some title")
//                .author("some author")
//                .isbn("321")
//                .build();
        //Independente do id passado , deverá retornar 404 , então usamos o "Mockito.anyLong()",
        // e também retornamos o Optional vazio "empty()"...
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
        //execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON);
        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception {

        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        //A classe Pageable serve para fazermos buscas páginadas nos nossos repositorys
        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                //Para retornar um objeto de página retornamos "new PageImpl"
                //O primeiro parâmetro do PageImpl é o content no caso "Arrays.asList",
                //o segundo parãmetro do PageImpl é o "PageRequest.of" e
                //o terceiro parãmetro é o total de registros "total"
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100),1));

        //O ponto de interrogação quer dizer que vamos receber algum parâmetro via GET e
        //o "&" significa que virá mais parâmetros
        //"/api/books?"
        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(),
                book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }


    private static BookDTO createNewBook() {
        return BookDTO
                .builder()
                .author("Arthur")
                .title("As Aventuras")
                .isbn("1213213")
                .build();
    }
}
