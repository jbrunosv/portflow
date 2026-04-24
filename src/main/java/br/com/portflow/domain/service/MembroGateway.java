package br.com.portflow.domain.service;

import br.com.portflow.api.model.MembroModel;

import java.util.List;

public interface MembroGateway {
    MembroModel.DTO criar(MembroModel.SaveVO vo);

    MembroModel.DTO buscarPorId(Long membroId);

    List<MembroModel.DTO> listar();
}
