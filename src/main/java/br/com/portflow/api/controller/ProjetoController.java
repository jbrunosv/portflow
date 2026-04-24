package br.com.portflow.api.controller;

import br.com.portflow.api.mapper.ProjetoMapper;
import br.com.portflow.api.model.ProjetoModel;
import br.com.portflow.domain.model.Projeto;
import br.com.portflow.domain.model.StatusProjeto;
import br.com.portflow.domain.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Recursos para gerenciar o ciclo de vida e andamento do portfólio de projetos")
@SecurityRequirement(name = "bearerAuth")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final ProjetoMapper projetoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('leitura-gravacao')")
    @Operation(summary = "Cria um novo projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ProjetoModel.DTO criar(@RequestBody @Valid ProjetoModel.SaveVO vo) {
        Projeto projeto = projetoService.salvar(vo);
        return projetoMapper.toDTO(projeto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('leitura-gravacao')")
    @Operation(summary = "Atualiza os dados de um projeto existente e reavalia seu risco")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ProjetoModel.DTO atualizar(@PathVariable Long id, @RequestBody @Valid ProjetoModel.UpdateVO vo) {
        Projeto projeto = projetoService.atualizar(id, vo);
        return projetoMapper.toDTO(projeto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('leitura-gravacao')")
    @Operation(summary = "Exclui um projeto do portfólio, validando regras de status")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Projeto não pode ser excluído no status atual"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public void excluir(@PathVariable Long id) {
        projetoService.excluir(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('leitura') or hasAuthority('leitura-gravacao')")
    @Operation(summary = "Busca os detalhes completos de um projeto pelo seu identificador único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoModel.DTO buscarPorId(@PathVariable Long id) {
        Projeto projeto = projetoService.buscarPorId(id);
        return projetoMapper.toDTO(projeto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('leitura') or hasAuthority('leitura-gravacao')")
    @Operation(summary = "Consulta o portfólio de projetos utilizando filtros opcionais e paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de projetos retornada com sucesso")
    })
    public Page<ProjetoModel.ListPageDTO> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) StatusProjeto status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        ProjetoModel.FilterVO filterVO = new ProjetoModel.FilterVO(nome, status);
        return projetoService.listarComFiltros(filterVO, pageable)
                .map(projetoMapper::toListPageDTO);
    }

    @PostMapping("/{projetoId}/membros/{membroId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('leitura-gravacao')")
    @Operation(summary = "Associa um membro (apenas funcionário) ao projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro associado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Regras de associação não atendidas (limites, não é funcionário)"),
            @ApiResponse(responseCode = "404", description = "Projeto ou Membro não encontrado")
    })
    public void associarMembro(@PathVariable Long projetoId, @PathVariable Long membroId) {
        projetoService.associarMembro(projetoId, membroId);
    }
}
