package com.rrmotor.nota;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout itemContainer;
    private TextView totalText;
    private TextView sisaText;
    private TextView statusText;

    private EditText namaInput;
    private EditText waInput;
    private EditText tanggalInput;
    private EditText motorInput;
    private EditText dpInput;

    private SharedPreferences prefs;

    private static final String PREF_NAME = "RR_MOTOR_NOTA";
    private static final String KEY_HISTORY = "HISTORY";

    private final NumberFormat rupiah =
            NumberFormat.getCurrencyInstance(
                    new Locale("id", "ID")
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        prefs = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

        tampilkanAplikasi();
    }

    private void tampilkanAplikasi() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout utama = new LinearLayout(this);
        utama.setOrientation(LinearLayout.VERTICAL);
        utama.setPadding(18, 12, 18, 25);

        scroll.addView(utama);

        TextView judul = new TextView(this);
        judul.setText("🏍️ RR MOTOR NOTA");
        judul.setTextSize(22);
        judul.setGravity(Gravity.CENTER);
        judul.setPadding(0, 5, 0, 10);
        utama.addView(judul);

        TextView subjudul = new TextView(this);
        subjudul.setText("Nota Servis & Penjualan");
        subjudul.setTextSize(14);
        subjudul.setGravity(Gravity.CENTER);
        subjudul.setPadding(0, 0, 0, 10);
        utama.addView(subjudul);

        namaInput = buatInput("Nama pelanggan *");
        utama.addView(namaInput);

        waInput = buatInput("No. WhatsApp *");
        waInput.setInputType(InputType.TYPE_CLASS_PHONE);
        utama.addView(waInput);

        tanggalInput = buatInput("Tanggal nota *");
        tanggalInput.setFocusable(false);
        tanggalInput.setOnClickListener(v -> pilihTanggal());
        utama.addView(tanggalInput);

        motorInput = buatInput("Tipe motor (opsional)");
        utama.addView(motorInput);

        TextView itemTitle = new TextView(this);
        itemTitle.setText("🧾 DAFTAR BARANG / JASA");
        itemTitle.setTextSize(17);
        itemTitle.setPadding(0, 12, 0, 5);
        utama.addView(itemTitle);

        itemContainer = new LinearLayout(this);
        itemContainer.setOrientation(LinearLayout.VERTICAL);
        utama.addView(itemContainer);

        Button tambahItem = new Button(this);
        tambahItem.setText("+ TAMBAH BARANG / JASA");
        tambahItem.setTextSize(13);
        tambahItem.setOnClickListener(v -> tambahBarisItem());
        utama.addView(tambahItem);

        totalText = buatHasilText("TOTAL: Rp0");
        utama.addView(totalText);

        dpInput = buatInput("Uang Muka / Pembayaran");
        dpInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        dpInput.setOnFocusChangeListener(
                (v, focus) -> {
                    if (!focus) {
                        hitungTotal();
                    }
                }
        );
        utama.addView(dpInput);

        sisaText = buatHasilText("SISA PEMBAYARAN: Rp0");
        utama.addView(sisaText);

        statusText = buatHasilText("STATUS: BELUM LUNAS");
        utama.addView(statusText);

        Button simpan = new Button(this);
        simpan.setText("💾 SIMPAN NOTA");
        simpan.setTextSize(13);
        simpan.setOnClickListener(v -> simpanNotaBaru());
        utama.addView(simpan);

        Button riwayat = new Button(this);
        riwayat.setText("📚 LIHAT RIWAYAT NOTA");
        riwayat.setTextSize(13);
        riwayat.setOnClickListener(v -> tampilkanRiwayat());
        utama.addView(riwayat);

        Button whatsapp = new Button(this);
        whatsapp.setText("📱 KIRIM VIA WHATSAPP");
        whatsapp.setTextSize(13);
        whatsapp.setOnClickListener(v -> kirimWhatsApp());
        utama.addView(whatsapp);

        tambahBarisItem();

        setContentView(scroll);
    }

    private EditText buatInput(String hint) {

        EditText edit = new EditText(this);

        edit.setHint(hint);
        edit.setTextSize(14);
        edit.setSingleLine(true);
        edit.setPadding(8, 5, 8, 5);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(0, 3, 0, 3);

        edit.setLayoutParams(lp);

        return edit;
    }

    private TextView buatHasilText(String text) {

        TextView tv = new TextView(this);

        tv.setText(text);
        tv.setTextSize(16);
        tv.setPadding(0, 8, 0, 8);

        return tv;
    }

    private void pilihTanggal() {

        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {

                            Calendar pilih =
                                    Calendar.getInstance();

                            pilih.set(year, month, day);

                            SimpleDateFormat format =
                                    new SimpleDateFormat(
                                            "dd/MM/yyyy",
                                            Locale.getDefault()
                                    );

                            tanggalInput.setText(
                                    format.format(
                                            pilih.getTime()
                                    )
                            );
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    private void tambahBarisItem() {

        LinearLayout baris = new LinearLayout(this);
        baris.setOrientation(LinearLayout.VERTICAL);
        baris.setPadding(0, 5, 0, 5);

        EditText nama =
                buatInput("Nama barang / jasa");

        EditText jumlah =
                buatInput("Jumlah");

        jumlah.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        EditText harga =
                buatInput("Harga satuan");

        harga.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        TextView subtotal =
                new TextView(this);

        subtotal.setText("Subtotal: Rp0");
        subtotal.setTextSize(14);

        Button hapus =
                new Button(this);

        hapus.setText("Hapus item");
        hapus.setTextSize(12);

        baris.addView(nama);
        baris.addView(jumlah);
        baris.addView(harga);
        baris.addView(subtotal);
        baris.addView(hapus);

        itemContainer.addView(baris);

        jumlah.setOnFocusChangeListener(
                (v, focus) -> {
                    if (!focus) hitungTotal();
                }
        );

        harga.setOnFocusChangeListener(
                (v, focus) -> {
                    if (!focus) hitungTotal();
                }
        );

        hapus.setOnClickListener(v -> {

            itemContainer.removeView(baris);

            hitungTotal();
        });

        hitungTotal();
    }

    private long angka(EditText edit) {

        String teks =
                edit.getText()
                        .toString()
                        .replace(".", "")
                        .replace(",", "")
                        .trim();

        if (teks.isEmpty()) {
            return 0;
        }

        try {
            return Long.parseLong(teks);
        } catch (Exception e) {
            return 0;
        }
    }

    private long hitungTotal() {

        long total = 0;

        if (itemContainer == null) {
            return 0;
        }

        for (int i = 0;
             i < itemContainer.getChildCount();
             i++) {

            LinearLayout baris =
                    (LinearLayout)
                            itemContainer.getChildAt(i);

            if (baris.getChildCount() < 5) {
                continue;
            }

            EditText jumlah =
                    (EditText) baris.getChildAt(1);

            EditText harga =
                    (EditText) baris.getChildAt(2);

            TextView subtotal =
                    (TextView) baris.getChildAt(3);

            long qty = angka(jumlah);
            long price = angka(harga);
            long sub = qty * price;

            subtotal.setText(
                    "Subtotal: "
                            + formatRupiah(sub)
            );

            total += sub;
        }

        if (totalText != null) {

            totalText.setText(
                    "TOTAL: "
                            + formatRupiah(total)
            );
        }

        long dp = 0;

        if (dpInput != null) {
            dp = angka(dpInput);
        }

        if (dp > total) {
            dp = total;
        }

        long sisa = total - dp;

        if (sisaText != null) {

            sisaText.setText(
                    "SISA PEMBAYARAN: "
                            + formatRupiah(sisa)
            );
        }

        if (statusText != null) {

            if (total > 0 && sisa == 0) {

                statusText.setText(
                        "STATUS: LUNAS"
                );

            } else {

                statusText.setText(
                        "STATUS: BELUM LUNAS"
                );
            }
        }

        return total;
    }

    private String formatRupiah(long angka) {

        return rupiah
                .format(angka)
                .replace(",00", "");
    }

    private void simpanNotaBaru() {

        if (namaInput.getText()
                .toString()
                .trim()
                .isEmpty()) {

            Toast.makeText(
                    this,
                    "Nama pelanggan wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (waInput.getText()
                .toString()
                .trim()
                .isEmpty()) {

            Toast.makeText(
                    this,
                    "No. WhatsApp wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (tanggalInput.getText()
                .toString()
                .trim()
                .isEmpty()) {

            Toast.makeText(
                    this,
                    "Tanggal nota wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        long total = hitungTotal();

        if (total <= 0) {

            Toast.makeText(
                    this,
                    "Masukkan minimal satu barang/jasa",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String data = buatDataNota();

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        if (!history.isEmpty()) {
            history += "\n";
        }

        history += data;

        prefs.edit()
                .putString(
                        KEY_HISTORY,
                        history
                )
                .apply();

        Toast.makeText(
                this,
                "Nota berhasil disimpan",
                Toast.LENGTH_SHORT
        ).show();
    }

    private String buatDataNota() {

        StringBuilder data =
                new StringBuilder();

        data.append(
                System.currentTimeMillis()
        );

        data.append("|")
                .append(
                        encode(
                                namaInput.getText()
                                        .toString()
                        )
                );

        data.append("|")
                .append(
                        encode(
                                waInput.getText()
                                        .toString()
                        )
                );

        data.append("|")
                .append(
                        encode(
                                tanggalInput.getText()
                                        .toString()
                        )
                );

        data.append("|")
                .append(
                        encode(
                                motorInput.getText()
                                        .toString()
                        )
                );

        data.append("|")
                .append(
                        angka(dpInput)
                );

        data.append("|");

        boolean adaItem = false;

        for (int i = 0;
             i < itemContainer.getChildCount();
             i++) {

            LinearLayout baris =
                    (LinearLayout)
                            itemContainer.getChildAt(i);

            if (baris.getChildCount() < 5) {
                continue;
            }

            EditText nama =
                    (EditText) baris.getChildAt(0);

            EditText jumlah =
                    (EditText) baris.getChildAt(1);

            EditText harga =
                    (EditText) baris.getChildAt(2);

            String n =
                    nama.getText()
                            .toString()
                            .trim();

            if (n.isEmpty()) {
                continue;
            }

            if (adaItem) {
                data.append(";");
            }

            data.append(
                    encode(n)
            );

            data.append("~")
                    .append(angka(jumlah));

            data.append("~")
                    .append(angka(harga));

            adaItem = true;
        }

        return data.toString();
    }

    private void tampilkanRiwayat() {

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                18, 18, 18, 25
        );

        TextView judul =
                new TextView(this);

        judul.setText(
                "📚 RIWAYAT NOTA RR MOTOR"
        );

        judul.setTextSize(21);

        layout.addView(judul);

        if (history.isEmpty()) {

            TextView kosong =
                    new TextView(this);

            kosong.setText(
                    "\nBelum ada riwayat nota."
            );

            kosong.setTextSize(16);

            layout.addView(kosong);

        } else {

            String[] semua =
                    history.split("\n");

            for (int i = semua.length - 1;
                 i >= 0;
                 i--) {

                if (semua[i].trim().isEmpty()) {
                    continue;
                }

                Nota nota =
                        bacaNota(semua[i]);

                if (nota == null) {
                    continue;
                }

                long sisa =
                        nota.total - nota.dp;

                if (sisa < 0) {
                    sisa = 0;
                }

                TextView info =
                        new TextView(this);

                info.setText(
                        "\nPelanggan: "
                                + nota.nama
                                + "\nWhatsApp: "
                                + nota.wa
                                + "\nTanggal: "
                                + nota.tanggal
                                + "\nMotor: "
                                + nota.motor
                                + "\nTotal: "
                                + formatRupiah(
                                        nota.total
                                )
                                + "\nDP/Pembayaran: "
                                + formatRupiah(
                                        nota.dp
                                )
                                + "\nSisa: "
                                + formatRupiah(
                                        sisa
                                )
                                + "\nSTATUS: "
                                + (
                                sisa == 0
                                        ? "LUNAS"
                                        : "BELUM LUNAS"
                        )
                );

                info.setTextSize(15);

                layout.addView(info);

                Button edit =
                        new Button(this);

                edit.setText(
                        "✏️ EDIT / PEMBAYARAN"
                );

                edit.setTextSize(12);

                edit.setOnClickListener(
                        v -> editNota(nota.timestamp)
                );

                layout.addView(edit);

                Button wa =
                        new Button(this);

                wa.setText(
                        "📱 KIRIM NOTA VIA WHATSAPP"
                );

                wa.setTextSize(12);

                wa.setOnClickListener(
                        v -> kirimWhatsAppNota(nota)
                );

                layout.addView(wa);

                TextView garis =
                        new TextView(this);

                garis.setText(
                        "────────────────────"
                );

                layout.addView(garis);
            }
        }

        Button kembali =
                new Button(this);

        kembali.setText("⬅️ KEMBALI");

        kembali.setOnClickListener(
                v -> tampilkanAplikasi()
        );

        layout.addView(kembali);

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(layout);

        setContentView(scroll);
    }

    private Nota bacaNota(String data) {

        try {

            String[] p =
                    data.split(
                            "\\|",
                            -1
                    );

            if (p.length < 7) {
                return null;
            }

            Nota nota = new Nota();

            nota.timestamp =
                    Long.parseLong(p[0]);

            nota.nama =
                    decode(p[1]);

            nota.wa =
                    decode(p[2]);

            nota.tanggal =
                    decode(p[3]);

            nota.motor =
                    decode(p[4]);

            nota.dp =
                    Long.parseLong(p[5]);

            if (!p[6].isEmpty()) {

                String[] semuaItem =
                        p[6].split(";");

                for (String item :
                        semuaItem) {

                    String[] x =
                            item.split(
                                    "~",
                                    -1
                            );

                    if (x.length < 3) {
                        continue;
                    }

                    Item it =
                            new Item();

                    it.nama =
                            decode(x[0]);

                    it.jumlah =
                            Long.parseLong(x[1]);

                    it.harga =
                            Long.parseLong(x[2]);

                    it.subtotal =
                            it.jumlah *
                                    it.harga;

                    nota.items.add(it);

                    nota.total +=
                            it.subtotal;
                }
            }

            return nota;

        } catch (Exception e) {

            return null;
        }
    }

    private void editNota(long timestamp) {

        Nota nota = null;

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        String[] semua =
                history.split("\n");

        for (String data : semua) {

            Nota n =
                    bacaNota(data);

            if (n != null
                    && n.timestamp == timestamp) {

                nota = n;
                break;
            }
        }

        if (nota == null) {

            Toast.makeText(
                    this,
                    "Nota tidak ditemukan",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        tampilkanFormEdit(nota);
    }

    private void tampilkanFormEdit(Nota nota) {

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                18, 18, 18, 25
        );

        scroll.addView(layout);

        TextView judul =
                new TextView(this);

        judul.setText(
                "✏️ EDIT NOTA / PEMBAYARAN"
        );

        judul.setTextSize(21);

        layout.addView(judul);

        EditText nama =
                buatInput("Nama pelanggan");

        nama.setText(nota.nama);

        layout.addView(nama);

        EditText wa =
                buatInput("No. WhatsApp");

        wa.setText(nota.wa);

        wa.setInputType(
                InputType.TYPE_CLASS_PHONE
        );

        layout.addView(wa);

        EditText tanggal =
                buatInput("Tanggal nota");

        tanggal.setText(nota.tanggal);
        tanggal.setFocusable(false);

        layout.addView(tanggal);

        tanggal.setOnClickListener(v -> {

            Calendar cal =
                    Calendar.getInstance();

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            this,
                            (view, year, month, day) -> {

                                Calendar pilih =
                                        Calendar.getInstance();

                                pilih.set(
                                        year,
                                        month,
                                        day
                                );

                                SimpleDateFormat format =
                                        new SimpleDateFormat(
                                                "dd/MM/yyyy",
                                                Locale.getDefault()
                                        );

                                tanggal.setText(
                                        format.format(
                                                pilih.getTime()
                                        )
                                );
                            },
                            cal.get(
                                    Calendar.YEAR
                            ),
                            cal.get(
                                    Calendar.MONTH
                            ),
                            cal.get(
                                    Calendar.DAY_OF_MONTH
                            )
                    );

            dialog.show();
        });

        EditText motor =
                buatInput("Tipe motor");

        motor.setText(nota.motor);

        layout.addView(motor);

        TextView total =
                buatHasilText(
                        "TOTAL: "
                                + formatRupiah(
                                        nota.total
                                )
                );

        layout.addView(total);

        EditText pembayaran =
                buatInput(
                        "Total pembayaran / DP"
                );

        pembayaran.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        pembayaran.setText(
                String.valueOf(nota.dp)
        );

        layout.addView(pembayaran);

        TextView status =
                buatHasilText("");

        layout.addView(status);

        Runnable updateStatus = () -> {

            long
