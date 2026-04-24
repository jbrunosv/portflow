package br.com.portflow.domain.service;

import br.com.portflow.domain.model.Membro;
import br.com.portflow.domain.repository.MembroRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembroService {

    private final MembroRepository membroRepository;

    @Transactional
    public Membro salvar(Membro membro) {
        return membroRepository.save(membro);
    }

    public Page<Membro> listarTodos(Pageable pageable) {
        return membroRepository.findAll(pageable);
    }

    public Membro buscarPorId(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membro não encontrado com ID: " + id));
    }
}
