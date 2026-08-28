package com.rrmotor.nota;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.FileOutputStream;
import java.io.IOException;

public class NotaPrintAdapter extends PrintDocumentAdapter {

    private final Context context;
    private final String isiNota;

    public NotaPrintAdapter(
            Context context,
            String isiNota
    ) {
        this.context = context;
        this.isiNota = isiNota;
    }

    @Override
    public void onLayout(
            PrintAttributes oldAttributes,
            PrintAttributes newAttributes,
            CancellationSignal cancellationSignal,
            LayoutResultCallback callback,
            Bundle extras
    ) {

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo info =
                new PrintDocumentInfo.Builder(
                        "Nota_RR_MOTOR.pdf"
                )
                        .setContentType(
                                PrintDocumentInfo.CONTENT_TYPE_DOCUMENT
                        )
                        .setPageCount(1)
                        .build();

        callback.onLayoutFinished(
                info,
                true
        );
    }

    @Override
    public void onWrite(
            PageRange[] pages,
            ParcelFileDescriptor destination,
            CancellationSignal cancellationSignal,
            WriteResultCallback callback
    ) {

        FileOutputStream output = null;

        try {

            output =
                    new FileOutputStream(
                            destination.getFileDescriptor()
                    );

            android.graphics.pdf.PdfDocument pdf =
                    new android.graphics.pdf.PdfDocument();

            PrintAttributes.MediaSize mediaSize =
                    PrintAttributes.MediaSize.ISO_A4;

            android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                    new android.graphics.pdf.PdfDocument.PageInfo.Builder(
                            595,
                            842,
                            1
                    ).create();

            android.graphics.pdf.PdfDocument.Page page =
                    pdf.startPage(pageInfo);

            Canvas canvas =
                    page.getCanvas();

            Paint paint =
                    new Paint();

            paint.setColor(
                    android.graphics.Color.BLACK
            );

            paint.setTextSize(12);

            paint.setTypeface(
                    Typeface.create(
                            Typeface.MONOSPACE,
                            Typeface.NORMAL
                    )
            );

            float x = 40;
            float y = 50;

            String[] baris =
                    isiNota.split("\n");

            for (String teks : baris) {

                if (cancellationSignal.isCanceled()) {

                    pdf.close();

                    callback.onWriteCancelled();

                    return;
                }

                canvas.drawText(
                        teks,
                        x,
                        y,
                        paint
                );

                y += 18;

                if (y > 800) {
                    break;
                }
            }

            pdf.finishPage(page);

            pdf.writeTo(output);

            pdf.close();

            callback.onWriteFinished(
                    new PageRange[]{
                            PageRange.ALL_PAGES
                    }
            );

        } catch (Exception e) {

            callback.onWriteFailed(
                    e.getMessage()
            );

        } finally {

            try {

                if (output != null) {
                    output.close();
                }

            } catch (IOException ignored) {
            }
        }
    }
}
