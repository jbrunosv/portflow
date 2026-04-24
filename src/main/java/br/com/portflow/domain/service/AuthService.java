package br.com.portflow.domain.service;

import br.com.portflow.api.mapper.UsuarioMapper;
import br.com.portflow.api.model.AuthModel;
import br.com.portflow.core.security.JwtService;
import br.com.portflow.core.security.UserDetailsServiceImpl;
import br.com.portflow.domain.model.Usuario;
import br.com.portflow.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public void registrar(AuthModel.NovoUsuarioVO vo) {
        if (usuarioRepository.findByLogin(vo.login()).isPresent()) {
            throw new IllegalArgumentException("Não foi possível concluir o registro. Verifique se os dados são válidos ou tente escolher outro login.");
        }
        Usuario usuario = usuarioMapper.toEntity(vo);
        usuario.setSenha(passwordEncoder.encode(vo.senha()));
        
        List<String> permissoes = new ArrayList<>();
        if (Boolean.TRUE.equals(vo.permissaoLeituraGravacao())) {
            permissoes.add("leitura-gravacao");
        } else if (Boolean.TRUE.equals(vo.permissaoLeitura())) {
            permissoes.add("leitura");
        } else {
            throw new IllegalArgumentException("O usuário deve ter ao menos uma permissão.");
        }
        usuario.setPermissoes(permissoes);

        usuarioRepository.save(usuario);
    }

    public AuthModel.TokenDTO login(AuthModel.LoginVO vo) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(vo.login(), vo.senha())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(vo.login());
        String accessToken = jwtService.generateToken(userDetails, false);
        String refreshToken = jwtService.generateToken(userDetails, true);

        return new AuthModel.TokenDTO(accessToken, refreshToken);
    }
}
