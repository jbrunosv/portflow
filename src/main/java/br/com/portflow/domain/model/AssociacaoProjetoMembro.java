package br.com.portflow.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "associacao_projeto_membro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociacaoProjetoMembro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;
}
