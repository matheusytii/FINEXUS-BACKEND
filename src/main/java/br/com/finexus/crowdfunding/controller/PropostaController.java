package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.*;
import br.com.finexus.crowdfunding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/propostas")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PropostaController {

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Criar nova proposta
    @PostMapping
    public ResponseEntity<?> criarProposta(@RequestBody Proposta proposta) {

        if (proposta.getSolicitante() == null || proposta.getSolicitante().getId() == null) {
            return ResponseEntity.badRequest().body("Usuário inválido.");
        }

        Usuario usuario = usuarioRepository.findById(proposta.getSolicitante().getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"TOMADOR".equalsIgnoreCase(usuario.getTipo().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Somente usuários TOMADOR podem criar propostas.");
        }

        // validações básicas de entrada para evitar NPE / divisão por zero
        if (proposta.getValorSolicitado() == null || proposta.getValorSolicitado() <= 0) {
            return ResponseEntity.badRequest().body("valorSolicitado inválido.");
        }
        if (proposta.getFaturamentoMensal() == null || proposta.getFaturamentoMensal() <= 0) {
            return ResponseEntity.badRequest().body("faturamentoMensal inválido.");
        }
        if (proposta.getPrazoMeses() == null || proposta.getPrazoMeses() <= 0) {
            return ResponseEntity.badRequest().body("prazoMeses inválido.");
        }

        proposta.setSolicitante(usuario);
        proposta.setDataCriacao(LocalDateTime.now());
        proposta.setStatus(StatusProposta.ABERTA);

        // --- CÁLCULO DO RISCO ---
        double risco = 0;

        double relacao = proposta.getValorSolicitado() / proposta.getFaturamentoMensal();
        if (relacao < 0.5)
            risco += 10;
        else if (relacao < 1.0)
            risco += 25;
        else if (relacao < 2.0)
            risco += 45;
        else
            risco += 70;

        if (proposta.getTempoAtuacaoMeses() != null) {
            if (proposta.getTempoAtuacaoMeses() >= 36)
                risco -= 15;
            else if (proposta.getTempoAtuacaoMeses() >= 12)
                risco -= 5;
            else
                risco += 10;
        } else {
            // se tempoAtuacaoMeses for nulo, trate como recente (maior risco)
            risco += 10;
        }

        risco = Math.min(100, Math.max(0, risco));
        proposta.setRiscoCalculado((int) Math.round(risco)); // usa o setter existente

        double jurosBase;

        if (risco <= 25)
            jurosBase = 2.5; // Baixo risco
        else if (risco <= 50)
            jurosBase = 3.8; // Risco moderado
        else if (risco <= 75)
            jurosBase = 5.2; // Risco alto
        else
            jurosBase = 7.5; // Risco muito alto

        double jurosFinal = jurosBase + (proposta.getPrazoMeses() * 0.02);
        proposta.setTaxaJuros(jurosFinal);

        // Aumento de juros por inadimplência prévia
        if (usuario.isInadimplente()) {
            int h = usuario.getHistoricoInadimplencia();

            if (h >= 5)
                proposta.setTaxaJuros(proposta.getTaxaJuros() + 5.0);
            else if (h >= 3)
                proposta.setTaxaJuros(proposta.getTaxaJuros() + 2.5);
            else if (h >= 1)
                proposta.setTaxaJuros(proposta.getTaxaJuros() + 1.0);
        }

        // --- VALOR TOTAL (juros compostos) ---
        double taxaCorrigida = proposta.getTaxaJuros(); // agora inclui inadimplência

        double total = proposta.getValorSolicitado()
                * Math.pow(1 + (taxaCorrigida / 100), proposta.getPrazoMeses());

        proposta.setValorTotalPagar(total);

        Proposta salva = propostaRepository.save(proposta);

        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    // Buscar propostas de um usuário
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long idUsuario) {
        List<Proposta> propostas = propostaRepository.findBySolicitanteId(idUsuario);
        if (propostas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma proposta encontrada para este usuário.");
        }
        return ResponseEntity.ok(propostas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return propostaRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada."));
    }

    // Atualizar status da proposta
    @PutMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Proposta> propostaOpt = propostaRepository.findById(id);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
        }

        Proposta proposta = propostaOpt.get();
        String novoStatusStr = body.get("status");

        try {
            StatusProposta novoStatus = StatusProposta.valueOf(novoStatusStr.toUpperCase());
            proposta.setStatus(novoStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Status inválido: " + novoStatusStr);
        }

        propostaRepository.save(proposta);
        return ResponseEntity.ok(proposta);
    }

    // Deletar proposta
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!propostaRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proposta não encontrada.");
        }

        propostaRepository.deleteById(id);
        return ResponseEntity.ok("Proposta deletada com sucesso!");
    }

    // Listar todas as propostas
    @GetMapping
    public ResponseEntity<List<Proposta>> listarTodas() {
        return ResponseEntity.ok(propostaRepository.findAll());
    }

    @GetMapping("/abertas")
    public ResponseEntity<List<Proposta>> listarAbertas() {
        return ResponseEntity.ok(propostaRepository.findByStatus(StatusProposta.ABERTA));
    }
}
