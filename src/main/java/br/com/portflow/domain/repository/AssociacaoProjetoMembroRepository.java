package br.com.portflow.domain.repository;

import br.com.portflow.domain.model.AssociacaoProjetoMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssociacaoProjetoMembroRepository extends JpaRepository<AssociacaoProjetoMembro, Long> {
    List<AssociacaoProjetoMembro> findByMembroId(Long membroId);
    boolean existsByProjetoIdAndMembroId(Long projetoId, Long membroId);
    long countByProjetoId(Long projetoId);
}
