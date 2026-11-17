package dev.mobilecodex.app;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.graphics.pdf.PdfDocument;
import java.io.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONObject;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AttachmentPdfTest {
    @Test public void rendersPdfPageAndRejectsOutOfRangePage() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument();
        try {
            var page = pdf.startPage(new PdfDocument.PageInfo.Builder(200, 300, 1).create());
            page.getCanvas().drawColor(android.graphics.Color.BLUE);
            pdf.finishPage(page); pdf.writeTo(bytes);
        } finally { pdf.close(); }
        AttachmentStore store = new AttachmentStore(context, new ImageStore(context));
        JSONObject file = store.store(new ByteArrayInputStream(bytes.toByteArray()), "document.pdf", "application/pdf");
        assertEquals("image", store.readForAgent(file.getString("id"), 0, 0).getJSONArray("mcpContent").getJSONObject(0).getString("type"));
        try { store.readForAgent(file.getString("id"), 0, 1); fail("Invalid page accepted"); }
        catch (IOException expected) { assertEquals("Invalid PDF page", expected.getMessage()); }
    }
}
