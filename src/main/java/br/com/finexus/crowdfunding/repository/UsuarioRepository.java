package br.com.finexus.crowdfunding.repository;

import br.com.finexus.crowdfunding.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; 

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    Optional<Usuario> findByCpf(String cpf);
    Usuario findByTelefone(String telefone);
}
