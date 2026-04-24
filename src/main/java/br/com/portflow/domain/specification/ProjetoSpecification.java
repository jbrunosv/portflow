package br.com.portflow.domain.specification;

import br.com.portflow.api.model.ProjetoModel;
import br.com.portflow.domain.model.Projeto;
import org.springframework.data.jpa.domain.Specification;

public class ProjetoSpecification {

    public static Specification<Projeto> comFiltros(ProjetoModel.FilterVO filterVO) {
        Specification<Projeto> spec = (root, query, cb) -> cb.conjunction();
        
        if (filterVO.nome() != null && !filterVO.nome().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + filterVO.nome().toLowerCase() + "%"));
        }
        if (filterVO.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statusAtual"), filterVO.status()));
        }
        
        return spec;
    }
}
