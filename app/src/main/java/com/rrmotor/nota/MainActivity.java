package com.rrmotor.nota;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    private static final int REQUEST_BLUETOOTH = 1001;

    private static final String PREF_NAME = "RR_MOTOR_NOTA";
    private static final String KEY_HISTORY = "HISTORY";

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

    private long editingTimestamp = -1;

    private String pendingBluetoothText = null;

    private String statusPembayaran = "BELUM LUNAS";

    private final NumberFormat rupiah =
            NumberFormat.getCurrencyInstance(
                    new Locale("id", "ID")
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Supaya keyboard tidak menutupi kolom.
         */
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        prefs = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

        bersihkanRiwayatLama();

        tampilkanAplikasi();
    }

    // =========================================================
    // MENU UTAMA
    // =========================================================

    private void tampilkanAplikasi() {

        editingTimestamp = -1;

        ScrollView scroll = new ScrollView(this);

        scroll.setFillViewport(true);

        LinearLayout utama =
                new LinearLayout(this);

        utama.setOrientation(
                LinearLayout.VERTICAL
        );

        utama.setPadding(
                18,
                12,
                18,
                25
        );

        scroll.addView(utama);

        TextView judul =
                new TextView(this);

        judul.setText(
                "🏍️ RR MOTOR NOTA"
        );

        judul.setTextSize(23);

        judul.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        judul.setGravity(
                Gravity.CENTER
        );

        judul.setPadding(
                0,
                5,
                0,
                5
        );

        utama.addView(judul);

        TextView subjudul =
                new TextView(this);

        subjudul.setText(
                "Nota Servis & Penjualan"
        );

        subjudul.setTextSize(14);

        subjudul.setGravity(
                Gravity.CENTER
        );

        subjudul.setPadding(
                0,
                0,
                0,
                10
        );

        utama.addView(subjudul);

        namaInput =
                buatInput(
                        "Nama pelanggan *"
                );

        utama.addView(namaInput);

        waInput =
                buatInput(
                        "No. WhatsApp *"
                );

        waInput.setInputType(
                InputType.TYPE_CLASS_PHONE
        );

        utama.addView(waInput);

        tanggalInput =
                buatInput(
                        "Tanggal nota *"
                );

        tanggalInput.setFocusable(false);

        tanggalInput.setOnClickListener(
                v -> pilihTanggal()
        );

        utama.addView(tanggalInput);

        motorInput =
                buatInput(
                        "Tipe motor (opsional)"
                );

        utama.addView(motorInput);

        TextView itemTitle =
                new TextView(this);

        itemTitle.setText(
                "🧾 DAFTAR BARANG / JASA"
        );

        itemTitle.setTextSize(17);

        itemTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        itemTitle.setPadding(
                0,
                12,
                0,
                5
        );

        utama.addView(itemTitle);

        itemContainer =
                new LinearLayout(this);

        itemContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        utama.addView(itemContainer);

        Button tambahItem =
                buatTombol(
                        "+ TAMBAH BARANG / JASA"
                );

        tambahItem.setOnClickListener(
                v -> tambahBarisItem()
        );

        utama.addView(tambahItem);

        totalText =
                buatHasilText(
                        "TOTAL: Rp0"
                );

        utama.addView(totalText);

        dpInput =
                buatInput(
                        "Uang Muka / DP"
                );

        dpInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        dpInput.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (!hasFocus) {
                        hitungTotal();
                    }
                }
        );

        utama.addView(dpInput);

        sisaText =
                buatHasilText(
                        "SISA PEMBAYARAN: Rp0"
                );

        utama.addView(sisaText);

        statusText =
                buatHasilText(
                        "STATUS: BELUM LUNAS"
                );

        utama.addView(statusText);

        Button statusButton =
                buatTombol(
                        "💰 UBAH STATUS PEMBAYARAN"
                );

        statusButton.setOnClickListener(
                v -> pilihStatus()
        );

        utama.addView(statusButton);

        Button simpan =
                buatTombol(
                        "💾 SIMPAN NOTA"
                );

        simpan.setOnClickListener(
                v -> simpanNota()
        );

        utama.addView(simpan);

        Button cetak =
                buatTombol(
                        "🖨️ CETAK NOTA"
                );

        cetak.setOnClickListener(
                v -> cetakNotaSekarang()
        );

        utama.addView(cetak);

        Button bluetooth =
                buatTombol(
                        "🔵 CETAK BLUETOOTH"
                );

        bluetooth.setOnClickListener(
                v -> cetakBluetoothSekarang()
        );

        utama.addView(bluetooth);

        Button riwayat =
                buatTombol(
                        "📚 LIHAT RIWAYAT NOTA"
                );

        riwayat.setOnClickListener(
                v -> tampilkanRiwayat()
        );

        utama.addView(riwayat);

        Button whatsapp =
                buatTombol(
                        "📱 KIRIM VIA WHATSAPP"
                );

        whatsapp.setOnClickListener(
                v -> kirimWhatsApp()
        );

        utama.addView(whatsapp);

        tambahBarisItem();

        setContentView(scroll);

        /*
         * Fokus awal tidak langsung membuka keyboard.
         */
        scroll.post(() ->
                scroll.scrollTo(0, 0)
        );
    }

    // =========================================================
    // INPUT & TOMBOL
    // =========================================================

    private EditText buatInput(String hint) {

        EditText edit =
                new EditText(this);

        edit.setHint(hint);

        edit.setTextSize(15);

        edit.setSingleLine(true);

        edit.setPadding(
                10,
                5,
                10,
                5
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(
                0,
                3,
                0,
                3
        );

        edit.setLayoutParams(lp);

        return edit;
    }

    private Button buatTombol(String text) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(14);

        button.setAllCaps(false);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(
                0,
                3,
                0,
                3
        );

        button.setLayoutParams(lp);

        return button;
    }

    private TextView buatHasilText(
            String text
    ) {

        TextView tv =
                new TextView(this);

        tv.setText(text);

        tv.setTextSize(17);

        tv.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        tv.setPadding(
                0,
                8,
                0,
                8
        );

        return tv;
    }

    // =========================================================
    // TANGGAL
    // =========================================================

    private void pilihTanggal() {

        Calendar cal =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, day) -> {

                            Calendar dipilih =
                                    Calendar.getInstance();

                            dipilih.set(
                                    year,
                                    month,
                                    day
                            );

                            SimpleDateFormat format =
                                    new SimpleDateFormat(
                                            "dd/MM/yyyy",
                                            Locale.getDefault()
                                    );

                            tanggalInput.setText(
                                    format.format(
                                            dipilih.getTime()
                                    )
                            );
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    // =========================================================
    // BARANG / JASA
    // =========================================================

    private void tambahBarisItem() {

        LinearLayout baris =
                new LinearLayout(this);

        baris.setOrientation(
                LinearLayout.VERTICAL
        );

        baris.setPadding(
                0,
                4,
                0,
                4
        );

        EditText nama =
                buatInput(
                        "Nama barang / jasa"
                );

        EditText jumlah =
                buatInput(
                        "Jumlah"
                );

        jumlah.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        EditText harga =
                buatInput(
                        "Harga satuan"
                );

        harga.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        TextView subtotal =
                new TextView(this);

        subtotal.setText(
                "Subtotal: Rp0"
        );

        subtotal.setTextSize(14);

        Button hapus =
                buatTombol(
                        "❌ Hapus item"
                );

        baris.addView(nama);
        baris.addView(jumlah);
        baris.addView(harga);
        baris.addView(subtotal);
        baris.addView(hapus);

        itemContainer.addView(
                baris
        );

        View.OnFocusChangeListener listener =
                (v, hasFocus) -> {

                    if (!hasFocus) {
                        hitungTotal();
                    }
                };

        jumlah.setOnFocusChangeListener(
                listener
        );

        harga.setOnFocusChangeListener(
                listener
        );

        jumlah.setOnKeyListener(
                (v, keyCode, event) -> {
                    hitungTotal();
                    return false;
                }
        );

        harga.setOnKeyListener(
                (v, keyCode, event) -> {
                    hitungTotal();
                    return false;
                }
        );

        hapus.setOnClickListener(v -> {

            itemContainer.removeView(
                    baris
            );

            hitungTotal();
        });

        hitungTotal();
    }

    // =========================================================
    // HITUNG
    // =========================================================

    private long angka(EditText edit) {

        if (edit == null) {
            return 0;
        }

        String teks =
                edit.getText()
                        .toString()
                        .replace(
                                ".",
                                ""
                        )
                        .replace(
                                ",",
                                ""
                        )
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

        for (
                int i = 0;
                i < itemContainer.getChildCount();
                i++
        ) {

            View view =
                    itemContainer.getChildAt(i);

            if (!(view instanceof LinearLayout)) {
                continue;
            }

            LinearLayout baris =
                    (LinearLayout) view;

            if (baris.getChildCount() < 5) {
                continue;
            }

            EditText jumlah =
                    (EditText)
                            baris.getChildAt(1);

            EditText harga =
                    (EditText)
                            baris.getChildAt(2);

            TextView subtotal =
                    (TextView)
                            baris.getChildAt(3);

            long qty =
                    angka(jumlah);

            long price =
                    angka(harga);

            long sub =
                    qty * price;

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

        long dp =
                angka(dpInput);

        if (dp > total) {
            dp = total;
        }

        long sisa =
                total - dp;

        if (sisaText != null) {

            sisaText.setText(
                    "SISA PEMBAYARAN: "
                            + formatRupiah(sisa)
            );
        }

        /*
         * Status tidak otomatis dipaksa.
         * Status bisa dipilih pengguna.
         */

        tampilkanStatus();

        return total;
    }

    private void tampilkanStatus() {

        if (statusText == null) {
            return;
        }

        statusText.setText(
                "STATUS: "
                        + statusPembayaran
        );
    }

    private String formatRupiah(long angka) {

        return rupiah
                .format(angka)
                .replace(
                        ",00",
                        ""
                );
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void pilihStatus() {

        String[] pilihan = {
                "BELUM LUNAS",
                "LUNAS"
        };

        int checked =
                statusPembayaran.equals(
                        "LUNAS"
                )
                        ? 1
                        : 0;

        new AlertDialog.Builder(this)
                .setTitle(
                        "Status Pembayaran"
                )
                .setSingleChoiceItems(
                        pilihan,
                        checked,
                        (dialog, which) -> {

                            statusPembayaran =
                                    pilihan[which];

                            tampilkanStatus();

                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        "Batal",
                        null
                )
                .show();
    }

    // =========================================================
    // SIMPAN NOTA
    // =========================================================

    private void simpanNota() {

        String nama =
                namaInput
                        .getText()
                        .toString()
                        .trim();

        String wa =
                waInput
                        .getText()
                        .toString()
                        .trim();

        String tanggal =
                tanggalInput
                        .getText()
                        .toString()
                        .trim();

        if (nama.isEmpty()) {

            Toast.makeText(
                    this,
                    "Nama pelanggan wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (wa.isEmpty()) {

            Toast.makeText(
                    this,
                    "No. WhatsApp wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (tanggal.isEmpty()) {

            Toast.makeText(
                    this,
                    "Tanggal nota wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        long total =
                hitungTotal();

        if (total <= 0) {

            Toast.makeText(
                    this,
                    "Masukkan minimal satu barang/jasa",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (editingTimestamp != -1) {

            perbaruiNota();

        } else {

            tambahNotaBaru();
        }
    }

    private void tambahNotaBaru() {

        String data =
                buatDataNota(
                        System.currentTimeMillis()
                );

        String lama =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        if (!lama.isEmpty()) {
            lama += "\n";
        }

        lama += data;

        prefs.edit()
                .putString(
                        KEY_HISTORY,
                        lama
                )
                .apply();

        Toast.makeText(
                this,
                "Nota berhasil disimpan",
                Toast.LENGTH_SHORT
        ).show();

        bersihkanForm();

        tampilkanAplikasi();
    }

    private void perbaruiNota() {

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        StringBuilder baru =
                new StringBuilder();

        String[] semua =
                history.split(
                        "\n"
                );

        String dataBaru =
                buatDataNota(
                        editingTimestamp
                );

        boolean ditemukan = false;

        for (String data : semua) {

            if (data.trim().isEmpty()) {
                continue;
            }

            try {

                String[] p =
                        data.split(
                                "\\|",
                                -1
                        );

                long timestamp =
                        Long.parseLong(
                                p[0]
                        );

                if (timestamp ==
                        editingTimestamp) {

                    if (baru.length() > 0) {
                        baru.append("\n");
                    }

                    baru.append(
                            dataBaru
                    );

                    ditemukan = true;

                } else {

                    if (baru.length() > 0) {
                        baru.append("\n");
                    }

                    baru.append(data);
                }

            } catch (Exception e) {

                if (baru.length() > 0) {
                    baru.append("\n");
                }

                baru.append(data);
            }
        }

        if (!ditemukan) {

            Toast.makeText(
                    this,
                    "Nota lama tidak ditemukan",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        prefs.edit()
                .putString(
                        KEY_HISTORY,
                        baru.toString()
                )
                .apply();

        Toast.makeText(
                this,
                "Nota berhasil diperbarui",
                Toast.LENGTH_SHORT
        ).show();

        tampilkanRiwayat();
    }

    // =========================================================
    // DATA NOTA
    // =========================================================

    private String buatDataNota(
            long timestamp
    ) {

        String nama =
                encode(
                        namaInput
                                .getText()
                                .toString()
                );

        String wa =
                encode(
                        waInput
                                .getText()
                                .toString()
                );

        String tanggal =
                encode(
                        tanggalInput
                                .getText()
                                .toString()
                );

        String motor =
                encode(
                        motorInput
                                .getText()
                                .toString()
                );

        long total =
                hitungTotal();

        long dp =
                angka(dpInput);

        if (dp > total) {
            dp = total;
        }

        StringBuilder itemData =
                new StringBuilder();

        for (
                int i = 0;
                i < itemContainer.getChildCount();
                i++
        ) {

            View view =
                    itemContainer.getChildAt(i);

            if (!(view instanceof LinearLayout)) {
                continue;
            }

            LinearLayout baris =
                    (LinearLayout) view;

            if (baris.getChildCount() < 5) {
                continue;
            }

            EditText namaBarang =
                    (EditText)
                            baris.getChildAt(0);

            EditText jumlah =
                    (EditText)
                            baris.getChildAt(1);

            EditText harga =
                    (EditText)
                            baris.getChildAt(2);

            String nb =
                    namaBarang
                            .getText()
                            .toString()
                            .trim();

            if (nb.isEmpty()) {
                continue;
            }

            if (itemData.length() > 0) {
                itemData.append(";");
            }

            itemData
                    .append(
                            encode(nb)
                    )
                    .append("~")
                    .append(
                            angka(jumlah)
                    )
                    .append("~")
                    .append(
                            angka(harga)
                    );
        }

        /*
         * Format baru:
         *
         * timestamp
         * nama
         * wa
         * tanggal
         * motor
         * dp
         * item
         * status
         */

        return timestamp
                + "|" + nama
                + "|" + wa
                + "|" + tanggal
                + "|" + motor
                + "|" + dp
                + "|" + itemData
                + "|" + encode(
                        statusPembayaran
                );
    }

    // =========================================================
    // RIWAYAT
    // =========================================================

    private void tampilkanRiwayat() {

        bersihkanRiwayatLama();

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                18,
                15,
                18,
                25
        );

        scroll.addView(layout);

        TextView judul =
                new TextView(this);

        judul.setText(
                "📚 RIWAYAT NOTA RR MOTOR"
        );

        judul.setTextSize(21);

        judul.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        layout.addView(judul);

        /*
         * Tombol kembali ke menu utama.
         */

        Button kembali =
                buatTombol(
                        "⬅️ KEMBALI KE MENU UTAMA"
                );

        kembali.setOnClickListener(
                v -> tampilkanAplikasi()
        );

        layout.addView(kembali);

        if (history.isEmpty()) {

            TextView kosong =
                    new TextView(this);

            kosong.setText(
                    "\nBelum ada riwayat nota."
            );

            kosong.setTextSize(17);

            layout.addView(kosong);

        } else {

            String[] semua =
                    history.split(
                            "\n"
                    );

            for (String data : semua) {

                if (data.trim().isEmpty()) {
                    continue;
                }

                Nota nota =
                        bacaNota(data);

                if (nota == null) {
                    continue;
                }

                long sisa =
                        nota.total
                                - nota.dp;

                if (sisa < 0) {
                    sisa = 0;
                }

                TextView info =
                        new TextView(this);

                info.setText(
                        "\n"
                                + "👤 Pelanggan: "
                                + nota.nama
                                + "\n"
                                + "📱 WhatsApp: "
                                + nota.wa
                                + "\n"
                                + "📅 Tanggal: "
                                + nota.tanggal
                                + "\n"
                                + "🏍️ Motor: "
                                + nota.motor
                                + "\n"
                                + "💵 Total: "
                                + formatRupiah(
                                        nota.total
                                )
                                + "\n"
                                + "💰 DP: "
                                + formatRupiah(
                                        nota.dp
                                )
                                + "\n"
                                + "💳 Sisa: "
                                + formatRupiah(
                                        sisa
                                )
                                + "\n"
                                + "📌 Status: "
                                + nota.status
                );

                info.setTextSize(15);

                info.setPadding(
                        0,
                        8,
                        0,
                        5
                );

                layout.addView(info);

                /*
                 * EDIT
                 */

                Button edit =
                        buatTombol(
                                "✏️ EDIT NOTA"
                        );

                edit.setOnClickListener(
                        v -> editNota(nota)
                );

                layout.addView(edit);

                /*
                 * CETAK
                 */

                Button cetak =
                        buatTombol(
                                "🖨️ CETAK NOTA INI"
                        );

                cetak.setOnClickListener(
                        v -> cetakDataNota(nota)
                );

                layout.addView(cetak);

                /*
                 * BLUETOOTH
                 */

                Button cetakBT =
                        buatTombol(
                                "🔵 CETAK BLUETOOTH"
                        );

                cetakBT.setOnClickListener(
                        v -> cetakBluetooth(nota)
                );

                layout.addView(cetakBT);

                /*
                 * HAPUS
                 */

                Button hapus =
                        buatTombol(
                                "🗑️ HAPUS NOTA"
                        );

                hapus.setOnClickListener(
                        v -> konfirmasiHapus(
                                nota.timestamp
                        )
                );

                layout.addView(hapus);

                View garis =
                        new View(this);

                LinearLayout.LayoutParams gp =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2
                        );

                gp.setMargins(
                        0,
                        15,
                        0,
                        15
                );

                garis.setLayoutParams(gp);

                layout.addView(garis);
            }
        }

        setContentView(scroll);
    }

    // =========================================================
    // EDIT NOTA
    // =========================================================

    private void editNota(Nota nota) {

        editingTimestamp =
                nota.timestamp;

        namaInput =
                null;

        tampilkanFormEdit(nota);
    }

    private void tampilkanFormEdit(
            Nota nota
    ) {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        LinearLayout utama =
                new LinearLayout(this);

        utama.setOrientation(
                LinearLayout.VERTICAL
        );

        utama.setPadding(
                18,
                15,
                18,
                25
        );

        scroll.addView(utama);

        TextView judul =
                new TextView(this);

        judul.setText(
                "✏️ EDIT NOTA"
        );

        judul.setTextSize(22);

        judul.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        utama.addView(judul);

        namaInput =
                buatInput(
                        "Nama pelanggan *"
                );

        namaInput.setText(
                nota.nama
        );

        utama.addView(namaInput);

        waInput =
                buatInput(
                        "No. WhatsApp *"
                );

        waInput.setInputType(
                InputType.TYPE_CLASS_PHONE
        );

        waInput.setText(
                nota.wa
        );

        utama.addView(waInput);

        tanggalInput =
                buatInput(
                        "Tanggal nota *"
                );

        tanggalInput.setFocusable(false);

        tanggalInput.setText(
                nota.tanggal
        );

        tanggalInput.setOnClickListener(
                v -> pilihTanggal()
        );

        utama.addView(tanggalInput);

        motorInput =
                buatInput(
                        "Tipe motor"
                );

        motorInput.setText(
                nota.motor
        );

        utama.addView(motorInput);

        TextView itemTitle =
                new TextView(this);

        itemTitle.setText(
                "🧾 BARANG / JASA"
        );

        itemTitle.setTextSize(17);

        itemTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        itemTitle.setPadding(
                0,
                12,
                0,
                5
        );

        utama.addView(itemTitle);

        itemContainer =
                new LinearLayout(this);

        itemContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        utama.addView(
                itemContainer
        );

        Button tambah =
                buatTombol(
                        "+ TAMBAH BARANG / JASA"
                );

        tambah.setOnClickListener(
                v -> tambahBarisItem()
        );

        utama.addView(tambah);

        totalText =
                buatHasilText(
                        "TOTAL: Rp0"
                );

        utama.addView(totalText);

        dpInput =
                buatInput(
                        "Uang Muka / DP"
                );

        dpInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        dpInput.setText(
                String.valueOf(nota.dp)
        );

        utama.addView(dpInput);

        sisaText =
                buatHasilText(
                        "SISA PEMBAYARAN: Rp0"
                );

        utama.addView(sisaText);

        statusPembayaran =
                nota.status;

        statusText =
                buatHasilText(
                        "STATUS: "
                                + statusPembayaran
                );

        utama.addView(statusText);

        Button statusButton =
                buatTombol(
                        "💰 UBAH STATUS PEMBAYARAN"
                );

        statusButton.setOnClickListener(
                v -> pilihStatus()
        );

        utama.addView(statusButton);

        Button simpan =
                buatTombol(
                        "💾 SIMPAN PERUBAHAN"
                );

        simpan.setOnClickListener(
                v -> simpanNota()
        );

        utama.addView(simpan);

        Button batal =
                buatTombol(
                        "⬅️ BATAL / KEMBALI"
                );

        batal.setOnClickListener(
                v -> tampilkanRiwayat()
        );

        utama.addView(batal);

        /*
         * Masukkan barang lama.
         */

        if (nota.items.isEmpty()) {

            tambahBarisItem();

        } else {

            for (Item it :
                    nota.items) {

                tambahBarisItem();

                int index =
                        itemContainer
                                .getChildCount()
                                - 1;

                LinearLayout baris =
                        (LinearLayout)
                                itemContainer
                                        .getChildAt(
                                                index
                                        );

                EditText nama =
                        (EditText)
                                baris.getChildAt(
                                        0
                                );

                EditText jumlah =
                        (EditText)
                                baris.getChildAt(
                                        1
                                );

                EditText harga =
                        (EditText)
                                baris.getChildAt(
                                        2
                                );

                nama.setText(
                        it.nama
                );

                jumlah.setText(
                        String.valueOf(
                                it.jumlah
                        )
                );

                harga.setText(
                        String.valueOf(
                                it.harga
                        )
                );
            }
        }

        setContentView(scroll);

        hitungTotal();

        /*
         * Jangan langsung munculkan keyboard.
         */

        scroll.post(() ->
                scroll.scrollTo(
                        0,
                        0
                )
        );
    }

    // =========================================================
    // BACA NOTA
    // =========================================================

    private Nota bacaNota(
            String data
    ) {

        try {

            String[] p =
                    data.split(
                            "\\|",
                            -1
                    );

            if (p.length < 7) {
                return null;
            }

            Nota nota =
                    new Nota();

            nota.timestamp =
                    Long.parseLong(
                            p[0]
                    );

            nota.nama =
                    decode(p[1]);

            nota.wa =
                    decode(p[2]);

            nota.tanggal =
                    decode(p[3]);

            nota.motor =
                    decode(p[4]);

            nota.dp =
                    Long.parseLong(
                            p[5]
                    );

            nota.items.clear();

            if (!p[6].isEmpty()) {

                String[] semuaItem =
                        p[6].split(
                                ";"
                        );

                for (
                        String item :
                        semuaItem
                ) {

                    String[] x =
                            item.split(
                                    "~",
                                    -1
                            );

                    if (x.length >= 3) {

                        Item it =
                                new Item();

                        it.nama =
                                decode(
                                        x[0]
                                );

                        it.jumlah =
                                Long.parseLong(
                                        x[1]
                                );

                        it.harga =
                                Long.parseLong(
                                        x[2]
                                );

                        it.subtotal =
                                it.jumlah
                                        * it.harga;

                        nota.items.add(
                                it
                        );

                        nota.total +=
                                it.subtotal;
                    }
                }
            }

            /*
             * Nota lama yang belum punya status
             * tetap bisa dibaca.
             */

            if (p.length >= 8
                    && !p[7].isEmpty()) {

                nota.status =
                        decode(p[7]);

            } else {

                if (nota.total > 0
                        && nota.dp >= nota.total) {

                    nota.status =
                            "LUNAS";

                } else {

                    nota.status =
                            "BELUM LUNAS";
                }
            }

            return nota;

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // HAPUS NOTA
    // =========================================================

    private void konfirmasiHapus(
            long timestamp
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Hapus nota?"
                )
                .setMessage(
                        "Nota ini akan dihapus dari riwayat."
                )
                .setPositiveButton(
                        "HAPUS",
                        (dialog, which) ->
                                hapusNota(timestamp)
                )
                .setNegativeButton(
                        "BATAL",
                        null
                )
                .show();
    }

    private void hapusNota(
            long timestamp
    ) {

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        StringBuilder baru =
                new StringBuilder();

        String[] semua =
                history.split(
                        "\n"
                );

        for (String data : semua) {

            try {

                String[] p =
                        data.split(
                                "\\|",
                                -1
                        );

                long waktu =
                        Long.parseLong(
                                p[0]
                        );

                if (waktu ==
                        timestamp) {

                    continue;
                }

                if (baru.length() > 0) {
                    baru.append("\n");
                }

                baru.append(data);

            } catch (Exception e) {

                if (baru.length() > 0) {
                    baru.append("\n");
                }

                baru.append(data);
            }
        }

        prefs.edit()
                .putString(
                        KEY_HISTORY,
                        baru.toString()
                )
                .apply();

        Toast.makeText(
                this,
                "Nota dihapus",
                Toast.LENGTH_SHORT
        ).show();

        tampilkanRiwayat();
    }

    // =========================================================
    // CETAK ANDROID
    // =========================================================

    private void cetakNotaSekarang() {

        String isi =
                buatIsiNota();

        cetakDenganAndroid(
                isi
        );
    }

    private void cetakDataNota(
            Nota nota
    ) {

        String isi =
                buatIsiNota(nota);

        cetakDenganAndroid(
                isi
        );
    }

    private void cetakDenganAndroid(
            String isi
    ) {

        PrintManager printManager =
                (PrintManager)
                        getSystemService(
                                PRINT_SERVICE
                        );

        if (printManager == null) {

            Toast.makeText(
                    this,
                    "Fitur cetak tidak tersedia.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        try {

            PrintAttributes.MediaSize ukuran =
                    PrintAttributes.MediaSize.ISO_A4;

            printManager.print(
                    "Nota RR MOTOR",
                    new NotaPrintAdapter(
                            this,
                            isi
                    ),
                    new PrintAttributes.Builder()
                            .setMediaSize(
                                    ukuran
                            )
                            .setMinMargins(
                                    PrintAttributes.Margins.NO_MARGINS
                            )
                            .build()
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Tidak dapat membuka cetak: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // ISI NOTA
    // =========================================================

    private String buatIsiNota() {

        long total =
                hitungTotal();

        long dp =
                angka(dpInput);

        if (dp > total) {
            dp = total;
        }

        long sisa =
                total - dp;

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "====================\n"
        );

        sb.append(
                "       RR MOTOR\n"
        );

        sb.append(
                "    NOTA SERVIS\n"
        );

        sb.append(
                "====================\n"
        );

        sb.append(
                "Nama : "
                        + namaInput
                        .getText()
                        .toString()
                        .trim()
                        + "\n"
        );

        sb.append(
                "WA   : "
                        + waInput
                        .getText()
                        .toString()
                        .trim()
                        + "\n"
        );

        sb.append(
                "Tanggal : "
                        + tanggalInput
                        .getText()
                        .toString()
                        .trim()
                        + "\n"
        );

        String motor =
                motorInput
                        .getText()
                        .toString()
                        .trim();

        if (!motor.isEmpty()) {

            sb.append(
                    "Motor : "
                            + motor
                            + "\n"
            );
        }

        sb.append(
                "--------------------\n"
        );

        tambahkanBarangKeNota(
                sb
        );

        sb.append(
                "--------------------\n"
        );

        sb.append(
                "TOTAL : "
                        + formatRupiah(total)
                        + "\n"
        );

        sb.append(
                "DP    : "
                        + formatRupiah(dp)
                        + "\n"
        );

        sb.append(
                "SISA  : "
                        + formatRupiah(sisa)
                        + "\n"
        );

        sb.append(
                "STATUS: "
                        + statusPembayaran
                        + "\n"
        );

        sb.append(
                "====================\n"
        );

        sb.append(
                "Terima kasih\n"
        );

        return sb.toString();
    }

    private void tambahkanBarangKeNota(
            StringBuilder sb
    ) {

        for (
                int i = 0;
                i < itemContainer.getChildCount();
                i++
        ) {

            View view =
                    itemContainer.getChildAt(i);

            if (!(view instanceof LinearLayout)) {
                continue;
            }

            LinearLayout baris =
                    (LinearLayout) view;

            if (baris.getChildCount() < 5) {
                continue;
            }

            EditText nama =
                    (EditText)
                            baris.getChildAt(0);

            EditText jumlah =
                    (EditText)
                            baris.getChildAt(1);

            EditText harga =
                    (EditText)
                            baris.getChildAt(2);

            String n =
                    nama.getText()
                            .toString()
                            .trim();

            if (n.isEmpty()) {
                continue;
            }

            long q =
                    angka(jumlah);

            long h =
                    angka(harga);

            long sub =
                    q * h;

            sb.append(
                    n
                            + "\n"
            );

            sb.append(
                    q
                            + " x "
                            + formatRupiah(h)
                            + " = "
                            + formatRupiah(sub)
                            + "\n"
            );
        }
    }

    private String buatIsiNota(
            Nota nota
    ) {

        long sisa =
                nota.total
                        - nota.dp;

        if (sisa < 0) {
            sisa = 0;
        }

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "====================\n"
        );

        sb.append(
                "       RR MOTOR\n"
        );

        sb.append(
                "    NOTA SERVIS\n"
        );

        sb.append(
                "====================\n"
        );

        sb.append(
                "Nama : "
                        + nota.nama
                        + "\n"
        );

        sb.append(
                "WA   : "
                        + nota.wa
                        + "\n"
        );

        sb.append(
                "Tanggal : "
                        + nota.tanggal
                        + "\n"
        );

        if (nota.motor != null
                && !nota.motor.isEmpty()) {

            sb.append(
                    "Motor : "
                            + nota.motor
                            + "\n"
            );
        }

        sb.append(
                "--------------------\n"
        );

        for (Item it :
                nota.items) {

            sb.append(
                    it.nama
                            + "\n"
            );

            sb.append(
                    it.jumlah
                            + " x "
                            + formatRupiah(
                                    it.harga
                            )
                            + " = "
                            + formatRupiah(
                                    it.subtotal
                            )
                            + "\n"
            );
        }

        sb.append(
                "--------------------\n"
        );

        sb.append(
                "TOTAL : "
                        + formatRupiah(
                                nota.total
                        )
                        + "\n"
        );

        sb.append(
                "DP    : "
                        + formatRupiah(
                                nota.dp
                        )
                        + "\n"
        );

        sb.append(
                "SISA  : "
                        + formatRupiah(
                                sisa
                        )
                        + "\n"
        );

        sb.append(
                "STATUS: "
                        + nota.status
                        + "\n"
        );

        sb.append(
                "====================\n"
        );

        sb.append(
                "Terima kasih\n"
        );

        return sb.toString();
    }

    // =========================================================
    // BLUETOOTH
    // =========================================================

    private void cetakBluetoothSekarang() {

        if (!cekIzinBluetooth()) {

            pendingBluetoothText =
                    buatIsiNota();

            return;
        }

        tampilkanDaftarPrinter(
                buatIsiNota()
        );
    }

    private void cetakBluetooth(
            Nota nota
    ) {

        if (!cekIzinBluetooth()) {

            pendingBluetoothText =
                    buatIsiNota(nota);

            return;
        }

        tampilkanDaftarPrinter(
                buatIsiNota(nota)
        );
    }

    private boolean cekIzinBluetooth() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S) {

            boolean connect =
                    checkSelfPermission(
                            Manifest.permission.BLUETOOTH_CONNECT
                    )
                            == PackageManager.PERMISSION_GRANTED;

            boolean scan =
                    checkSelfPermission(
                            Manifest.permission.BLUETOOTH_SCAN
                    )
                            == PackageManager.PERMISSION_GRANTED;

            if (!connect || !scan) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH
                );

                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode !=
                REQUEST_BLUETOOTH) {
            return;
        }

        boolean semua =
                grantResults.length > 0;

        for (int hasil :
                grantResults) {

            if (hasil !=
                    PackageManager.PERMISSION_GRANTED) {

                semua = false;
                break;
            }
        }

        if (semua) {

            Toast.makeText(
                    this,
                    "Bluetooth diizinkan.",
                    Toast.LENGTH_SHORT
            ).show();

            if (pendingBluetoothText != null) {

                String isi =
                        pendingBluetoothText;

                pendingBluetoothText =
                        null;

                tampilkanDaftarPrinter(
                        isi
                );
            }

        } else {

            Toast.makeText(
                    this,
                    "Izin Perangkat di sekitar diperlukan untuk printer Bluetooth.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void tampilkanDaftarPrinter(
            String isi
    ) {

        BluetoothAdapter adapter =
                BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {

            Toast.makeText(
                    this,
                    "HP tidak mendukung Bluetooth.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        try {

            if (!adapter.isEnabled()) {

                Intent intent =
                        new Intent(
                                BluetoothAdapter.ACTION_REQUEST_ENABLE
                        );

                startActivity(intent);

                Toast.makeText(
                        this,
                        "Aktifkan Bluetooth lalu tekan cetak Bluetooth lagi.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

        } catch (SecurityException e) {

            Toast.makeText(
                    this,
                    "Izin Bluetooth belum diberikan.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Set<BluetoothDevice> devices;

        try {

            devices =
                    adapter.getBondedDevices();

        } catch (SecurityException e) {

            Toast.makeText(
                    this,
                    "Izin Perangkat di sekitar belum diberikan.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (devices == null
                || devices.isEmpty()) {

            Toast.makeText(
                    this,
                    "Pasangkan printer Bluetooth terlebih dahulu di Pengaturan HP.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        ArrayList<BluetoothDevice> daftar =
                new ArrayList<>(
                        devices
                );

        String[] namaPrinter =
                new String[
                        daftar.size()
                ];

        for (
                int i = 0;
                i < daftar.size();
                i++
        ) {

            BluetoothDevice device =
                    daftar.get(i);

            String nama =
                    "Printer Bluetooth";

            try {

                if (device.getName() != null) {

                    nama =
                            device.getName();
                }

            } catch (SecurityException ignored) {
            }

            namaPrinter[i] =
                    nama;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Pilih Printer Bluetooth"
                )
                .setItems(
                        namaPrinter,
                        (dialog, which) -> {

                            BluetoothDevice device =
                                    daftar.get(
                                            which
                                    );

                            cetakKePrinter(
                                    device,
                                    isi
                            );
                        }
                )
                .setNegativeButton(
                        "Batal",
                        null
                )
                .show();
    }

    private void cetakKePrinter(
            BluetoothDevice device,
            String isi
    ) {

        new Thread(() -> {

            OutputStream output =
                    null;

            android.bluetooth.BluetoothSocket socket =
                    null;

            try {

                if (Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.S) {

                    if (checkSelfPermission(
                            Manifest.permission.BLUETOOTH_CONNECT
                    )
                            != PackageManager.PERMISSION_GRANTED) {

                        throw new SecurityException(
                                "Izin Bluetooth belum diberikan"
                        );
                    }
                }

                UUID uuid =
                        UUID.fromString(
                                "00001101-0000-1000-8000-00805F9B34FB"
                        );

                socket =
                        device.createRfcommSocketToServiceRecord(
                                uuid
                        );

                socket.connect();

                output =
                        socket.getOutputStream();

                /*
                 * Reset printer.
                 */

                output.write(
                        new byte[]{
                                0x1B,
                                0x40
                        }
                );

                /*
                 * Ukuran tulisan normal.
                 */

                output.write(
                        new byte[]{
                                0x1B,
                                0x21,
                                0x00
                        }
                );

                output.write(
                        isi.getBytes(
                                Charset.forName(
                                        "UTF-8"
                                )
                        )
                );

                output.write(
                        new byte[]{
                                0x0A,
                                0x0A,
                                0x0A
                        }
                );

                output.flush();

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Nota berhasil dikirim ke printer.",
                                Toast.LENGTH_LONG
                        ).show()
                );

            } catch (Exception e) {

                String pesan =
                        e.getMessage();

                if (pesan == null
                        || pesan.isEmpty()) {

                    pesan =
                            "Tidak dapat terhubung ke printer.";
                }

                String finalPesan =
                        pesan;

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Gagal mencetak: "
                                        + finalPesan,
                                Toast.LENGTH_LONG
                        ).show()
                );

            } finally {

                try {

                    if (output != null) {
                        output.close();
                    }

                } catch (Exception ignored) {
                }

                try {

                    if (socket != null) {
                        socket.close();
                    }

                } catch (Exception ignored) {
                }
            }

        }).start();
    }

    // =========================================================
    // WHATSAPP
    // =========================================================

    private void kirimWhatsApp() {

        String nomor =
                waInput
                        .getText()
                        .toString()
                        .trim();

        if (nomor.isEmpty()) {

            Toast.makeText(
                    this,
                    "No. WhatsApp wajib diisi.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        nomor =
                bersihkanNomor(
                        nomor
                );

        String pesan =
                buatIsiNota();

        try {

            String url =
                    "https://wa.me/"
                            + nomor
                            + "?text="
                            + URLEncoder.encode(
                                    pesan,
                                    "UTF-8"
                            );

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW
                    );

            intent.setData(
                    android.net.Uri.parse(
                            url
                    )
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "WhatsApp tidak dapat dibuka.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String bersihkanNomor(
            String nomor
    ) {

        nomor =
                nomor.replaceAll(
                        "[^0-9+]",
                        ""
                );

        if (nomor.startsWith("+")) {

            nomor =
                    nomor.substring(1);
        }

        if (nomor.startsWith("0")) {

            nomor =
                    "62"
                            + nomor.substring(1);
        }

        return nomor;
    }

    // =========================================================
    // BERSIHKAN RIWAYAT LAMA
    // =========================================================

    private void bersihkanRiwayatLama() {

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        if (history.isEmpty()) {
            return;
        }

        long batas =
                System.currentTimeMillis()
                        - (
                        365L
                                * 24L
                                * 60L
                                * 60L
                                * 1000L
                );

        StringBuilder baru =
                new StringBuilder();

        String[] semua =
                history.split(
                        "\n"
                );

        for (String data : semua) {

            try {

                String[] p =
                        data.split(
                                "\\|",
                                -1
                        );

                long waktu =
                        Long.parseLong(
                                p[0]
                        );

                if (waktu >= batas) {

                    if (baru.length() > 0) {
                        baru.append("\n");
                    }

                    baru.append(data);
                }

            } catch (Exception ignored) {
            }
        }

        prefs.edit()
                .putString(
                        KEY_HISTORY,
                        baru.toString()
                )
                .apply();
    }

    // =========================================================
    // ENCODE / DECODE
    // =========================================================

    private String encode(
            String teks
    ) {

        try {

            return URLEncoder.encode(
                    teks,
                    "UTF-8"
            );

        } catch (Exception e) {

            return teks;
        }
    }

    private String decode(
            String teks
    ) {

        try {

            return java.net.URLDecoder.decode(
                    teks,
                    "UTF-8"
            );

        } catch (Exception e) {

            return teks;
        }
    }

    // =========================================================
    // BERSIHKAN FORM
    // =========================================================

    private void bersihkanForm() {

        if (namaInput != null) {
            namaInput.setText("");
        }

        if (waInput != null) {
            waInput.setText("");
        }

        if (tanggalInput != null) {
            tanggalInput.setText("");
        }

        if (motorInput != null) {
            motorInput.setText("");
        }

        if (dpInput != null) {
            dpInput.setText("");
        }

        statusPembayaran =
                "BELUM LUNAS";
    }

    // =========================================================
    // KELAS DATA
    // =========================================================

    private static class Item {

        String nama = "";

        long jumlah = 0;

        long harga = 0;

        long subtotal = 0;
    }

    private static class Nota {

        long timestamp = 0;

        String nama = "";

        String wa = "";

        String tanggal = "";

        String motor = "";

        long dp = 0;

        long total = 0;

        String status =
                "BELUM LUNAS";

        ArrayList<Item> items =
                new ArrayList<>();
    }
}
