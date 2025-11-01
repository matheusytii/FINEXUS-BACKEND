package br.com.finexus.crowdfunding.model;

public enum StatusProposta {
    ABERTA,        // Criada, ainda não analisada
    EM_ANALISE,    // Em avaliação de risco
    APROVADA,      // Aprovada e liberada para investimento
    FINANCIADA,    // Valor solicitado foi atingido
    EM_PAGAMENTO,  // Tomador está pagando parcelas
    FINALIZADA,    // Pagamento completo
    REJEITADA      // Não aprovada
}
