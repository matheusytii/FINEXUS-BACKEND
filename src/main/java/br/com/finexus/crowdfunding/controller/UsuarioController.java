package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.Usuario;
import br.com.finexus.crowdfunding.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    //cadastra usuario
    @PostMapping
    public Usuario cadastrar(@RequestBody Usuario usuario) {
        //cripitografa antes de salva no banco
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha())); 
        //salva no banco e retonar o usuario salvo
        return usuarioRepository.save(usuario);
    }
    //lista todos usuario
    @GetMapping
    //retorna a lista completa de todos os usuarios
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
    //bucar usuario pelo id
    @GetMapping("/{id}")
    public  ResponseEntity<Usuario>buscarPorId(@PathVariable Long id){
        //busca pelo id no banco
        Optional<Usuario> usuario = usuarioRepository.findById(id);

        //se existir o usuario restorna 200 + usuario 
        if(usuario.isPresent()){
            return ResponseEntity.ok(usuario.get());
        }
        //se não existir erro 400
        else{
            return ResponseEntity.notFound().build();
        }
    }
    
}
