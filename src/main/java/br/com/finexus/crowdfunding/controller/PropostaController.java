package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.*;
import br.com.finexus.crowdfunding.repository.*;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/propostas")
public class PropostaController {

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FormularioRiscoRepository formularioRiscoRepository;

    // Criar nova proposta (somente MEI, e com formulário de risco válido)
    @PostMapping
    public ResponseEntity<?> criarProposta(@RequestBody Proposta proposta) {
        if (proposta.getSolicitante() == null || proposta.getSolicitante().getId() == null) {
            return ResponseEntity.badRequest().body("Solicitante inválido.");
        }

        Optional<Usuario> solicitanteOpt = usuarioRepository.findById(proposta.getSolicitante().getId());
        if (solicitanteOpt.isEmpty() || solicitanteOpt.get().getTipo() != TipoUsuario.TOMADOR) {
            return ResponseEntity.badRequest().body("Apenas usuários do tipo MEI podem criar propostas.");
        }

        Usuario solicitante = solicitanteOpt.get();

        // 🔍 Verifica se o usuário preencheu o formulário de risco
        Optional<FormularioRisco> formularioOpt = formularioRiscoRepository.findByUsuarioId(solicitante.getId());
        if (formularioOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("É necessário preencher o formulário de risco antes de criar uma proposta.");
        }

        FormularioRisco formulario = formularioOpt.get();
        String perfilRisco = formulario.getPerfilRisco();

        // ✅ Cria proposta com base no perfil de risco
        proposta.setSolicitante(solicitante);
        proposta.setPerfilRisco(perfilRisco);
        proposta.setStatus(StatusProposta.ABERTA);
        proposta.setDataAbertura(LocalDate.now());

        // 🔹 Calcula taxa de juros automática baseada em perfil e prazo
        double taxaBase;
        switch (perfilRisco.toLowerCase()) {
            case "baixo" -> taxaBase = 2.5;
            case "médio" -> taxaBase = 5.5;
            case "alto" -> taxaBase = 8.5; // agora não rejeita, apenas taxa maior
            default -> taxaBase = 5.0;
        }

        if (proposta.getPrazoMeses() != null) {
            // Acrescenta 0,5% ao mês no juros por prazo adicional
            taxaBase += proposta.getPrazoMeses() * 0.5;
        }

        proposta.setTaxaJuros(taxaBase);

        // 🔹 Calcula o valor total a pagar (juros simples)
        if (proposta.getValorSolicitado() != null && proposta.getPrazoMeses() != null) {
            double valorTotal = proposta.getValorSolicitado() * (1 + (taxaBase / 100));
            proposta.setValorTotal(valorTotal);
        }

        propostaRepository.save(proposta);
        return ResponseEntity.ok(proposta);
    }

    // Listar todas as propostas
    @GetMapping
    public List<Proposta> listar() {
        return propostaRepository.findAll();
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Proposta> buscarPorId(@PathVariable Long id) {
        Optional<Proposta> proposta = propostaRepository.findById(id);
        return proposta.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Listar propostas de um solicitante específico
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Proposta>> listarPorUsuario(@PathVariable Long idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(propostaRepository.findBySolicitante(usuarioOpt.get()));
    }

    // Atualizar status (ex: aprovar)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody Proposta propostaBody) {
        Optional<Proposta> propostaOpt = propostaRepository.findById(id);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Proposta proposta = propostaOpt.get();
        StatusProposta status = propostaBody.getStatus();

        if (status == StatusProposta.APROVADA && "Alto".equalsIgnoreCase(proposta.getPerfilRisco())) {
            return ResponseEntity.badRequest().body("Não é possível aprovar propostas com risco alto.");
        }

        proposta.setStatus(status);
        if (status == StatusProposta.APROVADA) {
            proposta.setDataAprovacao(LocalDate.now());
        }

        propostaRepository.save(proposta);
        return ResponseEntity.ok(proposta);
    }

    @OneToMany(mappedBy = "proposta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Investimento> investimentos;

    public List<Investimento> getInvestimentos() {
        return investimentos;
    }

    public void setInvestimentos(List<Investimento> investimentos) {
        this.investimentos = investimentos;
    }
}
