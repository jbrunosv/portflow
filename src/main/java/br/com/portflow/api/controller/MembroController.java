package br.com.portflow.api.controller;

import br.com.portflow.api.mapper.MembroMapper;
import br.com.portflow.api.model.MembroModel;
import br.com.portflow.domain.model.Membro;
import br.com.portflow.domain.service.MembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/membros")
@RequiredArgsConstructor
@Tag(name = "Membros", description = "Recursos para gerenciar membros que serão associados aos projetos")
@SecurityRequirement(name = "bearerAuth")
public class MembroController {

    private final MembroService membroService;
    private final MembroMapper membroMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('leitura-gravacao')")
    @Operation(summary = "Cria um novo membro")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Membro criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public MembroModel.DTO criar(@RequestBody @Valid MembroModel.SaveVO vo) {
        Membro membro = membroService.salvar(membroMapper.toEntity(vo));
        return membroMapper.toDTO(membro);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('leitura') or hasAuthority('leitura-gravacao')")
    @Operation(summary = "Lista os membros cadastrados com paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de membros retornada com sucesso")
    })
    public Page<MembroModel.ListPageDTO> listarTodos(Pageable pageable) {
        return membroService.listarTodos(pageable).map(membroMapper::toListPageDTO);
    }
}
