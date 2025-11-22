package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.*;
import br.com.finexus.crowdfunding.repository.*;
import br.com.finexus.crowdfunding.service.AvaliacaoRiscoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/propostas")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PropostaController {

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FormularioRiscoRepository formularioRiscoRepository;

    @Autowired
    private AvaliacaoRiscoService avaliacaoRiscoService;

    // Criar nova proposta
    @PostMapping
    public ResponseEntity<?> criarProposta(@RequestBody Proposta proposta) {
        if (proposta.getSolicitante() == null || proposta.getSolicitante().getId() == null) {
            return ResponseEntity.badRequest().body("Usuário inválido.");
        }

        Usuario usuario = usuarioRepository.findById(proposta.getSolicitante().getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        proposta.setSolicitante(usuario);

        if (usuario.getTipo() == null || !usuario.getTipo().name().equalsIgnoreCase("TOMADOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Somente usuários do tipo TOMADOR podem criar propostas.");
        }

        Optional<FormularioRisco> formularioOpt = formularioRiscoRepository.findByUsuarioId(usuario.getId());
        if (formularioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Usuário precisa preencher o Formulário de Risco antes de criar uma proposta.");
        }

        List<Proposta> propostasAbertas = propostaRepository.findBySolicitanteId(usuario.getId())
                .stream()
                .filter(p -> p.getStatus() != null && p.getStatus().equals(StatusProposta.ABERTA))
                .toList();

        if (propostasAbertas.size() >= 3) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você já possui 3 propostas ativas. Feche uma proposta antes de criar uma nova.");
        }

        FormularioRisco form = formularioOpt.get();
        proposta.setFormularioRisco(form);
        proposta.setStatus(StatusProposta.ABERTA);
        proposta.setDataCriacao(java.time.LocalDateTime.now());

        // Cálculo da taxa e valor total
        double taxaJuros = avaliacaoRiscoService.calcularTaxaJuros(form.getPerfilRisco(), proposta.getPrazoMeses());
        proposta.setTaxaJuros(taxaJuros);

        double valorTotal = proposta.getValorSolicitado() * (1 + (taxaJuros / 100.0));
        proposta.setValorTotalPagar(valorTotal);

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
}
