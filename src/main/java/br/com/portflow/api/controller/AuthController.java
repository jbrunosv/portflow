package br.com.portflow.api.controller;

import br.com.portflow.api.model.AuthModel;
import br.com.portflow.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um novo usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou registro falhou")
    })
    public void registrar(@RequestBody @Valid AuthModel.NovoUsuarioVO vo) {
        authService.registrar(vo);
    }

    @PostMapping("/login")
    @Operation(summary = "Realiza login e retorna tokens JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login bem sucedido e tokens retornados"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public AuthModel.TokenDTO login(@RequestBody @Valid AuthModel.LoginVO vo) {
        return authService.login(vo);
    }
}
