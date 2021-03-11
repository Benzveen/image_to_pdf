package com.benzveen.imagetopdf.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Toast;

import com.benzveen.imagetopdf.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.provider.Telephony.Mms.Part.FILENAME;

public class PDFOCRAsync extends AsyncTask<Void, Integer, Boolean> {

    private int pageIndex;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    private ParcelFileDescriptor mFileDescriptor;
    private MainActivity activity;
    private File pdfFile;
    private int mPageIndex;
    PdfReader pdfReader;
    BaseFont base_font;
    PdfStamper stamper;
    File tempfile;
    boolean isOCRCompleted;

    public PDFOCRAsync(File file, MainActivity activity) {
        this.pdfFile = file;
        this.activity = activity;
        mPageIndex = 0;

    }

    public void openRenderer() throws IOException {
        Context context = activity.getApplicationContext();
        // In this sample, we read a PDF from the assets directory.
        try {
            if (pdfFile.exists()) {
                mFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
                // This is the PdfRenderer we use to render the PDF.
                if (mFileDescriptor != null) {
                    mPdfRenderer = new PdfRenderer(mFileDescriptor);
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.showBottomSheet(getPageCount());
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        int pageCount = getPageCount();
        if (pageCount > 0) {
            try {
                base_font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/PDFOCR");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                tempfile = new File(myDir.getAbsolutePath(), pdfFile.getName());
                copyFileUsingStream(pdfFile, tempfile);
                pdfReader = new PdfReader(new FileInputStream(tempfile));
                stamper = new PdfStamper(pdfReader, new FileOutputStream(tempfile));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < pageCount; i++) {
                Bitmap bitmap = showPage(i);
                try {
                    processDocumentImage(bitmap, i + 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                publishProgress(i + 1);
            }
        }
        return null;
    }

    protected void onProgressUpdate(Integer... values) {
        activity.setProgress(values[0], getPageCount());
    }

    public void onPostExecute(Boolean bool) {
        Dispose();
        activity.runPostExecution(tempfile);
    }

    public int getPageCount() {
        return mPdfRenderer.getPageCount();
    }

    void Dispose() {
        if (stamper != null) {
            try {
                stamper.close();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (pdfReader != null) {
            pdfReader.close();
        }
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeRenderer() throws IOException {
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mPdfRenderer.close();
        mFileDescriptor.close();
    }

    private Bitmap showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return null;
        }
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        return bitmap;
    }

    private FirebaseVisionTextRecognizer getLocalDocumentRecognizer() {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        return detector;
    }

    private void processDocumentImage(Bitmap bitmap, final int currentPage) throws InterruptedException, ExecutionException {

        FirebaseVisionTextRecognizer detector = getLocalDocumentRecognizer();
        FirebaseVisionImage myImage = FirebaseVisionImage.fromBitmap(bitmap);

        Task<FirebaseVisionText> results = detector.processImage(myImage);
        FirebaseVisionText result = Tasks.await(results);

        if (result != null) {
            PdfContentByte cb = stamper.getOverContent(currentPage);
            for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                for (FirebaseVisionText.Line line : block.getLines()) {

                    String lineText = line.getText();
                    Float lineConfidence = line.getConfidence();
                    List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                    Point[] lineCornerPoints = line.getCornerPoints();
                    Rect lineFrame = line.getBoundingBox();
                    for (FirebaseVisionText.Element element : line.getElements()) {
                        String elementText = element.getText();
                        Float elementConfidence = element.getConfidence();
                        List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                        Point[] elementCornerPoints = element.getCornerPoints();
                        Rect elementFrame = element.getBoundingBox();

                        float textFontSize = getExactFontSize(elementText, elementFrame);
                        float ascent = base_font.getAscentPoint(elementText, textFontSize);
                        cb.beginText();
                        cb.setFontAndSize(base_font, textFontSize);
                        cb.setTextRenderingMode(3);
                        cb.setTextMatrix(elementFrame.left, mCurrentPage.getHeight() - elementFrame.top - ascent);
                        cb.showText(element.getText());
                        cb.endText();
                    }
                }
            }
        }
    }

    private void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    private int getExactFontSize(String text, Rect bounds) {
        int height = 1;
        int width = bounds.width();
        float textWidth = base_font.getWidthPoint(text, height);
        while (textWidth < width) {
            height = height + 1;
            textWidth = base_font.getWidthPoint(text, height);
        }

        return height;
    }
}
