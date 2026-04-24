package br.com.portflow.api.controller;

import br.com.portflow.api.model.RelatorioModel;
import br.com.portflow.domain.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Geração de relatórios gerenciais do portfólio")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/portfolio")
    @PreAuthorize("hasAuthority('leitura') or hasAuthority('leitura-gravacao')")
    @Operation(summary = "Retorna o resumo geral do portfólio de projetos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    })
    public RelatorioModel.DTO gerarRelatorioPortfolio() {
        return relatorioService.gerarRelatorio();
    }
}
