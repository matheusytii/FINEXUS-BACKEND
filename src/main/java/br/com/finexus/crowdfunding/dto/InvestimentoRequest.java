package br.com.finexus.crowdfunding.dto;

public class InvestimentoRequest {
    private Long idProposta;
    private Long idInvestidor;
    private Double valor;

    public Long getIdProposta() {
        return idProposta;
    }

    public void setIdProposta(Long idProposta) {
        this.idProposta = idProposta;
    }

    public Long getIdInvestidor() {
        return idInvestidor;
    }

    public void setIdInvestidor(Long idInvestidor) {
        this.idInvestidor = idInvestidor;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
