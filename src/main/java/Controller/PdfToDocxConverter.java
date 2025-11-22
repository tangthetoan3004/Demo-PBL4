package Controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;

public class PdfToDocxConverter {
    public static void convertPdfToDocx(String pdfPath, String docPath) throws Exception {
        try (PDDocument pdfDocument = PDDocument.load(new File(pdfPath));
             XWPFDocument doc = new XWPFDocument()) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDocument);
            String[] lines = text.split("\n");
            
            for (String line : lines) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(line.trim());
            }
            try (FileOutputStream out = new FileOutputStream(docPath)) {
                doc.write(out);
            }
        }
    }
}