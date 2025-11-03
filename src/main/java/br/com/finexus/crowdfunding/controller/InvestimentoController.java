package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.*;
import br.com.finexus.crowdfunding.repository.*;
import br.com.finexus.crowdfunding.dto.InvestimentoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/investimentos")
@CrossOrigin(origins = "*")
public class InvestimentoController {

    @Autowired
    private InvestimentoRepository investimentoRepository;

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 📌 Criar investimento (gera boleto simulado)
  @PostMapping
public ResponseEntity<?> investir(@RequestBody InvestimentoRequest request) {
    Optional<Proposta> propostaOpt = propostaRepository.findById(request.getIdProposta());
    Optional<Usuario> investidorOpt = usuarioRepository.findById(request.getIdInvestidor());

    if (propostaOpt.isEmpty())
        return ResponseEntity.badRequest().body("Proposta não encontrada.");
    if (investidorOpt.isEmpty())
        return ResponseEntity.badRequest().body("Investidor não encontrado.");

    Proposta proposta = propostaOpt.get();
    Usuario investidor = investidorOpt.get();

    if (proposta.getStatus() != StatusProposta.ABERTA)
        return ResponseEntity.badRequest().body("A proposta não está aberta para investimento.");

    Double valor = request.getValor();

  
    if (valor == null || valor.isNaN() || valor <= 0) {
        return ResponseEntity.badRequest().body("O valor do investimento deve ser positivo e maior que zero.");
    }

    double totalInvestido = proposta.getInvestimentos() == null
            ? 0.0
            : proposta.getInvestimentos().stream()
                    .mapToDouble(Investimento::getValorInvestido)
                    .sum();

    double restante = proposta.getValorSolicitado() - totalInvestido;

    if (restante <= 0) {
        return ResponseEntity.badRequest().body("A proposta já atingiu o valor total solicitado.");
    }

    if (valor > restante) {
        return ResponseEntity.badRequest().body(
                String.format("O valor máximo disponível para investimento é de R$ %.2f", restante));
    }

    Investimento investimento = new Investimento();
    investimento.setInvestidor(investidor);
    investimento.setProposta(proposta);
    investimento.setValorInvestido(valor);
    investimento.setStatus(StatusInvestimento.PENDENTE);

    // Gera QR Code fictício
    String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?data=BOLETO-" + UUID.randomUUID();
    investimento.setQrCodeUrl(qrCodeUrl);

    double rendimento = valor * (1 + (proposta.getTaxaJuros() / 100));
    investimento.setRendimentoEsperado(rendimento);

    investimentoRepository.save(investimento);

    return ResponseEntity.ok(investimento);
}

    // 📌 Confirmar pagamento (simula boleto pago)
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarPagamento(@PathVariable Long id) {
        Optional<Investimento> investimentoOpt = investimentoRepository.findById(id);
        if (investimentoOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Investimento investimento = investimentoOpt.get();
        if (investimento.getStatus() != StatusInvestimento.PENDENTE)
            return ResponseEntity.badRequest().body("Esse investimento já foi confirmado ou cancelado.");

        investimento.setStatus(StatusInvestimento.CONFIRMADO);
        investimentoRepository.save(investimento);

        // Atualiza valor total da proposta
        Proposta proposta = investimento.getProposta();
        double totalInvestido = proposta.getInvestimentos().stream()
                .filter(inv -> inv.getStatus() == StatusInvestimento.CONFIRMADO)
                .mapToDouble(Investimento::getValorInvestido)
                .sum();

        if (totalInvestido >= proposta.getValorSolicitado()) {
            proposta.setStatus(StatusProposta.FINANCIADA);
        }

        propostaRepository.save(proposta);

        return ResponseEntity.ok("Pagamento confirmado e investimento liberado.");
    }

    @GetMapping("/{id}/boleto")
    public ResponseEntity<byte[]> gerarBoleto(@PathVariable Long id) throws Exception {
        Optional<Investimento> invOpt = investimentoRepository.findById(id);
        if (invOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Investimento investimento = invOpt.get();
        Proposta proposta = investimento.getProposta();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 80);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();

        String bgPath = "src/main/resources/static/images/favicon.png.ico";
        String headerLogoPath = "src/main/resources/static/images/logo-finexus-df.png";

        PdfContentByte canvas = writer.getDirectContentUnder();

        // 🔹 Fundo com opacidade
        try {
            Image background = Image.getInstance(bgPath);
            background.scaleToFit(PageSize.A4.getWidth() * 0.6f, PageSize.A4.getHeight() * 0.6f);
            background.setAbsolutePosition(
                    (PageSize.A4.getWidth() - background.getScaledWidth()) / 2,
                    (PageSize.A4.getHeight() - background.getScaledHeight()) / 2);

            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.2f);
            canvas.saveState();
            canvas.setGState(gs);
            canvas.addImage(background);
            canvas.restoreState();
        } catch (Exception e) {
            System.err.println("Erro ao aplicar background: " + e.getMessage());
        }

        // 🔤 Fontes
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(102, 51, 153));
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(102, 51, 153));
        Font textFont = new Font(Font.FontFamily.HELVETICA, 12);

        // 🔹 Cabeçalho: logo + título centralizado
        try {
            Image logo = Image.getInstance(headerLogoPath);
            logo.scaleToFit(100, 100);

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[] { 1f, 2f, 1f });
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);

            PdfPCell titleCell = new PdfPCell(new Phrase("Boleto Simulado", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(titleCell);

            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(empty);

            document.add(headerTable);
        } catch (Exception e) {
            Paragraph titulo = new Paragraph("Boleto Simulado", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
        }

        document.add(new Paragraph("\n"));
        LineSeparator ls = new LineSeparator();
        document.add(ls);
        document.add(new Paragraph("\n"));

        // 🔹 Informações principais
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        table.addCell(new Phrase("Investidor:", labelFont));
        table.addCell(new Phrase(investimento.getInvestidor().getNome(), textFont));

        table.addCell(new Phrase("Proposta:", labelFont));
        table.addCell(new Phrase(proposta.getSolicitante().getNome(), textFont));

        table.addCell(new Phrase("Valor Investido:", labelFont));
        table.addCell(new Phrase(String.format("R$ %.2f", investimento.getValorInvestido()), textFont));

        table.addCell(new Phrase("Rendimento Esperado:", labelFont));
        table.addCell(new Phrase(String.format("R$ %.2f", investimento.getRendimentoEsperado()), textFont));

        document.add(table);

        // 🔹 Posicionamento do QR e rodapé manualmente (usando coordenadas absolutas)
        PdfContentByte cb = writer.getDirectContent();

        try {
            String qrData = investimento.getQrCodeUrl();
            Image qrImage;

            if (qrData != null && (qrData.startsWith("http://") || qrData.startsWith("https://"))) {
                qrImage = Image.getInstance(new URL(qrData));
            } else if (qrData != null) {
                qrImage = Image.getInstance(qrData);
            } else {
                qrImage = null;
            }

            if (qrImage != null) {
                qrImage.scaleToFit(150, 150);

                // 🔲 Moldura arredondada preta
                float qrW = 170;
                float qrH = 170;
                float qrX = (PageSize.A4.getWidth() - qrW) / 2;
                float qrY = 220; // sobe ou desce o bloco (ajustável)

                cb.saveState();
                cb.setLineWidth(1.5f);
                cb.setColorStroke(BaseColor.BLACK);
                cb.roundRectangle(qrX, qrY, qrW, qrH, 12);
                cb.stroke();
                cb.restoreState();

                // 🔹 QR centralizado dentro do quadro
                qrImage.setAbsolutePosition(
                        (PageSize.A4.getWidth() - qrImage.getScaledWidth()) / 2,
                        qrY + 10);
                document.add(qrImage);

                // 🔹 Texto logo ABAIXO do QR code
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        new Phrase("Escaneie o QR Code acima para Realizar o Pagamento via Pix.", textFont),
                        PageSize.A4.getWidth() / 2,
                        qrY - 30, // posição do texto abaixo do QR
                        0);

                // 🔹 Linha + rodapé (mais próximos do bloco do QR, mas com um leve gap)
                float footerLineY = qrY - 55; // linha logo abaixo do texto
                float footerTextY = footerLineY - 15; // texto abaixo da linha

                cb.moveTo(50, footerLineY);
                cb.lineTo(PageSize.A4.getWidth() - 50, footerLineY);
                cb.stroke();

                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        new Phrase("Finexus — Plataforma de Crédito Colaborativo",
                                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, new BaseColor(120, 120, 120))),
                        PageSize.A4.getWidth() / 2,
                        footerTextY,
                        0);
            }

        } catch (Exception e) {
            System.err.println("Erro ao gerar QR Code: " + e.getMessage());
        }

        document.close();

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=boleto-finexus.pdf")
                .body(outputStream.toByteArray());
    }

    // 📌 Consultar por proposta
    @GetMapping("/proposta/{idProposta}")
    public ResponseEntity<?> listarPorProposta(@PathVariable Long idProposta) {
        Optional<Proposta> proposta = propostaRepository.findById(idProposta);
        if (proposta.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(investimentoRepository.findByProposta(proposta.get()));
    }

    // 📌 Consultar por investidor
    @GetMapping("/investidor/{idInvestidor}")
    public ResponseEntity<?> listarPorInvestidor(@PathVariable Long idInvestidor) {
        Optional<Usuario> investidor = usuarioRepository.findById(idInvestidor);
        if (investidor.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(investimentoRepository.findByInvestidor(investidor.get()));
    }
}
