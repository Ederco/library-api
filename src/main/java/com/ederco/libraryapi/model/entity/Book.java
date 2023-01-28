package com.ederco.libraryapi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Book {
    @Id
    @Column
    //GeneratedValue indica que o Id é do tipo autoincremento
    //e o "strategy" com o valor "IDENTITY" diz que é o banco de dados que vai gerar o
    //valor adicionado ao id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column //O nome da coluna na base de dados é
    // o mesmo da propriedade ,neste caso , "title"
    private String title;
    @Column
    private String author;
    @Column
    private String isbn;

}
