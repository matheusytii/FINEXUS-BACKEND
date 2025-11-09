package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.*;
import br.com.finexus.crowdfunding.repository.*;
import br.com.finexus.crowdfunding.dto.InvestimentoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;
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

    // 🔹 Criar investimento
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
        if (valor == null || valor.isNaN() || valor <= 0)
            return ResponseEntity.badRequest().body("O valor do investimento deve ser positivo e maior que zero.");

        // 🔸 Calcular total já investido
        List<Investimento> investimentosDaProposta = investimentoRepository.findByProposta(proposta);
        double totalInvestido = investimentosDaProposta.stream()
                .filter(inv -> inv.getStatus() == StatusInvestimento.CONFIRMADO)
                .mapToDouble(Investimento::getValorInvestido)
                .sum();

        double restante = proposta.getValorSolicitado() - totalInvestido;
        if (restante <= 0)
            return ResponseEntity.badRequest().body("A proposta já atingiu o valor total solicitado.");

        if (valor > restante)
            return ResponseEntity.badRequest().body(String.format("O valor máximo disponível é R$ %.2f", restante));

        // 🔸 Criar investimento
        Investimento investimento = new Investimento();
        investimento.setInvestidor(investidor);
        investimento.setProposta(proposta);
        investimento.setValorInvestido(valor);
        investimento.setStatus(StatusInvestimento.PENDENTE);

        // QR Code fictício
        investimento.setQrCodeUrl("https://api.qrserver.com/v1/create-qr-code/?data=BOLETO-" + UUID.randomUUID());

        // Cálculo de rendimento esperado com base na taxa da proposta
        double taxaJuros = proposta.getTaxaJuros() != null ? proposta.getTaxaJuros() : 0.0;
        double rendimento = valor * (1 + (taxaJuros / 100));
        investimento.setRendimentoEsperado(rendimento);

        investimentoRepository.save(investimento);

        return ResponseEntity.ok(investimento);
    }

    // 🔹 Confirmar pagamento
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

        Proposta proposta = investimento.getProposta();

        // Recalcular o total investido
        double totalInvestido = investimentoRepository.findByProposta(proposta).stream()
                .filter(inv -> inv.getStatus() == StatusInvestimento.CONFIRMADO)
                .mapToDouble(Investimento::getValorInvestido)
                .sum();

        // Atualizar status da proposta se totalmente financiada
        if (totalInvestido >= proposta.getValorSolicitado()) {
            proposta.setStatus(StatusProposta.FINANCIADA);
            propostaRepository.save(proposta);
        }

        return ResponseEntity.ok("Pagamento confirmado e investimento liberado.");
    }

    // 🔹 Gerar boleto (PDF)
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

        String headerLogoPath = "src/main/resources/static/images/logo-finexus-df.png";
        String bgPath = "src/main/resources/static/images/favicon.png.ico";

        PdfContentByte canvas = writer.getDirectContentUnder();

        // 🔸 Fundo
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

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(102, 51, 153));
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(102, 51, 153));
        Font textFont = new Font(Font.FontFamily.HELVETICA, 12);

        // Cabeçalho
        try {
            Image logo = Image.getInstance(headerLogoPath);
            logo.scaleToFit(100, 100);

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 2f, 1f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);

            PdfPCell titleCell = new PdfPCell(new Phrase("Boleto Simulado", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(titleCell);

            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(empty);

            document.add(headerTable);
        } catch (Exception e) {
            document.add(new Paragraph("Boleto Simulado", titleFont));
        }

        document.add(new Paragraph("\n"));
        document.add(new LineSeparator());
        document.add(new Paragraph("\n"));

        // Dados
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        table.addCell(new Phrase("Investidor:", labelFont));
        table.addCell(new Phrase(investimento.getInvestidor().getNome(), textFont));

        table.addCell(new Phrase("Solicitante:", labelFont));
        table.addCell(new Phrase(proposta.getSolicitante().getNome(), textFont));

        table.addCell(new Phrase("CNPJ:", labelFont));
        table.addCell(new Phrase(proposta.getCnpj(), textFont));

        table.addCell(new Phrase("Valor Investido:", labelFont));
        table.addCell(new Phrase(String.format("R$ %.2f", investimento.getValorInvestido()), textFont));

        table.addCell(new Phrase("Rendimento Esperado:", labelFont));
        table.addCell(new Phrase(String.format("R$ %.2f", investimento.getRendimentoEsperado()), textFont));

        document.add(table);

        // QR Code
        PdfContentByte cb = writer.getDirectContent();
        try {
            Image qrImage = Image.getInstance(new URL(investimento.getQrCodeUrl()));
            qrImage.scaleToFit(150, 150);

            float qrY = 220;
            float qrW = 170;
            float qrX = (PageSize.A4.getWidth() - qrW) / 2;

            cb.saveState();
            cb.setLineWidth(1.5f);
            cb.setColorStroke(BaseColor.BLACK);
            cb.roundRectangle(qrX, qrY, qrW, qrW, 12);
            cb.stroke();
            cb.restoreState();

            qrImage.setAbsolutePosition((PageSize.A4.getWidth() - qrImage.getScaledWidth()) / 2, qrY + 10);
            document.add(qrImage);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Escaneie o QR Code acima para pagar via Pix.", textFont),
                    PageSize.A4.getWidth() / 2, qrY - 30, 0);
        } catch (Exception e) {
            System.err.println("Erro ao gerar QR Code: " + e.getMessage());
        }

        document.close();

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=boleto-finexus.pdf")
                .body(outputStream.toByteArray());
    }

    // 🔹 Listar por proposta
    @GetMapping("/proposta/{idProposta}")
    public ResponseEntity<?> listarPorProposta(@PathVariable Long idProposta) {
        Optional<Proposta> proposta = propostaRepository.findById(idProposta);
        if (proposta.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(investimentoRepository.findByProposta(proposta.get()));
    }

    // 🔹 Listar por investidor
    @GetMapping("/investidor/{idInvestidor}")
    public ResponseEntity<?> listarPorInvestidor(@PathVariable Long idInvestidor) {
        Optional<Usuario> investidor = usuarioRepository.findById(idInvestidor);
        if (investidor.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(investimentoRepository.findByInvestidor(investidor.get()));
    }
}
