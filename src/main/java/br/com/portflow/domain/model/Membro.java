package br.com.portflow.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "membro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Atribuicao atribuicao;
}
