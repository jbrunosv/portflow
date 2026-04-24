package br.com.portflow.api.mapper;

import br.com.portflow.api.model.MembroModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MembroMapper {
    MembroModel.ListDTO toListDTO(MembroModel.DTO dto);

    List<MembroModel.ListDTO> toListDTOs(List<MembroModel.DTO> dtos);
}
