package br.com.portflow.api.mapper;

import br.com.portflow.api.model.AuthModel;
import br.com.portflow.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    Usuario toEntity(AuthModel.NovoUsuarioVO vo);
}
