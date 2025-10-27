package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Você pode adicionar consultas personalizadas aqui depois
    Usuario findByEmail(String email);
}
