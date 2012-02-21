package com.wisemapping.test.freemind;

import com.wisemapping.exporter.ExportException;
import com.wisemapping.exporter.FreemindExporter;
import com.wisemapping.importer.ImportFormat;
import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.ImporterFactory;
import com.wisemapping.model.MindMap;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;

@Test
public class ImportExportTest {
    private static final String DATA_DIR_PATH = "src/test/data/freemind/";
    final private Importer importer;
    final private FreemindExporter exporter;

    public ImportExportTest() throws ImporterException {
        ImporterFactory exporterFactory = ImporterFactory.getInstance();
        importer = exporterFactory.getImporter(ImportFormat.FREEMIND);
        exporter = new FreemindExporter();

    }


    @Test(dataProvider = "Data-Provider-Function")
    public void exportImportTest(@NotNull final File freeMindFile, @NotNull final File wiseFile, @NotNull final File freeRecFile) throws ImporterException, IOException, ExportException {


        final FileInputStream fileInputStream = new FileInputStream(freeMindFile.getAbsolutePath());
        final MindMap mindMap = importer.importMap("basic", "basic", fileInputStream);


        // Compare mindmap output ...
        if (wiseFile.exists()) {
            // Compare rec and file ...
            final String recContent = readFile(wiseFile);

            // Export mile content ...
            Assert.assertEquals(mindMap.getXmlStr(), recContent);

        } else {
            final FileOutputStream fos = new FileOutputStream(wiseFile);
            fos.write(mindMap.getXmlStr().getBytes("UTF-8"));
            fos.close();
        }

        // Compare freemind output ...
        if (freeRecFile.exists()) {
            // Compare rec and file ...
            final String recContent = readFile(freeRecFile);

            // Export content ...
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exporter.export(mindMap, bos);

            Assert.assertEquals(bos.toByteArray(), recContent.getBytes("UTF-8"));

        } else {
            final FileOutputStream fos = new FileOutputStream(freeRecFile);
            exporter.export(mindMap, fos);
            fos.close();
        }

    }

    private String readFile(@NotNull File file) throws IOException {
        // Load rec file co
        final FileInputStream fis = new FileInputStream(file);
        final InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
        final BufferedReader br = new BufferedReader(isr);

        final StringBuilder result = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            result.append(line);
            line = br.readLine();

        }

        fis.close();
        return result.toString();
    }

    //This function will provide the parameter data
    @DataProvider(name = "Data-Provider-Function")
    public Object[][] parameterIntTestProvider() {

        final String testNameToRun = System.getProperty("wise.test.name");

        final File dataDir = new File(DATA_DIR_PATH);
        final File[] freeMindFiles = dataDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".mm") && (testNameToRun==null || name.startsWith(testNameToRun));
            }
        });

        final Object[][] result = new Object[freeMindFiles.length][2];
        for (int i = 0; i < freeMindFiles.length; i++) {
            File freeMindFile = freeMindFiles[i];
            final String name = freeMindFile.getName();
            String testName = name.substring(0, name.lastIndexOf("."));
            result[i] = new Object[]{freeMindFile, new File(DATA_DIR_PATH, testName + ".wxml"), new File(DATA_DIR_PATH, testName + ".mmr")};
        }

        return result;
    }


}
