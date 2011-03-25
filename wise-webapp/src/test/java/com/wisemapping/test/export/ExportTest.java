package com.wisemapping.test.export;

import com.wisemapping.exporter.ExportException;
import com.wisemapping.exporter.ExportFormat;
import com.wisemapping.exporter.ExportProperties;
import com.wisemapping.exporter.freemind.FreemindExporter;
import com.wisemapping.importer.ImportFormat;
import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.ImporterFactory;

import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindMapNative;
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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Test
public class ExportTest {
    private static final String DATA_DIR_PATH = "src/test/data/svg/";

    @Test(dataProvider = "Data-Provider-Function")
    public void exportSvgTest(@NotNull final File svgFile, @NotNull final File pngFile) throws ImporterException, IOException, ExportException {

        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        reader = new BufferedReader(new FileReader(svgFile));
        String text;
        while((text=reader.readLine()) != null){
            buffer.append(text).append(System.getProperty("line.separator"));
        }

        String svgXml = buffer.toString();

        final ExportFormat format = ExportFormat.PNG;
        final ExportProperties properties = ExportProperties.create(format);
        final ExportProperties.ImageProperties imageProperties = (ExportProperties.ImageProperties) properties;
        imageProperties.setSize(ExportProperties.ImageProperties.Size.LARGE);

        // Write content ...
        MindMap mindMap = new MindMap();
        MindMapNative nativeBrowser = new MindMapNative();
        nativeBrowser.setSvgXml(svgXml);
        mindMap.setNativeBrowser(nativeBrowser);

        //Export to PNG
            OutputStream outputStream = new FileOutputStream(pngFile, false);
            try {
                mindMap.export(properties, outputStream);
                outputStream.close();
                System.out.println("finished");
            } catch (JAXBException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (TranscoderException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (TransformerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (XMLStreamException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
