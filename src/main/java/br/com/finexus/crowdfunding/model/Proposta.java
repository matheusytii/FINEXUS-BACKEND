package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propostas")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valorSolicitado;
    private Double taxaJuros;
    private Integer prazoMeses;
    private Double valorTotal;
    private String perfilRisco;
    private LocalDate dataAbertura;
    private LocalDate dataAprovacao;

    @Enumerated(EnumType.STRING)
    private StatusProposta status;

    @ManyToOne
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    private Double valorRestante;

    // 🔹 Informações do negócio do MEI
    private String ramoNegocio;            // Ex: "Alimentação", "Beleza", "Tecnologia"
    private String tamanhoNegocio;         // Ex: "Individual", "Microempresa", etc.
    private LocalDate dataAberturaNegocio; // Quando o negócio foi aberto
    private String cnpj;                   // CNPJ do MEI
    @Column(length = 1000)
    private String motivacaoAbertura;      // Por que abriu o negócio (história pessoal ou oportunidade)

    // 🔹 Motivo do empréstimo
    @Column(length = 1500)
    private String motivoEmprestimo;       // Ex: "Comprar novos equipamentos", "Reformar o ponto", etc.

    @OneToMany(mappedBy = "proposta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Investimento> investimentos = new ArrayList<>();

    public Proposta() {
        this.dataAbertura = LocalDate.now();
        this.status = StatusProposta.ABERTA;
    }

    // 🔹 Métodos auxiliares
    public void adicionarInvestimento(Investimento investimento) {
        this.investimentos.add(investimento);
        investimento.setProposta(this);
        atualizarValorRestante();
    }

    public void removerInvestimento(Investimento investimento) {
        this.investimentos.remove(investimento);
        investimento.setProposta(null);
        atualizarValorRestante();
    }

    public void atualizarValorRestante() {
        double totalInvestido = investimentos.stream()
                .mapToDouble(Investimento::getValorInvestido)
                .sum();
        this.valorRestante = Math.max(0, valorSolicitado - totalInvestido);
    }

    // 🔹 Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getValorSolicitado() {
        return valorSolicitado;
    }

    public void setValorSolicitado(Double valorSolicitado) {
        this.valorSolicitado = valorSolicitado;
        atualizarValorRestante();
    }

    public Double getTaxaJuros() {
        return taxaJuros;
    }

    public void setTaxaJuros(Double taxaJuros) {
        this.taxaJuros = taxaJuros;
    }

    public Integer getPrazoMeses() {
        return prazoMeses;
    }

    public void setPrazoMeses(Integer prazoMeses) {
        this.prazoMeses = prazoMeses;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getPerfilRisco() {
        return perfilRisco;
    }

    public void setPerfilRisco(String perfilRisco) {
        this.perfilRisco = perfilRisco;
    }

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDate dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDate getDataAprovacao() {
        return dataAprovacao;
    }

    public void setDataAprovacao(LocalDate dataAprovacao) {
        this.dataAprovacao = dataAprovacao;
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

    public Double getValorRestante() {
        return valorRestante;
    }

    public void setValorRestante(Double valorRestante) {
        this.valorRestante = valorRestante;
    }

    public List<Investimento> getInvestimentos() {
        return investimentos;
    }

    public void setInvestimentos(List<Investimento> investimentos) {
        this.investimentos = investimentos;
        atualizarValorRestante();
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
}
