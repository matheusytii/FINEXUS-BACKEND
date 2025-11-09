package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "propostas")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valorSolicitado;
    private Integer prazoMeses;

    private String ramoNegocio;
    private String tamanhoNegocio;
    private LocalDate dataAberturaNegocio;
    private String cnpj;
    private String motivacaoAbertura;
    private String motivoEmprestimo;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private StatusProposta status = StatusProposta.ABERTA;

    // 💰 Campos novos
    private Double taxaJuros;       // Percentual anual baseado no perfil de risco
    private Double valorTotalPagar; // Valor final com juros aplicados

    // 📎 Relacionamento com Usuário (somente TOMADOR pode criar)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference("usuario-proposta")
    private Usuario solicitante;

    // 📎 Relacionamento com Formulário de Risco
    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private FormularioRisco formularioRisco;

    // ================== GETTERS E SETTERS ==================
    public Long getId() {
        return id;
    }

    public Double getValorSolicitado() {
        return valorSolicitado;
    }

    public void setValorSolicitado(Double valorSolicitado) {
        this.valorSolicitado = valorSolicitado;
    }

    public Integer getPrazoMeses() {
        return prazoMeses;
    }

    public void setPrazoMeses(Integer prazoMeses) {
        this.prazoMeses = prazoMeses;
    }

    public String getRamoNegocio() {
        return ramoNegocio;
    }

    public void setRamoNegocio(String ramoNegocio) {
        this.ramoNegocio = ramoNegocio;
    }

    public String getTamanhoNegocio() {
        return tamanhoNegocio;
    }

    public void setTamanhoNegocio(String tamanhoNegocio) {
        this.tamanhoNegocio = tamanhoNegocio;
    }

    public LocalDate getDataAberturaNegocio() {
        return dataAberturaNegocio;
    }

    public void setDataAberturaNegocio(LocalDate dataAberturaNegocio) {
        this.dataAberturaNegocio = dataAberturaNegocio;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getMotivacaoAbertura() {
        return motivacaoAbertura;
    }

    public void setMotivacaoAbertura(String motivacaoAbertura) {
        this.motivacaoAbertura = motivacaoAbertura;
    }

    public String getMotivoEmprestimo() {
        return motivoEmprestimo;
    }

    public void setMotivoEmprestimo(String motivoEmprestimo) {
        this.motivoEmprestimo = motivoEmprestimo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public StatusProposta getStatus() {
        return status;
    }

    public void setStatus(StatusProposta status) {
        this.status = status;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public FormularioRisco getFormularioRisco() {
        return formularioRisco;
    }

    public void setFormularioRisco(FormularioRisco formularioRisco) {
        this.formularioRisco = formularioRisco;
    }

    public Double getTaxaJuros() {
        return taxaJuros;
    }

    public void setTaxaJuros(Double taxaJuros) {
        this.taxaJuros = taxaJuros;
    }

    public Double getValorTotalPagar() {
        return valorTotalPagar;
    }

    public void setValorTotalPagar(Double valorTotalPagar) {
        this.valorTotalPagar = valorTotalPagar;
    }
}
