package br.com.alura.forumhub.repository;

import br.com.alura.forumhub.model.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RespostaRepository extends JpaRepository<Resposta, Long> {

    List<Resposta> findByTopicoId(Long idTopico);
}