package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.Usuario;
import br.com.finexus.crowdfunding.repository.UsuarioRepository;
import br.com.finexus.crowdfunding.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/autenticacao") // rota principal
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 🧩 Cadastro de novo usuário (público)
    @PostMapping("/cadastro")
    public Map<String, Object> cadastrar(@RequestBody Usuario novoUsuario) {
        Map<String, Object> resposta = new HashMap<>();

        if (usuarioRepository.findByEmail(novoUsuario.getEmail()) != null) {
            resposta.put("erro", "Email já cadastrado");
            return resposta;
        }

        // Criptografa a senha
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));

        // Salva no banco
        Usuario salvo = usuarioRepository.save(novoUsuario);

        // ⚙️ Gera token com o tipo de usuário incluso
        String token = jwtUtil.gerarToken(salvo.getEmail(), salvo.getTipo().name());

        resposta.put("mensagem", "Usuário cadastrado com sucesso!");
        resposta.put("token", token);
        resposta.put("tipo", salvo.getTipo());
        resposta.put("id", salvo.getId());

        return resposta;
    }

    // 🔐 Login do usuário
    @PostMapping("/entrar")
    public Map<String, Object> entrar(@RequestBody Usuario dadosLogin) {
        Usuario usuario = usuarioRepository.findByEmail(dadosLogin.getEmail());
        Map<String, Object> resposta = new HashMap<>();

        if (usuario != null && passwordEncoder.matches(dadosLogin.getSenha(), usuario.getSenha())) {

            // ⚙️ Gera token com role
            String token = jwtUtil.gerarToken(usuario.getEmail(), usuario.getTipo().name());

            resposta.put("token", token);
            resposta.put("tipo", usuario.getTipo());
            resposta.put("id", usuario.getId());
            return resposta;
        } else {
            resposta.put("erro", "Email ou senha inválidos");
            return resposta;
        }
    }
}
