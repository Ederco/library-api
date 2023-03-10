package com.ederco.libraryapi.api.resource;

import com.ederco.libraryapi.api.dto.LoanDTO;
import com.ederco.libraryapi.api.dto.LoanFilterDTO;
import com.ederco.libraryapi.api.dto.ReturnedLoanDTO;
import com.ederco.libraryapi.exception.BusinessException;
import com.ederco.libraryapi.model.entity.Book;
import com.ederco.libraryapi.model.entity.Loan;
import com.ederco.libraryapi.service.BookService;
import com.ederco.libraryapi.service.LoanService;
import com.ederco.libraryapi.service.LoanServiceTest;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;
    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um empr??stimo")
    public void createLoanTest() throws Exception {
        //cen??rio
        //O "Fulano vai pedir emprestado o livro cujo isbn ?? "123":
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .email("customer@email.com")
                .customer("Fulano")
                .build();
        //Aqui eu formei o meu json:
        String json = new ObjectMapper().writeValueAsString(dto);

        //Retorna um Optional , porqu?? pode ser que n??o existe um livro com este isbn...
        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder()
                .id(1l)
                .customer("Fulano")
                .book(book)
                .loanDate(LocalDate.now())
                .build();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empr??stimo de livro inexistente")
    public void invalidIsbnCreateLoanTest() throws Exception {
        //cen??rio
        //O "Fulano vai pedir emprestado o livro cujo isbn ?? "123":
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Fulano")
                .build();
        //Aqui eu formei o meu json:
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empr??stimo de livro emprestado")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {
        //cen??rio
        //O "Fulano vai pedir emprestado o livro cujo isbn ?? "123":
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Fulano")
                .build();
        //Aqui eu formei o meu json:
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));

    }

    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception {
        //cen??rio { returned: true }
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder()
                .returned(true)
                .build();
        Loan loan = Loan.builder()
                .id(1l)
                .build();
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isOk());

        Mockito.verify(loanService, Mockito.times(1)).update(loan);

    }
    @Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
    public void returnInexistentBookTest() throws Exception {
        //cen??rio { returned: true }
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder()
                .returned(true)
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);
        //N??o haver?? loan para retornar ...
//        Loan loan = Loan.builder()
//                .id(1l)
//                .build();
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }
    @Test
    @DisplayName("Deve filtrar empr??stimos")
    public void findLoansTest() throws Exception {
        //cen??rio
        Long id = 1l;
        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        Book book = Book.builder().id(1l).isbn("321").build();
        loan.setBook(book);


        //A classe Pageable serve para fazermos buscas p??ginadas nos nossos repositorys
        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                //Para retornar um objeto de p??gina retornamos "new PageImpl"
                //O primeiro par??metro do PageImpl ?? o content no caso "Arrays.asList",
                //o segundo par??metro do PageImpl ?? o "PageRequest.of" e
                //o terceiro par??metro ?? o total de registros "total"
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10),1));

        //O ponto de interroga????o quer dizer que vamos receber algum par??metro via GET e
        //o "&" significa que vir?? mais par??metros
        //"/api/books?"
        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                book.getIsbn(),
                loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

}
