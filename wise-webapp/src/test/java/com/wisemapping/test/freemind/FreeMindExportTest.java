package com.wisemapping.test.freemind;

import com.wisemapping.exporter.ExportException;
import com.wisemapping.exporter.freemind.FreemindExporter;
import com.wisemapping.importer.ImportFormat;
import com.wisemapping.importer.Importer;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.ImporterFactory;

import com.wisemapping.model.MindMap;
import org.testng.annotations.Test;

import java.io.*;

@Test
public class FreeMindExportTest {
    private static final String DATA_DIR_PATH = "src/test/data/freemind/";

    @Test
    public void exportImportExportTest() throws ImporterException, IOException, ExportException {

        ImporterFactory instance = ImporterFactory.getInstance();
        Importer importer = instance.getImporter(ImportFormat.FREEMIND);

        FileInputStream fileInputStream = new FileInputStream(new File(DATA_DIR_PATH, "basic.mm").getAbsolutePath());
        final MindMap mindMap = importer.importMap("basic", "basic", fileInputStream);



        final FreemindExporter freemindExporter = new FreemindExporter();
        FileOutputStream fos = new FileOutputStream(new File(DATA_DIR_PATH,"basice.mm"));
        freemindExporter.export(mindMap,fos);
        fos.close();

    }


}
