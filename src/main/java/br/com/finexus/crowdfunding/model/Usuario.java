package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank // indica que esse campo não pode ser nullo
    private String nome;

    @NotBlank
    @Email
    @Column(nullable =  false, unique = true) //indica que email tem que ser unico
    private String email;

    @NotBlank
    private  String senha;

    @Enumerated(EnumType.STRING) 
    private TipoUsuario tipo;


    // getters e setters
    public Long getId () {return id;}
    public void setId (Long id){this.id= id;}

    public  String getNome() {return nome;}
    public void SetNome (String nome) {this.nome = nome;}

    public String getEmail () {return email;}
    public void SetEmail(String email){ this.email= email;}

    public String getSenha (){return senha;}
    public void setSenha (String senha) { this.senha = senha;}

    public TipoUsuario getTipo(){return tipo;}
    public void SetTipo ( TipoUsuario tipo){ this.tipo = tipo;}



}
