package com.benzveen.imagetopdf.PDFCreater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.SizeF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.benzveen.imagetopdf.Adapter.CollageViewerAdapter;
import com.benzveen.imagetopdf.Adapter.ImageDocument;
import com.benzveen.imagetopdf.ImageToPDF;
import com.benzveen.imagetopdf.PdfCreater;
import com.benzveen.imagetopdf.R;
import com.benzveen.imagetopdf.TouchUtils.LockableScrollView;
import com.benzveen.imagetopdf.TouchUtils.MultiTouchListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class PdfDocument {

    PdfCreater createrActiviry;
    ArrayList<ArrayList<ImageDocument>> datasets;
    HashMap<Integer, FlexboxLayout> layouts;
    int itemsPerPage = 1;
    int itemPerRow = 1;
    int itemPerColomn = 1;
    int flexDirection = FlexDirection.ROW;
    int flexWrap = FlexWrap.WRAP;
    private LinearLayout mainParentView;
    private LockableScrollView listView;

    public PdfDocument(PdfCreater creater) {
        createrActiviry = creater;
        datasets = new ArrayList<>();
        datasets = chopped(ImageToPDF.documents, itemsPerPage);
        layouts = new HashMap<>();
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public void setFlexDirection(int flexDirection) {
        this.flexDirection = flexDirection;
    }

    public void setItemPerRow(int itemPerRow) {
        this.itemPerRow = itemPerRow;
    }

    public void setItemPerColomn(int itemPerColomn) {
        this.itemPerColomn = itemPerColomn;
    }

    public void setFlexWrap(int flexWrap) {
        this.flexWrap = flexWrap;
    }

    public int getPageCount() {
        return datasets.size();
    }

    public ArrayList<ArrayList<ImageDocument>> getDatasets() {
        return datasets;
    }

    public View getView(int position) {
        return getTabLayout(position, false);
    }

    public void DoLayout() {
        datasets = new ArrayList<>();
        datasets = chopped(ImageToPDF.documents, itemsPerPage);
        listView = new LockableScrollView(createrActiviry);
        CollageViewerAdapter adapter = new CollageViewerAdapter(createrActiviry);
        listView.setAdapter(adapter);
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(mainParams);
        if (createrActiviry.GetPdfParentView().getChildCount() > 0) {
            createrActiviry.GetPdfParentView().removeAllViews();
        }
        createrActiviry.GetPdfParentView().addView(listView);
        if (layouts.size() > 0) {
            layouts.clear();
        }
    }

    int width, height;

    private FlexboxLayout getTabLayout(int position, boolean value) {
        final FlexboxLayout parentView;
        if (layouts.containsKey(position)) {
            parentView = layouts.get(position);
        } else {
            ArrayList<ImageDocument> doc = datasets.get(position);
            parentView = new FlexboxLayout(createrActiviry);
            parentView.setFlexWrap(FlexWrap.WRAP);
            parentView.setFlexDirection(flexDirection);
            parentView.setJustifyContent(JustifyContent.CENTER);
            parentView.setAlignItems(AlignItems.CENTER);
            // parentView.setAlignContent(AlignContent.CENTER);
            parentView.setBackgroundColor(createrActiviry.getResources().getColor(R.color.white));

            width = createrActiviry.getResources()
                    .getDisplayMetrics().widthPixels;
            height = createrActiviry.getResources()
                    .getDisplayMetrics().heightPixels;

            SizeF size = new SizeF(width, height);
            SizeF fsize = computePageBitmapSize(size, new SizeF(595, 842));
            width = (int) fsize.getWidth();
            height = (int) fsize.getHeight();
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    width,
                    height
            );
           // params.setMargins(50, 50, 50, 50);
            parentView.setLayoutParams(params);

            for (ImageDocument image : doc) {
                if (!value) {
                    Glide.with(createrActiviry)
                            .asBitmap()
                            .load(image.getImageDocument()).override(1000, 1000)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    CollageView imageView = new CollageView(createrActiviry);
                                    imageView.setOnTouchListener(new MultiTouchListener(listView));
                                    //SizeF bounds = computePageBitmapSize(new SizeF(resource.getWidth(), resource.getHeight()), new SizeF(595, 842));
                                    SizeF bounds = scaleSize(resource.getWidth(), resource.getHeight(), (width -300) / itemPerRow, (height - 300) / itemPerColomn);

                                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams((int) bounds.getWidth(), (int) bounds.getHeight());
                                    params.setMargins(15, 15, 15, 15);

                                    imageView.setLayoutParams(params);
                                    imageView.setBackgroundColor(Color.RED);
                                    imageView.setImageBitmap(resource);
                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                    parentView.addView(imageView);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                } else {

                    FutureTarget<Bitmap> futureBitmap = Glide.with(createrActiviry)
                            .asBitmap()
                            .load(image.getImageDocument())
                           .submit();
                    try {
                        Bitmap resource = futureBitmap.get();
                        CollageView imageView = new CollageView(createrActiviry);
                        imageView.setOnTouchListener(new MultiTouchListener(listView));
                        //SizeF bounds = computePageBitmapSize(new SizeF(resource.getWidth(), resource.getHeight()), new SizeF(595, 842));
                        SizeF bounds = scaleSize(resource.getWidth(), resource.getHeight(), (width - 300) / itemPerRow, (height - 300) / itemPerColomn);

                        FlexboxLayout.LayoutParams params2 = new FlexboxLayout.LayoutParams((int) bounds.getWidth(), (int) bounds.getHeight());
                        params2.setMargins(15, 15, 15, 15);

                        imageView.setLayoutParams(params2);
                        imageView.setBackgroundColor(Color.RED);
                        imageView.setImageBitmap(resource);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        parentView.addView(imageView);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

            layouts.put(position, parentView);
        }
        return parentView;
    }


    static ArrayList<ArrayList<ImageDocument>> chopped(ArrayList<ImageDocument> list, final int L) {
        ArrayList<ArrayList<ImageDocument>> parts = new ArrayList<ArrayList<ImageDocument>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<ImageDocument>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    private SizeF computePageBitmapSize(SizeF mImageViewSize, SizeF pageSize) {
        float width;
        float width2 = pageSize.getWidth() / pageSize.getHeight();
        if (width2 > mImageViewSize.getWidth() / mImageViewSize.getHeight()) {
            width = mImageViewSize.getWidth();
            if (width > 3072.0f) {
                width = 3072.0f;
            }
            width2 = (float) Math.round(width / width2);
        } else {
            width = mImageViewSize.getHeight();
            if (width > 3072.0f) {
                width = 3072.0f;
            }
            float f = width2 * width;
            width2 = width;
            width = f;
        }
        return new SizeF(width, width2);
    }

    public void PrintPDF(final String fileName) {
        new AsyncTask<String, Integer, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                createrActiviry.showBottomSheet(getDatasets().size());
            }

            @Override
            protected Boolean doInBackground(String... strings) {

                try {
                    File root = createrActiviry.getFilesDir();
                    File myDir = new File(root + "/ImageToPDF");
                    if (!myDir.exists()) {
                        myDir.mkdirs();
                    }
                    File file = new File(myDir.getAbsolutePath(), fileName+".pdf");
                    if (file.exists())
                        file.delete();

                    Document destination = new Document();
                    PdfWriter writer = PdfWriter.getInstance(destination, new FileOutputStream(file));
                    destination.open();
                    for (int i = 0; i < getDatasets().size(); i++) {
                        View v;
                        Bitmap b;
                        if (layouts.containsKey(i)) {
                            v = layouts.get(i);
                            b = Bitmap.createBitmap(width,
                                    height,
                                    Bitmap.Config.ARGB_8888);
                            b.setDensity(300);
                            //Create a canvas with the specified bitmap to draw into
                            Canvas c = new Canvas(b);

                            //Render this view (and all of its children) to the given Canvas
                            v.draw(c);

                        } else {

                            v = getTabLayout(i, true);
                            b = createBitmapFromLayout(v);
                        }
                        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.JPEG, 100, imageStream);
                        Image image = Image.getInstance(imageStream.toByteArray());
                        image.setAbsolutePosition(0, 0);
                        image.scaleAbsoluteWidth(destination.getPageSize().getWidth());
                        image.scaleAbsoluteHeight(destination.getPageSize().getHeight());
                        imageStream.close();
                        destination.add(image);
                        destination.newPage();
                        publishProgress(i + 1);
                    }

                    destination.close();

                } catch (
                        Exception ex) {
                    String message = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                createrActiviry.runPostExecution(result);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                createrActiviry.setProgress(values[0], getDatasets().size());
            }


        }.execute(fileName);


    }

    public Bitmap createBitmapFromLayout(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

        view.layout(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width,
                height,
                Bitmap.Config.ARGB_8888);

        //Create a canvas with the specified bitmap to draw into
        Canvas c = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(c);

        return bitmap;
    }

    private SizeF scaleSize(int width, int height, int maxWidth, int maxHeight) {

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int) (width / ratio);

        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }

        return new SizeF(width, height);
    }

}
