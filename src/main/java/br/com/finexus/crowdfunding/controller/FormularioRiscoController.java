package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.FormularioRisco;
import br.com.finexus.crowdfunding.model.Usuario;
import br.com.finexus.crowdfunding.repository.FormularioRiscoRepository;
import br.com.finexus.crowdfunding.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/formularios")
@CrossOrigin(origins = "*", allowedHeaders = "*") // 🔓 libera requisições externas (Postman, front etc)
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

        Usuario usuario = usuarioOpt.get();

        // 🔒 Verifica se o usuário já possui formulário
        Optional<FormularioRisco> formularioExistente = formularioRiscoRepository.findByUsuarioId(usuario.getId());
        if (formularioExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Usuário já possui um formulário cadastrado. Edite o existente.");
        }

        formulario.setUsuario(usuario);
        formulario.avaliarRisco();

        formularioRiscoRepository.save(formulario);
        return ResponseEntity.status(HttpStatus.CREATED).body(formulario);
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
        formulario.avaliarRisco();

        formularioRiscoRepository.save(formulario);
        return ResponseEntity.ok(formulario);
    }

    // ---------- Buscar formulário por usuário ----------
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> buscarPorUsuario(@PathVariable Long idUsuario) {
        Optional<FormularioRisco> formularioOpt = formularioRiscoRepository.findByUsuarioId(idUsuario);
        if (formularioOpt.isPresent()) {
            return ResponseEntity.ok(formularioOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhum formulário encontrado para este usuário.");
        }
    }

    // ---------- Listar todos os formulários ----------
    @GetMapping
    public ResponseEntity<List<FormularioRisco>> listarTodos() {
        return ResponseEntity.ok(formularioRiscoRepository.findAll());
    }
}
