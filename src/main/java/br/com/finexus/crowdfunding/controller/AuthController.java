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
//prefixar a rota como auth
@RequestMapping("/auth")
public class AuthController {

    //injeção automatica do repository usuários
    @Autowired
    private UsuarioRepository usuarioRepository;

    //injeção responsável por gera tokens JWT
    @Autowired
    private JwtUtil jwtUtil;

    // Instasia por verificar a senha cripitografada
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // faz o login e retorna o token JWT
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Usuario loginRequest) {
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        Map<String, Object> response = new HashMap<>();

        if (usuario != null && passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha())) {
            String token = jwtUtil.gerarToken(usuario.getEmail());
            response.put("token", token);
            response.put("tipo", usuario.getTipo());
            return response;
        } else {
            response.put("erro", "Email ou senha inválidos");
            return response;
        }
    }
}
