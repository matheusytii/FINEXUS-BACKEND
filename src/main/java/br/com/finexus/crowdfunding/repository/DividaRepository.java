package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.Divida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DividaRepository extends JpaRepository<Divida, Long> {

    // buscar dívida pela proposta
    Divida findByPropostaId(Long propostaId);

    // buscar dívidas pelo tomador
    List<Divida> findByTomadorId(Long tomadorId);
}
