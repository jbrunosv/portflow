package br.com.portflow.api.mapper;

import br.com.portflow.api.model.ProjetoModel;
import br.com.portflow.domain.model.Projeto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = { MembroMapper.class })
public interface ProjetoMapper {

    @Mapping(target = "gerente", ignore = true)
    @Mapping(target = "statusAtual", ignore = true)
    @Mapping(target = "risco", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "membrosAssociados", ignore = true)
    Projeto toEntity(ProjetoModel.SaveVO saveVO);

    @Mapping(target = "membros", source = "membrosAssociados")
    ProjetoModel.DTO toDTO(Projeto projeto);

    ProjetoModel.ListPageDTO toListPageDTO(Projeto projeto);
}
