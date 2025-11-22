package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.dto.LoginRequest;
import br.com.finexus.crowdfunding.model.Usuario;
import br.com.finexus.crowdfunding.repository.UsuarioRepository;
import br.com.finexus.crowdfunding.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // CADASTRO DE USUÁRIO (público)
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody Usuario novoUsuario) {
        if (usuarioRepository.findByEmail(novoUsuario.getEmail()) != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Email já cadastrado"));
        }

        if (usuarioRepository.findByCpf(novoUsuario.getCpf()) != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "CPF já cadastrado"));
        }

        // Criptografa a senha antes de salvar
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));

        Usuario salvo = usuarioRepository.save(novoUsuario);

        // Gera token JWT com CPF e tipo de usuário
        String token = jwtUtil.gerarToken(salvo.getCpf(), salvo.getTipo().name());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("mensagem", "Usuário cadastrado com sucesso!");
        resposta.put("token", token);
        resposta.put("tipo", salvo.getTipo());
        resposta.put("id", salvo.getId());

        return ResponseEntity.ok(resposta);
    }

    //  LOGIN DO USUÁRIO (por CPF)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dadosLogin) {

    Usuario usuario = usuarioRepository.findByCpf(dadosLogin.getCpf());
    Map<String, Object> resposta = new HashMap<>();

    if (usuario == null || !passwordEncoder.matches(dadosLogin.getSenha(), usuario.getSenha())) {
        resposta.put("erro", "CPF ou senha inválidos");
        return ResponseEntity.status(401).body(resposta);
    }

    String token = jwtUtil.gerarToken(usuario.getCpf(), usuario.getTipo().name());

    resposta.put("token", token);
    resposta.put("tipo", usuario.getTipo());
    resposta.put("id", usuario.getId());

    return ResponseEntity.ok(resposta);
    }


    // LISTAR TODOS OS USUÁRIOS (rota protegida)
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // 🔍 BUSCAR USUÁRIO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ATUALIZAR DADOS DO USUÁRIO
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Usuario dadosAtualizados) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    if (dadosAtualizados.getNome() != null) usuarioExistente.setNome(dadosAtualizados.getNome());
                    if (dadosAtualizados.getEmail() != null) usuarioExistente.setEmail(dadosAtualizados.getEmail());
                    if (dadosAtualizados.getCpf() != null) usuarioExistente.setCpf(dadosAtualizados.getCpf());
                    if (dadosAtualizados.getTipo() != null) usuarioExistente.setTipo(dadosAtualizados.getTipo());

                    if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
                        usuarioExistente.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
                    }

                    usuarioRepository.save(usuarioExistente);
                    return ResponseEntity.ok(Map.of(
                            "mensagem", "Usuário atualizado com sucesso!",
                            "usuario", usuarioExistente
                    ));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("erro", "Usuário não encontrado")));
    }

    // DELETAR CONTA DO USUÁRIO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return ResponseEntity.ok(Map.of("mensagem", "Usuário deletado com sucesso!"));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("erro", "Usuário não encontrado")));
    }
}
