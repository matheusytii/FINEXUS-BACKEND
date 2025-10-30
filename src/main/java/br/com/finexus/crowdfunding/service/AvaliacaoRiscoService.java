package br.com.finexus.crowdfunding.service;

import br.com.finexus.crowdfunding.model.FormularioRisco;
import org.springframework.stereotype.Service;

@Service
public class AvaliacaoRiscoService {

    public String calcularPerfilRisco(FormularioRisco form) {
        double score = 0.0;

        // Exemplo simples de lógica
        if (form.getRendaMensal() > 5000) score += 2;
        if (form.getDividasTotais() < 2000) score += 2;
        if (form.getTempoDeAtividadeMeses() > 12) score += 1;
        if (form.getPossuiGarantia()) score += 1;
        if (form.getHistoricoInadimplencia()) score -= 3;

        if (score >= 4) return "Baixo";
        if (score >= 2) return "Médio";
        return "Alto";
    }

    public double calcularTaxaJuros(String perfilRisco, int prazoMeses) {
        double base = switch (perfilRisco) {
            case "Baixo" -> 2.5;
            case "Médio" -> 5.5;
            default -> 9.0;
        };
        // Aumenta um pouco os juros conforme o prazo
        return base + (prazoMeses / 12.0);
    }
}
