package br.com.portflow.domain.service;

import br.com.portflow.api.model.MembroModel;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MembroGatewayService implements MembroGateway {

    private final RestClient restClient;
    private final boolean mockEnabled;
    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final Map<Long, MembroModel.DTO> membrosMock = new ConcurrentHashMap<>();

    public MembroGatewayService(
            RestClient.Builder restClientBuilder,
            @Value("${integracao.membros.base-url}") String membrosBaseUrl,
            @Value("${integracao.membros.mock-enabled:true}") boolean mockEnabled) {
        this.restClient = restClientBuilder.baseUrl(membrosBaseUrl).build();
        this.mockEnabled = mockEnabled;
    }

    @Override
    public MembroModel.DTO criar(MembroModel.SaveVO vo) {
        if (mockEnabled) {
            Long id = idGenerator.getAndIncrement();
            MembroModel.DTO dto = new MembroModel.DTO(id, vo.nome(), vo.atribuicao());
            membrosMock.put(id, dto);
            return dto;
        }
        return restClient.post()
                .uri("/membros")
                .body(vo)
                .retrieve()
                .body(MembroModel.DTO.class);
    }

    @Override
    public MembroModel.DTO buscarPorId(Long membroId) {
        if (mockEnabled) {
            MembroModel.DTO membro = membrosMock.get(membroId);
            if (membro == null) {
                throw new EntityNotFoundException("Membro não encontrado com ID: " + membroId);
            }
            return membro;
        }
        return restClient.get()
                .uri("/membros/{id}", membroId)
                .retrieve()
                .body(MembroModel.DTO.class);
    }

    @Override
    public List<MembroModel.DTO> listar() {
        if (mockEnabled) {
            return new ArrayList<>(membrosMock.values());
        }
        return restClient.get()
                .uri("/membros")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
