package com.wisemapping.test.export;

import com.wisemapping.exporter.ExportException;
import com.wisemapping.exporter.ExportFormat;
import com.wisemapping.exporter.ExportProperties;
import com.wisemapping.exporter.ExporterFactory;
import com.wisemapping.importer.ImporterException;

import com.wisemapping.model.MindMap;
import org.apache.batik.transcoder.TranscoderException;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.*;

@Test
public class ExportTest {
    private static final String DATA_DIR_PATH = "src/test/data/svg/";

    @Test(dataProvider = "Data-Provider-Function")
    public void exportSvgTest(@NotNull final File svgFile, @NotNull final File pngFile) throws ImporterException, IOException, ExportException, TransformerException, XMLStreamException, JAXBException, SAXException, TranscoderException, ParserConfigurationException {

        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        reader = new BufferedReader(new FileReader(svgFile));
        String text;
        while ((text = reader.readLine()) != null) {
            buffer.append(text).append(System.getProperty("line.separator"));
        }

        String svgXml = buffer.toString();

        final ExportFormat format = ExportFormat.PNG;
        final ExportProperties properties = ExportProperties.create(format);
        final ExportProperties.ImageProperties imageProperties = (ExportProperties.ImageProperties) properties;
        imageProperties.setSize(ExportProperties.ImageProperties.Size.LARGE);
        String baseUrl = "file://" + svgFile.getParentFile().getAbsolutePath() + "/../../../../../wise-editor/src/main/webapp/icons";
        properties.setBaseImagePath(baseUrl);

        // Write content ...
        if (pngFile.exists()) {
            // Export mile content ...
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ExporterFactory.export(imageProperties, null, bos, svgXml);

            // Load rec file co
            final FileInputStream fis = new FileInputStream(pngFile);
            final InputStreamReader isr = new InputStreamReader(fis);
            final BufferedReader br = new BufferedReader(isr);

            final StringBuilder recContent = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                recContent.append(line);
                line = br.readLine();
            }

            fis.close();

            //Since line separator chenges between \r and \n, lets read line by line
            final String exportContent = new String(bos.toByteArray());
            BufferedReader expBuf = new BufferedReader(new StringReader(exportContent));
            final StringBuilder expContent = new StringBuilder();
            String expLine = expBuf.readLine();
            while (expLine != null) {
                expContent.append(expLine);
                expLine = expBuf.readLine();

            }

            Assert.assertEquals(expContent.toString().trim(), expContent.toString().trim());

        } else {
            OutputStream outputStream = new FileOutputStream(pngFile, false);
            ExporterFactory.export(imageProperties, null, outputStream, svgXml);
            outputStream.close();
        }
    }

    //This function will provide the parameter data
    @DataProvider(name = "Data-Provider-Function")
    public Object[][] parameterIntTestProvider() {

        final File dataDir = new File(DATA_DIR_PATH);
        final File[] freeMindFiles = dataDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".svg");
            }
        });

        final Object[][] result = new Object[freeMindFiles.length][2];
        for (int i = 0; i < freeMindFiles.length; i++) {
            File freeMindFile = freeMindFiles[i];
            final String name = freeMindFile.getName();
            result[i] = new Object[]{freeMindFile, new File(DATA_DIR_PATH, name.substring(0, name.lastIndexOf(".")) + ".png")};
        }

        return result;
    }


}
