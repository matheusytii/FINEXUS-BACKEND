package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.dto.LoginRequest;
import br.com.finexus.crowdfunding.model.TipoUsuario;
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
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, Object> body) {

        String senha = (String) body.get("senha");
        String confirmarSenha = (String) body.get("confirmarSenha");

        if (!senha.equals(confirmarSenha)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "As senhas não coincidem"));
        }

        String cpf = (String) body.get("cpf");

        // Validação do formato do CPF
        if (!cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            return ResponseEntity.badRequest().body(
                    Map.of("erro", "CPF inválido. Use o formato 000.000.000-00"));
        }

        // Verificar se email já existe
        if (usuarioRepository.findByEmail((String) body.get("email")) != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Email já cadastrado"));
        }

        // 👉 Agora funcionando corretamente com Optional
        if (usuarioRepository.findByCpf(cpf).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "CPF já cadastrado"));
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome((String) body.get("nome"));
        novoUsuario.setEmail((String) body.get("email"));
        novoUsuario.setCpf(cpf);
        novoUsuario.setTelefone((String) body.get("telefone"));
        novoUsuario.setTipo(TipoUsuario.valueOf((String) body.get("tipo")));
        novoUsuario.setSenha(passwordEncoder.encode(senha));

        Usuario salvo = usuarioRepository.save(novoUsuario);

        String token = jwtUtil.gerarToken(salvo.getCpf(), salvo.getTipo().name());

        return ResponseEntity.ok(
                Map.of(
                        "mensagem", "Usuário cadastrado com sucesso!",
                        "token", token,
                        "tipo", salvo.getTipo(),
                        "id", salvo.getId()));
    }

    // LOGIN DO USUÁRIO (por CPF)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dadosLogin) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(dadosLogin.getCpf());
        Map<String, Object> resposta = new HashMap<>();

        if (usuarioOpt.isEmpty()) {
            resposta.put("erro", "CPF ou senha inválidos");
            return ResponseEntity.status(401).body(resposta);
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(dadosLogin.getSenha(), usuario.getSenha())) {
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

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<?> buscarPorCpf(@PathVariable String cpf) {
        return usuarioRepository.findByCpf(cpf)
                .<ResponseEntity<?>>map(usuario -> ResponseEntity.ok(usuario))
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("erro", "Usuário não encontrado")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {

                    if (body.containsKey("email")) {
                        usuarioExistente.setEmail((String) body.get("email"));
                    }

                    if (body.containsKey("cpf")) {
                        usuarioExistente.setCpf((String) body.get("cpf"));
                    }

                    if (body.containsKey("telefone")) {
                        usuarioExistente.setTelefone((String) body.get("telefone"));
                    }

                    if (body.containsKey("senha")) {
                        String novaSenha = (String) body.get("senha");
                        String confirmarSenha = (String) body.get("confirmarSenha");

                        if (confirmarSenha == null) {
                            return ResponseEntity.badRequest().body(
                                    Map.of("erro", "É necessário confirmar a senha."));
                        }

                        if (!novaSenha.equals(confirmarSenha)) {
                            return ResponseEntity.badRequest().body(
                                    Map.of("erro", "As senhas não coincidem."));
                        }

                        usuarioExistente.setSenha(passwordEncoder.encode(novaSenha));
                    }

                    // 🔥 ATUALIZAÇÃO DOS CAMPOS NOVOS

                    if (body.containsKey("inadimplente")) {
                        usuarioExistente.setInadimplente((Boolean) body.get("inadimplente"));
                    }

                    if (body.containsKey("historicoInadimplencia")) {
                        usuarioExistente.setHistoricoInadimplencia((Integer) body.get("historicoInadimplencia"));
                    }

                    if (body.containsKey("riscoAlto")) {
                        usuarioExistente.setRiscoAlto((Boolean) body.get("riscoAlto"));
                    }

                    usuarioRepository.save(usuarioExistente);

                    return ResponseEntity.ok(Map.of(
                            "mensagem", "Usuário atualizado com sucesso!",
                            "usuario", usuarioExistente));
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
