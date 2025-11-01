package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.Investimento;
import br.com.finexus.crowdfunding.model.Proposta;
import br.com.finexus.crowdfunding.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {
    List<Investimento> findByInvestidor(Usuario investidor);
    List<Investimento> findByProposta(Proposta proposta);
}
