package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.Saldo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaldoRepository extends JpaRepository<Saldo, Long> {
    Saldo findByUsuarioId(Long idUsuario);
}
