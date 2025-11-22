package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;

@Entity
@Table(name = "saldos")
public class Saldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private Double valor = 0.0;

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
}
