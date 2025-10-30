package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.FormularioRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormularioRiscoRepository extends JpaRepository<FormularioRisco, Long> {
    Optional<FormularioRisco> findByUsuarioId(Long usuarioId);
}
