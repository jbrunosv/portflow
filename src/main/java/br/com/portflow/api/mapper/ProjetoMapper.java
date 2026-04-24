package br.com.portflow.api.mapper;

import br.com.portflow.api.model.ProjetoModel;
import br.com.portflow.domain.model.AssociacaoProjetoMembro;
import br.com.portflow.domain.model.Projeto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjetoMapper {

    @Mapping(target = "gerenteId", ignore = true)
    @Mapping(target = "statusAtual", ignore = true)
    @Mapping(target = "risco", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "membrosAssociados", ignore = true)
    Projeto toEntity(ProjetoModel.SaveVO saveVO);

    @Mapping(target = "membrosIds", source = "membrosAssociados")
    ProjetoModel.DTO toDTO(Projeto projeto);

    ProjetoModel.ListPageDTO toListPageDTO(Projeto projeto);

    default List<Long> toMembrosIds(Set<AssociacaoProjetoMembro> associacoes) {
        if (associacoes == null) {
            return List.of();
        }
        return associacoes.stream().map(AssociacaoProjetoMembro::getMembroId).toList();
    }
}
