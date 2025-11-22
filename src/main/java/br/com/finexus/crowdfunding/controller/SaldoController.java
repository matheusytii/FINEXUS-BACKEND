package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.Saldo;
import br.com.finexus.crowdfunding.repository.SaldoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/saldos")
@CrossOrigin(origins = "*")
public class SaldoController {

    @Autowired
    private SaldoRepository saldoRepository;

    // Buscar saldo por usuário
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getSaldoByUsuario(@PathVariable Long idUsuario) {
        Saldo saldo = saldoRepository.findByUsuarioId(idUsuario);
        if (saldo == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(saldo);
    }

    @PutMapping("/sacar/{idUsuario}")
    public ResponseEntity<?> sacar(@PathVariable Long idUsuario) {
        Saldo saldo = saldoRepository.findByUsuarioId(idUsuario);
        if (saldo == null)
            return ResponseEntity.badRequest().body("Saldo não encontrado para este usuário.");

        saldo.setValor(0.0);
        saldoRepository.save(saldo);

        return ResponseEntity.ok("Saque realizado. O saldo agora é R$ 0,00.");
    }
}
