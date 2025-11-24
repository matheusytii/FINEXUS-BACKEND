package br.com.finexus.crowdfunding.controller;

import br.com.finexus.crowdfunding.model.*;
import br.com.finexus.crowdfunding.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/parcelas")
@CrossOrigin(origins = "*")
public class ParcelaDividaController {

    @Autowired
    private ParcelaDividaRepository parcelaRepository;

    @Autowired
    private DividaRepository dividaRepository;

    @Autowired
    private SaldoRepository saldoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // <-- AGORA ESTÁ CORRETO!

    // =======================================================
    // MÉTODO QUE ATUALIZA PARCELAS VENCIDAS AUTOMATICAMENTE
    // =======================================================
    private void atualizarStatusVencidas(List<ParcelaDivida> parcelas) {
        LocalDate hoje = LocalDate.now();

        for (ParcelaDivida parcela : parcelas) {

            if (parcela.getStatus() != StatusParcela.PAGA &&
                    parcela.getVencimento().isBefore(hoje)) {

                parcela.setStatus(StatusParcela.VENCIDA);
                parcelaRepository.save(parcela);

                Usuario tomador = parcela.getDivida().getTomador();

                tomador.setInadimplente(true);
                tomador.setHistoricoInadimplencia(
                        tomador.getHistoricoInadimplencia() + 1);
                tomador.setRiscoAlto(true); // COR DE PERFIL (vermelho)

                usuarioRepository.save(tomador);
            }
        }
    }

    @GetMapping("/divida/{idDivida}")
    public ResponseEntity<?> listar(@PathVariable Long idDivida) {

        Optional<Divida> dividaOpt = dividaRepository.findById(idDivida);
        if (dividaOpt.isEmpty())
            return ResponseEntity.badRequest().body("Dívida não encontrada.");

        List<ParcelaDivida> parcelas = parcelaRepository.findByDividaId(idDivida);

        // 🔥 Atualiza automaticamente as atrasadas
        atualizarStatusVencidas(parcelas);

        return ResponseEntity.ok(parcelas);
    }

    @GetMapping("/{idParcela}/boleto")
    public ResponseEntity<byte[]> gerarBoleto(@PathVariable Long idParcela) throws Exception {

        Optional<ParcelaDivida> parcelaOpt = parcelaRepository.findById(idParcela);
        if (parcelaOpt.isEmpty())
            return ResponseEntity.notFound().build();

        ParcelaDivida parcela = parcelaOpt.get();
        Divida divida = parcela.getDivida();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 80);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();

        // Caminhos dos assets
        String headerLogoPath = "src/main/resources/static/images/logo-finexus-df.png";
        String bgPath = "src/main/resources/static/images/favicon.png.ico";

        PdfContentByte canvas = writer.getDirectContentUnder();

        // Fundo com opacidade
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
            System.err.println("Erro no background: " + e.getMessage());
        }

        // Fontes roxas iguais ao de investimento
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(102, 51, 153));
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(102, 51, 153));
        Font textFont = new Font(Font.FontFamily.HELVETICA, 12);

        // Cabeçalho com logo + título
        try {
            Image logo = Image.getInstance(headerLogoPath);
            logo.scaleToFit(100, 100);

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[] { 1f, 2f, 1f });
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);

            PdfPCell titleCell = new PdfPCell(new Phrase("Boleto da Parcela", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(titleCell);

            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(empty);

            document.add(headerTable);
        } catch (Exception e) {
            document.add(new Paragraph("Boleto da Parcela", titleFont));
        }

        document.add(new Paragraph("\n"));
        document.add(new LineSeparator());
        document.add(new Paragraph("\n"));

        // Tabela com dados da parcela (substituindo as variáveis de investimento)
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        table.addCell(new Phrase("Tomador:", labelFont));
        table.addCell(new Phrase(divida.getTomador().getNome(), textFont));

        table.addCell(new Phrase("Número da Parcela:", labelFont));
        table.addCell(new Phrase(String.valueOf(parcela.getNumeroParcela()), textFont));

        table.addCell(new Phrase("Valor da Parcela:", labelFont));
        table.addCell(new Phrase(String.format("R$ %.2f", parcela.getValor()), textFont));

        table.addCell(new Phrase("Vencimento:", labelFont));
        table.addCell(new Phrase(String.valueOf(parcela.getVencimento()), textFont));

        document.add(table);

        // Caixa do QR Code (fixo, opcional)
        PdfContentByte cb = writer.getDirectContent();
        try {
            // Caso futuramente coloque QR Code real no model
            Image qrImage = Image.getInstance("src/main/resources/static/images/qrcode-pix-exemplo.png");
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
                    new Phrase("Escaneie o QR Code para pagar esta parcela.", textFont),
                    PageSize.A4.getWidth() / 2, qrY - 30, 0);

        } catch (Exception e) {
            System.err.println("QR Code não encontrado: " + e.getMessage());
        }

        document.close();

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=boleto-parcela.pdf")
                .body(outputStream.toByteArray());
    }

    // Pagar parcela usando saldo
    @PutMapping("/{idParcela}/pagar/{idTomador}")
    public ResponseEntity<?> pagar(
            @PathVariable Long idParcela,
            @PathVariable Long idTomador) {

        Optional<ParcelaDivida> parcelaOpt = parcelaRepository.findById(idParcela);
        if (parcelaOpt.isEmpty())
            return ResponseEntity.badRequest().body("Parcela não encontrada.");

        ParcelaDivida parcela = parcelaOpt.get();
        Divida divida = parcela.getDivida();
        Proposta proposta = divida.getProposta();

        if (!divida.getTomador().getId().equals(idTomador))
            return ResponseEntity.badRequest().body("Esta parcela não pertence a este tomador.");

        if (parcela.getStatus() == StatusParcela.PAGA)
            return ResponseEntity.badRequest().body("Esta parcela já está paga.");

        // ======================================================
        // 1) DESCONTAR SALDO DO TOMADOR
        // ======================================================

        Saldo saldoTomador = saldoRepository.findByUsuarioId(idTomador);
        if (saldoTomador == null)
            return ResponseEntity.badRequest().body("O tomador não possui saldo.");

        if (saldoTomador.getValor() < parcela.getValor())
            return ResponseEntity.badRequest().body("Saldo insuficiente.");

        saldoTomador.setValor(saldoTomador.getValor() - parcela.getValor());
        saldoRepository.save(saldoTomador);
        boolean existiaParcelaPagaAntes = parcelaRepository
                .existsByDividaIdAndStatus(divida.getId(), StatusParcela.PAGA);

        // 2) Agora sim paga esta parcela
        parcela.setStatus(StatusParcela.PAGA);
        parcela.setDataPagamento(LocalDate.now());
        parcelaRepository.save(parcela);

        // 3) Se NÃO existia nenhuma antes e a proposta está FINANCIADA → muda para EM
        // PAGAMENTO
        if (!existiaParcelaPagaAntes && proposta.getStatus() == StatusProposta.FINANCIADA) {
            proposta.setStatus(StatusProposta.EM_PAGAMENTO);
        }

        // ======================================================
        // 2) PAGAR APENAS INVESTIDORES QUE ESTÃO NA DIVIDA
        // ======================================================

        List<Long> idsInvestidoresDaDivida = divida.getInvestidoresIds();

        if (idsInvestidoresDaDivida == null || idsInvestidoresDaDivida.isEmpty())
            return ResponseEntity.ok("Parcela paga pelo tomador. Nenhum investidor a ser pago.");

        // Todos investimentos CONFIRMADOS da proposta
        List<Investimento> investimentosConfirmados = proposta.getInvestimentos().stream()
                .filter(inv -> inv.getStatus() == StatusInvestimento.CONFIRMADO)
                .toList();

        if (investimentosConfirmados.isEmpty())
            return ResponseEntity.ok("Parcela paga, mas nenhum investimento confirmado para receber.");

        // Agora filtra somente investimentos que fazem parte da dívida
        List<Investimento> investidoresQueDevemReceber = investimentosConfirmados.stream()
                .filter(inv -> idsInvestidoresDaDivida.contains(inv.getInvestidor().getId()))
                .toList();

        if (investidoresQueDevemReceber.isEmpty())
            return ResponseEntity
                    .ok("Parcela paga, mas investidores da dívida não correspondem aos investimentos confirmados.");

        // Soma total investido pelos investidores envolvidos nesta dívida
        double totalInvestido = investidoresQueDevemReceber.stream()
                .mapToDouble(Investimento::getValorInvestido)
                .sum();

        double valorParcela = parcela.getValor();

        // ======================================================
        // 3) DISTRIBUIR O PAGAMENTO PROPORCIONALMENTE
        // ======================================================

        for (Investimento inv : investidoresQueDevemReceber) {

            double percentual = inv.getValorInvestido() / totalInvestido;
            double valorReceber = valorParcela * percentual;

            Saldo saldoInvestidor = saldoRepository.findByUsuarioId(inv.getInvestidor().getId());

            if (saldoInvestidor == null) {
                saldoInvestidor = new Saldo();
                saldoInvestidor.setUsuario(inv.getInvestidor());
                saldoInvestidor.setValor(0.0);
            }

            saldoInvestidor.setValor(saldoInvestidor.getValor() + valorReceber);
            saldoRepository.save(saldoInvestidor);
        }

        return ResponseEntity.ok("Parcela paga e valores distribuídos proporcionalmente aos investidores!");
    }
}
