package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;
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

    // ---------- CAMPOS DO NEGÓCIO ----------
    private String nomeNegocio;
    private String categoria;
    private String descricaoNegocio;
    private String cnpj;
    private Integer tempoAtuacaoMeses;
    private Double faturamentoMensal;

    // ---------- CAMPOS DO EMPRÉSTIMO ----------
    private Double valorSolicitado;
    private Double saldoInvestido = 0.0;
    private Integer prazoMeses;

    private String motivoEmprestimo;
    private String descricaoUsoRecurso;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private StatusProposta status = StatusProposta.ABERTA;

    // ---------- RESULTADOS DO CÁLCULO ----------
    private Integer riscoCalculado;
    private Double taxaJuros;
    private Double valorTotalPagar;

    // ---------- RELACIONAMENTOS ----------
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference("usuario-proposta")
    private Usuario solicitante;

    @OneToMany(mappedBy = "proposta")
    @JsonIgnore
    private List<Investimento> investimentos = new ArrayList<>();

    // =============================================================
    // GETTERS E SETTERS
    // =============================================================

    public Long getId() {
        return id;
    }

    public String getNomeNegocio() {
        return nomeNegocio;
    }

    public void setNomeNegocio(String nomeNegocio) {
        this.nomeNegocio = nomeNegocio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricaoNegocio() {
        return descricaoNegocio;
    }

    public void setDescricaoNegocio(String descricaoNegocio) {
        this.descricaoNegocio = descricaoNegocio;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Integer getTempoAtuacaoMeses() {
        return tempoAtuacaoMeses;
    }

    public void setTempoAtuacaoMeses(Integer tempoAtuacaoMeses) {
        this.tempoAtuacaoMeses = tempoAtuacaoMeses;
    }

    public Double getFaturamentoMensal() {
        return faturamentoMensal;
    }

    public void setFaturamentoMensal(Double faturamentoMensal) {
        this.faturamentoMensal = faturamentoMensal;
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

    public String getMotivoEmprestimo() {
        return motivoEmprestimo;
    }

    public void setMotivoEmprestimo(String motivoEmprestimo) {
        this.motivoEmprestimo = motivoEmprestimo;
    }

    public String getDescricaoUsoRecurso() {
        return descricaoUsoRecurso;
    }

    public void setDescricaoUsoRecurso(String descricaoUsoRecurso) {
        this.descricaoUsoRecurso = descricaoUsoRecurso;
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

    public Integer getRiscoCalculado() {
        return riscoCalculado;
    }

    public void setRiscoCalculado(Integer riscoCalculado) {
        this.riscoCalculado = riscoCalculado;
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

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public List<Investimento> getInvestimentos() {
        return investimentos;
    }

    public void setInvestimentos(List<Investimento> investimentos) {
        this.investimentos = investimentos;
    }

    // =============================================================
    // LÓGICAS DO SISTEMA
    // =============================================================

    /** Calcula risco, juros e total baseado nas novas regras */
    public void calcularRiscoEJuros() {

        double risco = 0;

        // Relação valor solicitado / faturamento
        double relacao = valorSolicitado / faturamentoMensal;

        if (relacao < 0.5)
            risco += 10;
        else if (relacao < 1.0)
            risco += 25;
        else if (relacao < 2.0)
            risco += 45;
        else
            risco += 70;

        // Tempo de atuação
        if (tempoAtuacaoMeses >= 36)
            risco -= 15;
        else if (tempoAtuacaoMeses >= 12)
            risco -= 5;
        else
            risco += 10;

        risco = Math.min(100, Math.max(0, risco));
        this.riscoCalculado = (int) risco;

        // Taxa de juros baseada no risco
        double jurosBase;
        if (risco <= 25)
            jurosBase = 2.5; // Baixo risco
        else if (risco <= 50)
            jurosBase = 3.8; // Risco moderado
        else if (risco <= 75)
            jurosBase = 5.2; // Risco alto
        else
            jurosBase = 7.5;
        double jurosFinal = jurosBase + (prazoMeses * 0.02);

        this.taxaJuros = jurosFinal;

        // Valor total a pagar (juros compostos)
        this.valorTotalPagar = valorSolicitado * Math.pow(1 + (jurosFinal / 100), prazoMeses);
    }

    /** Registra investimento e atualiza status */
    public void adicionarInvestimento(Double valor) {
        this.saldoInvestido += valor;

        if (this.saldoInvestido >= this.valorSolicitado) {
            this.status = StatusProposta.FINANCIADA;
        }
    }
}
