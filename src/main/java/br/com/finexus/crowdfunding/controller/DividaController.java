package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.Divida;
import br.com.finexus.crowdfunding.repository.DividaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dividas")
@CrossOrigin(origins = "*")
public class DividaController {

    @Autowired
    private DividaRepository dividaRepository;

    // Buscar dívida pela proposta
    @GetMapping("/proposta/{idProposta}")
    public ResponseEntity<?> getByProposta(@PathVariable Long idProposta) {
        Divida divida = dividaRepository.findByPropostaId(idProposta);
        if (divida == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(divida);
    }

    // Buscar dívida pelo usuário tomador
    @GetMapping("/tomador/{idUsuario}")
    public ResponseEntity<?> getByTomador(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(dividaRepository.findByTomadorId(idUsuario));
    }

}
