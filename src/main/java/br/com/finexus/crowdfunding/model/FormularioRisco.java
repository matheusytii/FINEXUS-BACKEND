package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "formularios_risco")
public class FormularioRisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double rendaMensal;
    private Double dividasTotais;
    private Integer tempoDeAtividadeMeses;
    private Boolean possuiGarantia;
    private Boolean historicoInadimplencia;

    private Double pontuacaoRisco; // 0–100
    private String perfilRisco; // "Baixo", "Médio" ou "Alto"

    private LocalDate dataEnvio;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // ---------- CONSTRUTOR ----------
    public FormularioRisco() {
        this.dataEnvio = LocalDate.now();
    }

    // ---------- MÉTODO AUTOMÁTICO DE AVALIAÇÃO ----------
    @PrePersist
    @PreUpdate
    public void avaliarRisco() {
        double score = 50.0; // base neutra

        // 📊 Renda maior = menor risco
        if (rendaMensal != null) {
            if (rendaMensal >= 10000) score += 20;
            else if (rendaMensal >= 5000) score += 10;
            else if (rendaMensal < 2000) score -= 15;
        }

        // 💸 Dívidas altas = maior risco
        if (dividasTotais != null && rendaMensal != null) {
            double relacaoDividaRenda = dividasTotais / rendaMensal;
            if (relacaoDividaRenda > 0.7) score -= 20;
            else if (relacaoDividaRenda > 0.4) score -= 10;
            else score += 5;
        }

        // ⏳ Tempo de atividade ajuda
        if (tempoDeAtividadeMeses != null) {
            if (tempoDeAtividadeMeses > 24) score += 15;
            else if (tempoDeAtividadeMeses > 12) score += 5;
            else score -= 5;
        }

        // 🏦 Garantias ajudam
        if (Boolean.TRUE.equals(possuiGarantia)) score += 10;

        // ⚠️ Histórico de inadimplência prejudica
        if (Boolean.TRUE.equals(historicoInadimplencia)) score -= 25;

        // Normaliza o score entre 0 e 100
        this.pontuacaoRisco = Math.max(0, Math.min(100, score));

        // Define o perfil automaticamente
        if (pontuacaoRisco >= 70) this.perfilRisco = "Baixo";
        else if (pontuacaoRisco >= 40) this.perfilRisco = "Médio";
        else this.perfilRisco = "Alto";
    }

    // ---------- GETTERS E SETTERS ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getRendaMensal() { return rendaMensal; }
    public void setRendaMensal(Double rendaMensal) { this.rendaMensal = rendaMensal; }

    public Double getDividasTotais() { return dividasTotais; }
    public void setDividasTotais(Double dividasTotais) { this.dividasTotais = dividasTotais; }

    public Integer getTempoDeAtividadeMeses() { return tempoDeAtividadeMeses; }
    public void setTempoDeAtividadeMeses(Integer tempoDeAtividadeMeses) { this.tempoDeAtividadeMeses = tempoDeAtividadeMeses; }

    public Boolean getPossuiGarantia() { return possuiGarantia; }
    public void setPossuiGarantia(Boolean possuiGarantia) { this.possuiGarantia = possuiGarantia; }

    public Boolean getHistoricoInadimplencia() { return historicoInadimplencia; }
    public void setHistoricoInadimplencia(Boolean historicoInadimplencia) { this.historicoInadimplencia = historicoInadimplencia; }

    public Double getPontuacaoRisco() { return pontuacaoRisco; }
    public String getPerfilRisco() { return perfilRisco; }

    public LocalDate getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDate dataEnvio) { this.dataEnvio = dataEnvio; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
