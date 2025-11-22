package br.com.finexus.crowdfunding.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "investimentos")
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valorInvestido;

    private LocalDate dataInvestimento;

    private Double rendimentoEsperado;

    @Enumerated(EnumType.STRING)
    private StatusInvestimento status;

    private String qrCodeUrl;

    @ManyToOne
    @JoinColumn(name = "investidor_id", nullable = false)
    @JsonBackReference 
    private Usuario investidor;

    @ManyToOne
    @JoinColumn(name = "proposta_id", nullable = false)
    @JsonBackReference 
    private Proposta proposta;

    public Investimento() {
        this.dataInvestimento = LocalDate.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getValorInvestido() { return valorInvestido; }
    public void setValorInvestido(Double valorInvestido) { this.valorInvestido = valorInvestido; }

    public LocalDate getDataInvestimento() { return dataInvestimento; }
    public void setDataInvestimento(LocalDate dataInvestimento) { this.dataInvestimento = dataInvestimento; }

    public Double getRendimentoEsperado() { return rendimentoEsperado; }
    public void setRendimentoEsperado(Double rendimentoEsperado) { this.rendimentoEsperado = rendimentoEsperado; }

    public StatusInvestimento getStatus() { return status; }
    public void setStatus(StatusInvestimento status) { this.status = status; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public Usuario getInvestidor() { return investidor; }
    public void setInvestidor(Usuario investidor) { this.investidor = investidor; }

    public Proposta getProposta() { return proposta; }
    public void setProposta(Proposta proposta) { this.proposta = proposta; }
}