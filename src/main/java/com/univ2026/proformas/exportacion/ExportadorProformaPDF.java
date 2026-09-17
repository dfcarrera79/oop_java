package com.univ2026.proformas.exportacion;

import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Exporta una proforma guardada a un documento PDF A4. */
public final class ExportadorProformaPDF {
    private static final float MARGEN = 40;
    private static final float ANCHO = PDRectangle.A4.getWidth() - 2 * MARGEN;
    private static final Color AZUL_OSCURO = new Color(24, 54, 93);
    private static final Color AZUL = new Color(38, 103, 166);
    private static final Color AZUL_CLARO = new Color(231, 240, 248);
    private static final Color GRIS = new Color(90, 100, 110);
    private static final PDFont NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(Locale.US);
    private static final float[] ANCHOS = {31, 53, 164, 38, 58, 45, 36, 90};
    private final Path directorioPredeterminado;
    private final DatosEmpresa empresa;

    public ExportadorProformaPDF(Path directorioPredeterminado) {
        this(directorioPredeterminado, DatosEmpresa.CM_INSUMOS_MEDICOS);
    }

    public ExportadorProformaPDF(Path directorioPredeterminado, DatosEmpresa empresa) {
        this.directorioPredeterminado =
                Objects.requireNonNull(directorioPredeterminado, "El directorio predeterminado no puede ser null");
        this.empresa = Objects.requireNonNull(empresa, "Los datos de empresa no pueden ser null");
    }

    /** Exporta con un nombre derivado del numero bajo el directorio inyectado. */
    public Path exportar(Proforma proforma) throws IOException {
        validar(proforma);
        return exportar(proforma, directorioPredeterminado);
    }

    /** Exporta a un archivo PDF o, si no tiene extension PDF, dentro del directorio indicado. */
    public Path exportar(Proforma proforma, Path destino) throws IOException {
        validar(proforma);
        Objects.requireNonNull(destino, "El destino no puede ser null");
        Path archivo = resolverArchivo(proforma, destino).toAbsolutePath().normalize();
        Files.createDirectories(archivo.getParent());

        Path temporal =
                Files.createTempFile(archivo.getParent(), archivo.getFileName().toString(), ".tmp");
        try {
            try (PDDocument documento = new PDDocument();
                    DocumentoPDF pdf = new DocumentoPDF(documento)) {
                pdf.nuevaPagina(false);
                dibujarEncabezado(pdf, proforma);
                dibujarCliente(pdf, proforma.getCliente());
                dibujarItems(pdf, proforma.getItems());
                dibujarTotales(pdf, proforma);
                dibujarBloque(pdf, "OBSERVACIONES", proforma.getObservaciones(), "Sin observaciones");
                dibujarBloque(pdf, "INSTRUCCIONES DE PAGO", proforma.getInstruccionesPago(), "No especificadas");
                pdf.cerrarFlujo();
                documento.save(temporal.toFile());
            }
            try {
                Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
        return archivo;
    }

    private static void validar(Proforma proforma) {
        Objects.requireNonNull(proforma, "La proforma no puede ser null");
        if (proforma.getNumero() == null || proforma.getNumero().isBlank()) {
            throw new IllegalArgumentException("La proforma debe estar guardada y tener numero");
        }
        if (proforma.getItems().isEmpty()) {
            throw new IllegalArgumentException("La proforma debe tener al menos un item");
        }
    }

    private static Path resolverArchivo(Proforma proforma, Path destino) {
        String nombre = "proforma-" + proforma.getNumero().replaceAll("[^A-Za-z0-9._-]", "_") + ".pdf";
        Path nombreDestino = destino.getFileName();
        boolean esPdf = nombreDestino != null
                && nombreDestino.toString().toLowerCase(Locale.ROOT).endsWith(".pdf");
        return esPdf ? destino : destino.resolve(nombre);
    }

    private void dibujarEncabezado(DocumentoPDF pdf, Proforma proforma) throws IOException {
        pdf.rellenar(MARGEN, pdf.y - 68, ANCHO, 68, AZUL_OSCURO);
        pdf.texto(empresa.nombre(), MARGEN + 16, pdf.y - 25, NEGRITA, 17, Color.WHITE);
        pdf.texto(
                "RUC: " + empresa.ruc() + "  |  Tel: " + empresa.telefono(),
                MARGEN + 16,
                pdf.y - 45,
                NORMAL,
                9,
                Color.WHITE);
        pdf.textoDerecha("PROFORMA", MARGEN + ANCHO - 16, pdf.y - 23, NEGRITA, 16, Color.WHITE);
        pdf.textoDerecha(proforma.getNumero(), MARGEN + ANCHO - 16, pdf.y - 43, NEGRITA, 11, Color.WHITE);
        pdf.textoDerecha(FECHA.format(proforma.getFecha()), MARGEN + ANCHO - 16, pdf.y - 57, NORMAL, 9, Color.WHITE);
        pdf.y -= 83;
    }

    private static void dibujarCliente(DocumentoPDF pdf, Cliente cliente) throws IOException {
        pdf.tituloSeccion("DATOS DEL CLIENTE");
        List<String> lineas = List.of(
                "Nombre: " + cliente.getNombre(),
                "Identificacion: " + cliente.getIdentificacion() + "    Tipo: " + cliente.getTipo(),
                "Direccion: " + valor(cliente.getDireccion()),
                "Telefono: " + valor(cliente.getTelefono()) + "    Email: "
                        + valor(cliente.getEmail().toString()));
        for (String linea : lineas) {
            for (String fragmento : dividir(linea, NORMAL, 9, ANCHO - 20)) {
                pdf.asegurarEspacio(13, false);
                pdf.texto(fragmento, MARGEN + 10, pdf.y, NORMAL, 9, Color.DARK_GRAY);
                pdf.y -= 13;
            }
        }
        pdf.y -= 8;
    }

    private static void dibujarItems(DocumentoPDF pdf, List<ItemProforma> items) throws IOException {
        pdf.asegurarEspacio(55, true);
        dibujarCabeceraTabla(pdf);
        for (int indice = 0; indice < items.size(); indice++) {
            dibujarItem(pdf, items.get(indice), indice % 2 == 1);
        }
        pdf.y -= 10;
    }

    private static void dibujarCabeceraTabla(DocumentoPDF pdf) throws IOException {
        String[] titulos = {"CANT.", "CODIGO", "DESCRIPCION", "TALLA", "P. UNIT.", "DTO.", "IVA", "TOTAL"};
        float x = MARGEN;
        pdf.rellenar(x, pdf.y - 22, ANCHO, 22, AZUL);
        for (int i = 0; i < titulos.length; i++) {
            pdf.textoCentrado(titulos[i], x, pdf.y - 14, ANCHOS[i], NEGRITA, 7, Color.WHITE);
            x += ANCHOS[i];
        }
        pdf.y -= 22;
    }

    private static void dibujarItem(DocumentoPDF pdf, ItemProforma item, boolean alterna) throws IOException {
        Producto producto = item.getProducto();
        List<String> descripcion = dividir(
                producto.getNombre() + (producto.getDescripcion().isBlank() ? "" : " - " + producto.getDescripcion()),
                NORMAL,
                8,
                ANCHOS[2] - 8);
        int linea = 0;
        do {
            if (pdf.y < 72) {
                pdf.nuevaPagina(true);
                dibujarCabeceraTabla(pdf);
            }
            int caben = Math.max(1, (int) ((pdf.y - 58) / 11));
            int cantidadLineas = Math.min(caben, descripcion.size() - linea);
            float alto = Math.max(22, cantidadLineas * 11 + 8);
            if (alterna) {
                pdf.rellenar(MARGEN, pdf.y - alto, ANCHO, alto, AZUL_CLARO);
            }
            String[] valores = {
                linea == 0 ? Integer.toString(item.getCantidad()) : "",
                linea == 0 ? producto.getCodigo() : "",
                "",
                linea == 0 && item.getTalla() != null ? item.getTalla().getEtiqueta() : "",
                linea == 0 ? dinero(producto.getPrecio().valor()) : "",
                linea == 0 ? porcentaje(item.getDescuentoPct()) : "",
                linea == 0 ? producto.getIvaPct() + "%" : "",
                linea == 0 ? dinero(item.calcularTotal()) : ""
            };
            float x = MARGEN;
            for (int columna = 0; columna < valores.length; columna++) {
                if (columna != 2) {
                    pdf.textoCentrado(valores[columna], x, pdf.y - 14, ANCHOS[columna], NORMAL, 7, Color.DARK_GRAY);
                }
                x += ANCHOS[columna];
            }
            for (int i = 0; i < cantidadLineas; i++) {
                pdf.texto(
                        descripcion.get(linea + i),
                        MARGEN + ANCHOS[0] + ANCHOS[1] + 4,
                        pdf.y - 14 - i * 11,
                        NORMAL,
                        8,
                        Color.DARK_GRAY);
            }
            pdf.linea(MARGEN, pdf.y - alto, MARGEN + ANCHO, pdf.y - alto, new Color(200, 210, 220));
            pdf.y -= alto;
            linea += cantidadLineas;
        } while (linea < descripcion.size());
    }

    private static void dibujarTotales(DocumentoPDF pdf, Proforma proforma) throws IOException {
        pdf.asegurarEspacio(78, true);
        float x = MARGEN + ANCHO - 190;
        pdf.texto("SUBTOTAL", x, pdf.y, NEGRITA, 9, GRIS);
        pdf.textoDerecha(dinero(proforma.calcularSubtotal()), MARGEN + ANCHO, pdf.y, NEGRITA, 9, GRIS);
        pdf.y -= 17;
        pdf.texto("IVA", x, pdf.y, NEGRITA, 9, GRIS);
        pdf.textoDerecha(dinero(proforma.calcularImpuesto()), MARGEN + ANCHO, pdf.y, NEGRITA, 9, GRIS);
        pdf.y -= 25;
        pdf.rellenar(x - 10, pdf.y - 8, 200, 25, AZUL_OSCURO);
        pdf.texto("TOTAL", x, pdf.y, NEGRITA, 11, Color.WHITE);
        pdf.textoDerecha(dinero(proforma.calcularTotal()), MARGEN + ANCHO - 5, pdf.y, NEGRITA, 11, Color.WHITE);
        pdf.y -= 28;
    }

    private static void dibujarBloque(DocumentoPDF pdf, String titulo, String contenido, String vacio)
            throws IOException {
        pdf.asegurarEspacio(45, true);
        pdf.tituloSeccion(titulo);
        List<String> lineas = dividir(contenido.isBlank() ? vacio : contenido, NORMAL, 9, ANCHO - 20);
        for (String linea : lineas) {
            pdf.asegurarEspacio(14, true);
            pdf.texto(linea, MARGEN + 10, pdf.y, NORMAL, 9, Color.DARK_GRAY);
            pdf.y -= 13;
        }
        pdf.y -= 8;
    }

    private static String valor(String texto) {
        return texto.isBlank() ? "No informado" : texto;
    }

    private static String dinero(BigDecimal monto) {
        return DINERO.format(monto);
    }

    private static String porcentaje(BigDecimal porcentaje) {
        return porcentaje.stripTrailingZeros().toPlainString() + "%";
    }

    private static List<String> dividir(String texto, PDFont fuente, float tamanio, float ancho) {
        String limpio = limpiar(texto);
        List<String> resultado = new ArrayList<>();
        for (String parrafo : limpio.split("\\R", -1)) {
            String restante = parrafo.strip();
            if (restante.isEmpty()) {
                resultado.add("");
                continue;
            }
            while (!restante.isEmpty()) {
                int corte = corte(restante, fuente, tamanio, ancho);
                resultado.add(restante.substring(0, corte).stripTrailing());
                restante = restante.substring(corte).stripLeading();
            }
        }
        return resultado;
    }

    private static int corte(String texto, PDFont fuente, float tamanio, float ancho) {
        int ultimoEspacio = -1;
        for (int i = 1; i <= texto.length(); i++) {
            if (Character.isWhitespace(texto.charAt(i - 1))) {
                ultimoEspacio = i - 1;
            }
            if (anchoTexto(texto.substring(0, i), fuente, tamanio) > ancho) {
                return ultimoEspacio > 0 ? ultimoEspacio : Math.max(1, i - 1);
            }
        }
        return texto.length();
    }

    private static float anchoTexto(String texto, PDFont fuente, float tamanio) {
        try {
            return fuente.getStringWidth(limpiar(texto)) / 1000 * tamanio;
        } catch (IOException excepcion) {
            throw new IllegalStateException("No se pudo medir el texto del PDF", excepcion);
        }
    }

    private static String ajustar(String texto, PDFont fuente, float tamanio, float ancho) {
        String limpio = limpiar(texto);
        if (anchoTexto(limpio, fuente, tamanio) <= ancho) {
            return limpio;
        }
        String sufijo = "...";
        int longitud = limpio.length();
        while (longitud > 0
                && anchoTexto(limpio.substring(0, longitud).stripTrailing() + sufijo, fuente, tamanio) > ancho) {
            longitud--;
        }
        return limpio.substring(0, longitud).stripTrailing() + sufijo;
    }

    private static String limpiar(String texto) {
        StringBuilder limpio = new StringBuilder();
        texto.codePoints().forEach(codigo -> {
            if (codigo == '\n' || codigo == '\r' || codigo == '\t') {
                limpio.appendCodePoint(codigo);
            } else if (codigo >= 32 && codigo <= 255) {
                limpio.appendCodePoint(codigo);
            } else if (!Character.isISOControl(codigo)) {
                limpio.append('?');
            }
        });
        return limpio.toString();
    }

    private static final class DocumentoPDF implements AutoCloseable {
        private final PDDocument documento;
        private PDPageContentStream flujo;
        private float y;

        private DocumentoPDF(PDDocument documento) {
            this.documento = documento;
        }

        private void nuevaPagina(boolean continuacion) throws IOException {
            cerrarFlujo();
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            flujo = new PDPageContentStream(documento, pagina);
            y = PDRectangle.A4.getHeight() - MARGEN;
            if (continuacion) {
                texto("CM INSUMOS MEDICOS  |  PROFORMA (continuacion)", MARGEN, y, NEGRITA, 9, AZUL_OSCURO);
                y -= 20;
            }
        }

        private void asegurarEspacio(float alto, boolean continuacion) throws IOException {
            if (y - alto < MARGEN) {
                nuevaPagina(continuacion);
            }
        }

        private void tituloSeccion(String titulo) throws IOException {
            asegurarEspacio(28, true);
            rellenar(MARGEN, y - 18, ANCHO, 18, AZUL_CLARO);
            texto(titulo, MARGEN + 8, y - 12, NEGRITA, 9, AZUL_OSCURO);
            y -= 29;
        }

        private void texto(String texto, float x, float posicionY, PDFont fuente, float tamanio, Color color)
                throws IOException {
            flujo.beginText();
            flujo.setFont(fuente, tamanio);
            flujo.setNonStrokingColor(color);
            flujo.newLineAtOffset(x, posicionY);
            flujo.showText(limpiar(texto));
            flujo.endText();
        }

        private void textoDerecha(
                String texto, float derecha, float posicionY, PDFont fuente, float tamanio, Color color)
                throws IOException {
            texto(texto, derecha - anchoTexto(texto, fuente, tamanio), posicionY, fuente, tamanio, color);
        }

        private void textoCentrado(
                String texto, float x, float posicionY, float ancho, PDFont fuente, float tamanio, Color color)
                throws IOException {
            String ajustado = ajustar(texto, fuente, tamanio, ancho - 4);
            float posicionX = x + Math.max(2, (ancho - anchoTexto(ajustado, fuente, tamanio)) / 2);
            texto(ajustado, posicionX, posicionY, fuente, tamanio, color);
        }

        private void rellenar(float x, float posicionY, float ancho, float alto, Color color) throws IOException {
            flujo.setNonStrokingColor(color);
            flujo.addRect(x, posicionY, ancho, alto);
            flujo.fill();
        }

        private void linea(float x1, float y1, float x2, float y2, Color color) throws IOException {
            flujo.setStrokingColor(color);
            flujo.moveTo(x1, y1);
            flujo.lineTo(x2, y2);
            flujo.stroke();
        }

        private void cerrarFlujo() throws IOException {
            if (flujo != null) {
                flujo.close();
                flujo = null;
            }
        }

        @Override
        public void close() throws IOException {
            cerrarFlujo();
        }
    }
}
