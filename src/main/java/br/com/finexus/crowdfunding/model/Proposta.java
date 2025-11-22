package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "propostas")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valorSolicitado;

    private Double saldoInvestido = 0.0;

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

    private Double taxaJuros;
    private Double valorTotalPagar;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference("usuario-proposta")
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private FormularioRisco formularioRisco;

    @OneToMany(mappedBy = "proposta")
    @JsonIgnore
    private List<Investimento> investimentos = new ArrayList<>();

    public List<Investimento> getInvestimentos() {
        return investimentos;
    }

    public void setInvestimentos(List<Investimento> investimentos) {
        this.investimentos = investimentos;
    }

    public Long getId() {
        return id;
    }

    public Double getValorSolicitado() {
        return valorSolicitado;
    }

    public void setValorSolicitado(Double valorSolicitado) {
        this.valorSolicitado = valorSolicitado;
    }

    public Double getSaldoInvestido() {
        return saldoInvestido;
    }

    public void setSaldoInvestido(Double saldoInvestido) {
        this.saldoInvestido = saldoInvestido;
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

    public void adicionarInvestimento(Double valor) {
        this.saldoInvestido += valor;

        if (this.saldoInvestido >= this.valorSolicitado) {
            this.status = StatusProposta.FINANCIADA;
        }
    }
}
