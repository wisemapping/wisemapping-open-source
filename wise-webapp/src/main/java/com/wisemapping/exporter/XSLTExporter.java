package com.wisemapping.exporter;

import com.wisemapping.model.Mindmap;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class XSLTExporter implements Exporter {

    private Type type;

    public XSLTExporter(@NotNull Type type) {
        this.type = type;
    }

    @Override
    public void export(@NotNull byte[] xml, @NotNull OutputStream outputStream) throws ExportException {
        final ByteArrayOutputStream mmos = new ByteArrayOutputStream();

        // Convert to freemind ...
        final FreemindExporter exporter = new FreemindExporter();
        exporter.export(xml, mmos);

        // Convert to xslt transform ...
        final InputStream xsltis = this.getClass().getResourceAsStream("/com/wisemapping/export/xslt/" + type.getXsltName());
        if (xsltis == null) {
            throw new IllegalStateException("XSLT could not be resolved.");
        }

        try {
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Source xslt = new StreamSource(xsltis);
            Transformer transformer = factory.newTransformer(xslt);

            final CharArrayReader reader = new CharArrayReader(mmos.toString("iso-8859-1").toCharArray());
            final Source mmSource = new StreamSource(reader);
            transformer.transform(mmSource, new StreamResult(outputStream));
        } catch (TransformerException | UnsupportedEncodingException e) {
            throw new ExportException(e);
        }

    }

    @Override
    public void export(@NotNull Mindmap map, OutputStream outputStream) throws ExportException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static Exporter create(@NotNull Type type) {
        return new XSLTExporter(type);
    }

    public static enum Type {
        TEXT("mm2text.xsl"),
        WORD("mm2wordml_utf8.xsl"),
        CSV("mm2csv.xsl"),
        LATEX("mm2latex.xsl"),
        MICROSOFT_EXCEL("mm2xls_utf8.xsl"),
        MINDJET("mm2mj.xsl"),
        OPEN_OFFICE("mm2oowriter.xsl");

        public String getXsltName() {
            return xsltName;
        }

        private String xsltName;

        Type(@NotNull String xstFile) {
            this.xsltName = xstFile;
        }
    }


}

