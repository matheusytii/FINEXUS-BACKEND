package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.Proposta;
import br.com.finexus.crowdfunding.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {
    List<Proposta> findBySolicitante(Usuario solicitante);
}
