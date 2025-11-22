package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcelas_divida")
public class ParcelaDivida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "divida_id")
    private Divida divida;

    private Integer numeroParcela;
    private Double valor;
    private LocalDate vencimento;

    @Enumerated(EnumType.STRING)
    private StatusParcela status = StatusParcela.PENDENTE;

    private LocalDateTime dataAberturaBoleto;

    private LocalDate dataPagamento;

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Divida getDivida() { return divida; }
    public void setDivida(Divida divida) { this.divida = divida; }

    public Integer getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(Integer numeroParcela) { this.numeroParcela = numeroParcela; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public LocalDate getVencimento() { return vencimento; }
    public void setVencimento(LocalDate vencimento) { this.vencimento = vencimento; }

    public StatusParcela getStatus() { return status; }
    public void setStatus(StatusParcela status) { this.status = status; }

    public LocalDateTime getDataAberturaBoleto() { return dataAberturaBoleto; }
    public void setDataAberturaBoleto(LocalDateTime dataAberturaBoleto) { this.dataAberturaBoleto = dataAberturaBoleto; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
}
