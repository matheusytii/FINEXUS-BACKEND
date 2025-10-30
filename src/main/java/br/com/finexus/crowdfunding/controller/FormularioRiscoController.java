package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.FormularioRisco;
import br.com.finexus.crowdfunding.model.Usuario;
import br.com.finexus.crowdfunding.repository.FormularioRiscoRepository;
import br.com.finexus.crowdfunding.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/formularios")
public class FormularioRiscoController {

    @Autowired
    private FormularioRiscoRepository formularioRiscoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ---------- Criar novo formulário ----------
    @PostMapping
    public ResponseEntity<?> criarFormulario(@RequestBody FormularioRisco formulario) {
        if (formulario.getUsuario() == null || formulario.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Usuário inválido.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(formulario.getUsuario().getId());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        formulario.setUsuario(usuarioOpt.get());

        // Avalia risco e perfil usando o método do modelo
        formulario.avaliarRisco();

        formularioRiscoRepository.save(formulario);
        return ResponseEntity.ok(formulario);
    }

    // ---------- Atualizar formulário existente ----------
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarFormulario(@PathVariable Long id, @RequestBody FormularioRisco formularioAtualizado) {
        Optional<FormularioRisco> formularioOpt = formularioRiscoRepository.findById(id);
        if (formularioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FormularioRisco formulario = formularioOpt.get();
        formulario.setRendaMensal(formularioAtualizado.getRendaMensal());
        formulario.setDividasTotais(formularioAtualizado.getDividasTotais());
        formulario.setTempoDeAtividadeMeses(formularioAtualizado.getTempoDeAtividadeMeses());
        formulario.setPossuiGarantia(formularioAtualizado.getPossuiGarantia());
        formulario.setHistoricoInadimplencia(formularioAtualizado.getHistoricoInadimplencia());

        // Avalia risco novamente
        formulario.avaliarRisco();

        formularioRiscoRepository.save(formulario);
        return ResponseEntity.ok(formulario);
    }

    // ---------- Buscar formulário por usuário ----------
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<FormularioRisco> buscarPorUsuario(@PathVariable Long idUsuario) {
        Optional<FormularioRisco> formularioOpt = formularioRiscoRepository.findByUsuarioId(idUsuario);
        return formularioOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ---------- Listar todos os formulários ----------
    @GetMapping
    public List<FormularioRisco> listarTodos() {
        return formularioRiscoRepository.findAll();
    }
}
