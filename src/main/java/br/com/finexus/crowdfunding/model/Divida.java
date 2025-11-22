package br.com.finexus.crowdfunding.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dividas")
public class Divida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "proposta_id", nullable = false)
    private Proposta proposta;

    @ManyToOne
    @JoinColumn(name = "tomador_id", nullable = false)
    private Usuario tomador;

    private Double valorTotal;
    private Integer parcelas;
    private Double valorParcela;

    @ElementCollection
    @CollectionTable(name = "divida_investidores", joinColumns = @JoinColumn(name = "divida_id"))
    @Column(name = "id_investidor")
    private List<Long> investidoresIds = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Proposta getProposta() {
        return proposta;
    }

    public void setProposta(Proposta proposta) {
        this.proposta = proposta;
    }

    public Usuario getTomador() {
        return tomador;
    }

    public void setTomador(Usuario tomador) {
        this.tomador = tomador;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public Double getValorParcela() {
        return valorParcela;
    }

    public void setValorParcela(Double valorParcela) {
        this.valorParcela = valorParcela;
    }

    public List<Long> getInvestidoresIds() {
        return investidoresIds;
    }

    public void setInvestidoresIds(List<Long> investidoresIds) {
        this.investidoresIds = investidoresIds;
    }
}
