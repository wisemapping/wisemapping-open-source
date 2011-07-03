package com.wisemapping.test.freemind;

import com.wisemapping.exporter.ExportException;
import com.wisemapping.exporter.FreemindExporter;
import com.wisemapping.importer.ImporterException;

import com.wisemapping.model.MindMap;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;

@Test
public class FreemindExportTest {
    private static final String DATA_DIR_PATH = "src/test/data/wisemaps/";

    @Test(dataProvider = "Data-Provider-Function")
    public void exportImportExportTest(@NotNull final File wisemap, @NotNull final File recFile) throws ImporterException, IOException, ExportException {


        final MindMap mindmap = load(wisemap);

        final FreemindExporter freemindExporter = new FreemindExporter();
        if (recFile.exists()) {
            // Compare rec and file ...

            // Load rec file co
            final FileInputStream fis = new FileInputStream(recFile);
            final InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
            final BufferedReader br = new BufferedReader(isr);

            final StringBuilder recContent = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                recContent.append(line);
                line = br.readLine();

            }

            fis.close();

            // Export mile content ...
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            freemindExporter.export(mindmap, bos);
            final String exportContent = new String(bos.toByteArray(),"UTF-8");

            Assert.assertEquals(recContent.toString(), exportContent);

        } else {
            final FileOutputStream fos = new FileOutputStream(recFile);
            freemindExporter.export(mindmap, fos);
            fos.close();
        }


    }

    private MindMap load(@NotNull File wisemap) throws IOException {
        final FileInputStream fis = new FileInputStream(wisemap);
        final InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
        final BufferedReader br = new BufferedReader(isr);

        final StringBuilder content = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            content.append(line);
            line = br.readLine();

        }
        fis.close();

        final MindMap result = new MindMap();
        result.setXml(content.toString().getBytes("UTF-8"));
        result.setNativeXml(content.toString());
        return result;
    }

    //This function will provide the parameter data
    @DataProvider(name = "Data-Provider-Function")
    public Object[][] parameterIntTestProvider() {

        final File dataDir = new File(DATA_DIR_PATH);
        final File[] freeMindFiles = dataDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".wxml");
            }
        });

        final Object[][] result = new Object[freeMindFiles.length][2];
        for (int i = 0; i < freeMindFiles.length; i++) {
            File freeMindFile = freeMindFiles[i];
            final String name = freeMindFile.getName();
            result[i] = new Object[]{freeMindFile, new File(DATA_DIR_PATH, name.substring(0, name.lastIndexOf(".")) + ".mmr")};
        }

        return result;
    }


}
