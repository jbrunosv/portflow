package br.com.portflow.api.mapper;

import br.com.portflow.api.model.MembroModel;
import br.com.portflow.domain.model.AssociacaoProjetoMembro;
import br.com.portflow.domain.model.Membro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MembroMapper {

    @Mapping(target = "id", ignore = true)
    Membro toEntity(MembroModel.SaveVO saveVO);

    MembroModel.DTO toDTO(Membro membro);

    default MembroModel.DTO toDTO(AssociacaoProjetoMembro associacao) {
        if (associacao == null || associacao.getMembro() == null) {
            return null;
        }
        return toDTO(associacao.getMembro());
    }

    MembroModel.ListPageDTO toListPageDTO(Membro membro);
}
