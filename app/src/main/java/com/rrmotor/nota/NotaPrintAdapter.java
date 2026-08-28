package com.rrmotor.nota;

import android.app.Activity;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PageRange;
import android.print.PrintManager;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.io.FileOutputStream;
import java.io.IOException;

public class NotaPrintAdapter extends PrintDocumentAdapter {

    private final Activity activity;
    private final String isiNota;

    public NotaPrintAdapter(Activity activity, String isiNota) {
        this.activity = activity;
        this.isiNota = isiNota;
    }

    @Override
    public void onLayout(
            PrintAttributes oldAttributes,
            PrintAttributes newAttributes,
            CancellationSignal cancellationSignal,
            LayoutResultCallback callback,
            Bundle extras) {

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo info =
                new PrintDocumentInfo.Builder("Nota_RR_MOTOR.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build();

        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(
            PageRange[] pages,
            ParcelFileDescriptor destination,
            CancellationSignal cancellationSignal,
            WriteResultCallback callback) {

        FileOutputStream output = null;

        try {
            output = new FileOutputStream(
                    destination.getFileDescriptor()
            );

            android.graphics.pdf.PdfDocument document =
                    new android.graphics.pdf.PdfDocument();

            PrintAttributes attributes =
                    new PrintAttributes.Builder()
                            .setMediaSize(
                                    PrintAttributes.MediaSize.ISO_A4
                            )
                            .setMinMargins(
                                    PrintAttributes.Margins.NO_MARGINS
                            )
                            .build();

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(
                            595,
                            842,
                            1
                    ).create();

            android.graphics.pdf.PdfDocument.Page page =
                    document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            paint.setColor(android.graphics.Color.BLACK);
            paint.setTextSize(12);
            paint.setTypeface(Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
            ));

            float x = 30;
            float y = 40;

            String[] baris = isiNota.split("\n");

            for (String teks : baris) {

                if (cancellationSignal.isCanceled()) {
                    document.close();
                    callback.onWriteCancelled();
                    return;
                }

                if (teks.length() > 80) {
                    teks = teks.substring(0, 80);
                }

                canvas.drawText(teks, x, y, paint);
                y += 18;

                if (y > 810) {
                    break;
                }
            }

            document.finishPage(page);

            document.writeTo(output);
            document.close();

            callback.onWriteFinished(
                    new PageRange[]{PageRange.ALL_PAGES}
            );

        } catch (Exception e) {

            callback.onWriteFailed(
                    e.getMessage()
            );

        } finally {

            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
