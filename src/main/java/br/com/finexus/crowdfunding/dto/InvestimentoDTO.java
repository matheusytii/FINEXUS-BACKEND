package br.com.finexus.crowdfunding.dto;

import java.time.LocalDate;

public class InvestimentoDTO {

    private Long id;

    private Double valorInvestido;
    private LocalDate dataInvestimento;
    private Double rendimentoEsperado;
    private String status;
    private String qrCodeUrl;

    // ---- IDs ----
    private Long investidorId;
    private Long propostaId;
    private Long tomadorId;

    // ---- Informações de Proposta ----
    private String nomeNegocio;
    private String cnpj;
    private Integer prazoMeses;
    private Double valorSolicitado;
    private Double saldoInvestido;
    private Double taxaJuros;
    private String statusProposta;

    // ---- Nomes ----
    private String nomeTomador;
    private String nomeInvestidor;

    public InvestimentoDTO() {}

    public InvestimentoDTO(
            Long id,
            Double valorInvestido,
            LocalDate dataInvestimento,
            Double rendimentoEsperado,
            String status,
            String qrCodeUrl,
            Long investidorId,
            Long propostaId,
            Long tomadorId,
            String nomeNegocio,
            String cnpj,
            Integer prazoMeses,
            Double valorSolicitado,
            Double saldoInvestido,
            Double taxaJuros,
            String statusProposta,
            String nomeTomador,
            String nomeInvestidor
    ) {
        this.id = id;
        this.valorInvestido = valorInvestido;
        this.dataInvestimento = dataInvestimento;
        this.rendimentoEsperado = rendimentoEsperado;
        this.status = status;
        this.qrCodeUrl = qrCodeUrl;
        this.investidorId = investidorId;
        this.propostaId = propostaId;
        this.tomadorId = tomadorId;
        this.nomeNegocio = nomeNegocio;
        this.cnpj = cnpj;
        this.prazoMeses = prazoMeses;
        this.valorSolicitado = valorSolicitado;
        this.saldoInvestido = saldoInvestido;
        this.taxaJuros = taxaJuros;
        this.statusProposta = statusProposta;
        this.nomeTomador = nomeTomador;
        this.nomeInvestidor = nomeInvestidor;
    }

    // ============================
    // Getters e Setters
    // ============================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getValorInvestido() { return valorInvestido; }
    public void setValorInvestido(Double valorInvestido) { this.valorInvestido = valorInvestido; }

    public LocalDate getDataInvestimento() { return dataInvestimento; }
    public void setDataInvestimento(LocalDate dataInvestimento) { this.dataInvestimento = dataInvestimento; }

    public Double getRendimentoEsperado() { return rendimentoEsperado; }
    public void setRendimentoEsperado(Double rendimentoEsperado) { this.rendimentoEsperado = rendimentoEsperado; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public Long getInvestidorId() { return investidorId; }
    public void setInvestidorId(Long investidorId) { this.investidorId = investidorId; }

    public Long getPropostaId() { return propostaId; }
    public void setPropostaId(Long propostaId) { this.propostaId = propostaId; }

    public Long getTomadorId() { return tomadorId; }
    public void setTomadorId(Long tomadorId) { this.tomadorId = tomadorId; }

    public String getNomeNegocio() { return nomeNegocio; }
    public void setNomeNegocio(String nomeNegocio) { this.nomeNegocio = nomeNegocio; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public Integer getPrazoMeses() { return prazoMeses; }
    public void setPrazoMeses(Integer prazoMeses) { this.prazoMeses = prazoMeses; }

    public Double getValorSolicitado() { return valorSolicitado; }
    public void setValorSolicitado(Double valorSolicitado) { this.valorSolicitado = valorSolicitado; }

    public Double getSaldoInvestido() { return saldoInvestido; }
    public void setSaldoInvestido(Double saldoInvestido) { this.saldoInvestido = saldoInvestido; }

    public Double getTaxaJuros() { return taxaJuros; }
    public void setTaxaJuros(Double taxaJuros) { this.taxaJuros = taxaJuros; }

    public String getStatusProposta() { return statusProposta; }
    public void setStatusProposta(String statusProposta) { this.statusProposta = statusProposta; }

    public String getNomeTomador() { return nomeTomador; }
    public void setNomeTomador(String nomeTomador) { this.nomeTomador = nomeTomador; }

    public String getNomeInvestidor() { return nomeInvestidor; }
    public void setNomeInvestidor(String nomeInvestidor) { this.nomeInvestidor = nomeInvestidor; }
}
