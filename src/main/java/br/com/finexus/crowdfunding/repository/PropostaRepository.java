package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.Proposta;
import br.com.finexus.crowdfunding.model.StatusProposta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, Long> {
    List<Proposta> findBySolicitanteId(Long usuarioId);

    List<Proposta> findByStatus(StatusProposta status);
}
