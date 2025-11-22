package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.ParcelaDivida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelaDividaRepository extends JpaRepository<ParcelaDivida, Long> {

    List<ParcelaDivida> findByDividaId(Long dividaId);

    ParcelaDivida findByDividaIdAndNumeroParcela(Long dividaId, Integer numeroParcela);
}
