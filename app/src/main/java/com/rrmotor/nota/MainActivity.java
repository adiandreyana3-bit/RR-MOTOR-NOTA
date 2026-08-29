package com.rrmotor.nota;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    // =========================================================
    // KONSTANTA
    // =========================================================

    private static final int REQUEST_BLUETOOTH = 1001;

    private static final String PREF_NAME =
            "RR_MOTOR_NOTA";

    private static final String KEY_HISTORY =
            "HISTORY";

    // Bluetooth Serial Port Profile
    private static final UUID SPP_UUID =
            UUID.fromString(
                    "00001101-0000-1000-8000-00805F9B34FB"
            );

    // Charset untuk printer thermal
    private final Charset PRINTER_CHARSET =
            Charset.forName("ISO-8859-1");

    // =========================================================
    // UI
    // =========================================================

    private LinearLayout itemContainer;

    private TextView totalText;
    private TextView sisaText;
    private TextView statusText;

    private EditText namaInput;
    private EditText waInput;
    private EditText tanggalInput;
    private EditText motorInput;
    private EditText dpInput;

    // =========================================================
    // DATA
    // =========================================================

    private SharedPreferences prefs;

    private long editingTimestamp = -1;

    private String pendingBluetoothText = null;

    private String statusPembayaran =
            "BELUM LUNAS";

    private final NumberFormat rupiah =
            NumberFormat.getCurrencyInstance(
                    new Locale("id", "ID")
            );

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        prefs =
                getSharedPreferences(
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

        statusPembayaran =
                "BELUM LUNAS";

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        LinearLayout utama =
                new LinearLayout(this);

        utama.setOrientation(
                LinearLayout.VERTICAL
        );

        utama.setPadding(
                14,
                8,
                14,
                20
        );

        scroll.addView(utama);

        TextView judul =
                new TextView(this);

        judul.setText(
                "🏍️ RR MOTOR NOTA"
        );

        judul.setTextSize(21);

        judul.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        judul.setGravity(
                Gravity.CENTER
        );

        judul.setPadding(
                0,
                4,
                0,
                3
        );

        utama.addView(judul);

        TextView subjudul =
                new TextView(this);

        subjudul.setText(
                "Nota Servis & Penjualan"
        );

        subjudul.setTextSize(13);

        subjudul.setGravity(
                Gravity.CENTER
        );

        subjudul.setPadding(
                0,
                0,
                0,
                6
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

        itemTitle.setTextSize(16);

        itemTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        itemTitle.setPadding(
                0,
                8,
                0,
                3
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

        scroll.post(
                () -> scroll.scrollTo(0, 0)
        );
    }

    // =========================================================
    // INPUT
    // =========================================================

    private EditText buatInput(
            String hint
    ) {

        EditText edit =
                new EditText(this);

        edit.setHint(hint);

        edit.setTextSize(14);

        edit.setSingleLine(true);

        edit.setPadding(
                8,
                2,
                8,
                2
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(
                0,
                1,
                0,
                1
        );

        edit.setLayoutParams(lp);

        return edit;
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private Button buatTombol(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(13);

        button.setAllCaps(false);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(
                0,
                1,
                0,
                1
        );

        button.setLayoutParams(lp);

        return button;
    }

    // =========================================================
    // HASIL
    // =========================================================

    private TextView buatHasilText(
            String text
    ) {

        TextView tv =
                new TextView(this);

        tv.setText(text);

        tv.setTextSize(16);

        tv.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        tv.setPadding(
                0,
                5,
                0,
                5
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
    }

    // =========================================================
    // BARANG
    // =========================================================

    private void tambahBarisItem() {

        LinearLayout baris =
                new LinearLayout(this);

        baris.setOrientation(
                LinearLayout.VERTICAL
        );

        baris.setPadding(
                0,
                2,
                0,
                2
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

        subtotal.setTextSize(13);

        Button hapus =
                buatTombol(
                        "❌ Hapus item"
                );

        baris.addView(nama);
        baris.addView(jumlah);
        baris.addView(harga);
        baris.addView(subtotal);
        baris.addView(hapus);

        itemContainer.addView(baris);

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

        hapus.setOnClickListener(
                v -> {

                    itemContainer.removeView(
                            baris
                    );

                    if (
                            itemContainer
                                    .getChildCount()
                                    == 0
                    ) {
                        tambahBarisItem();
                    }

                    hitungTotal();
                }
        );

        hitungTotal();
    }

    // =========================================================
    // ANGKA
    // =========================================================

    private long angka(
            EditText edit
    ) {

        if (edit == null) {
            return 0;
        }

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

    // =========================================================
    // HITUNG TOTAL
    // =========================================================

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
                    itemContainer
                            .getChildAt(i);

            if (
                    !(view instanceof LinearLayout)
            ) {
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

        tampilkanStatus();

        return total;
    }

    // =========================================================
    // RUPIAH
    // =========================================================

    private String formatRupiah(
            long angka
    ) {

        return rupiah
                .format(angka)
                .replace(",00", "");
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void tampilkanStatus() {

        if (statusText != null) {

            statusText.setText(
                    "STATUS: "
                            + statusPembayaran
            );
        }
    }

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
                        "BATAL",
                        null
                )
                .show();
    }

    // =========================================================
    // SIMPAN
    // =========================================================

    private void simpanNota() {

        String nama =
                namaInput.getText()
                        .toString()
                        .trim();

        String wa =
                waInput.getText()
                        .toString()
                        .trim();

        String tanggal =
                tanggalInput.getText()
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

        tampilkanAplikasi();
    }

    private void perbaruiNota() {

        String history =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        String[] semua =
                history.split(
                        "\n",
                        -1
                );

        StringBuilder baru =
                new StringBuilder();

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
                        Long.parseLong(p[0]);

                if (
                        timestamp ==
                                editingTimestamp
                ) {

                    if (baru.length() > 0) {
                        baru.append("\n");
                    }

                    baru.append(dataBaru);

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
                        namaInput.getText()
                                .toString()
                );

        String wa =
                encode(
                        waInput.getText()
                                .toString()
                );

        String tanggal =
                encode(
                        tanggalInput.getText()
                                .toString()
                );

        String motor =
                encode(
                        motorInput.getText()
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
                    itemContainer
                            .getChildAt(i);

            if (
                    !(view instanceof LinearLayout)
            ) {
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
                    namaBarang.getText()
                            .toString()
                            .trim();

            if (nb.isEmpty()) {
                continue;
            }

            if (itemData.length() > 0) {
                itemData.append(";");
            }

            itemData
                    .append(encode(nb))
                    .append("~")
                    .append(angka(jumlah))
                    .append("~")
                    .append(angka(harga));
        }

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
                14,
                10,
                14,
                20
        );

        scroll.addView(layout);

        TextView judul =
                new TextView(this);

        judul.setText(
                "📚 RIWAYAT NOTA RR MOTOR"
        );

        judul.setTextSize(20);

        judul.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        layout.addView(judul);

        Button kembali =
                buatTombol(
                        "⬅️ KEMBALI KE MENU UTAMA"
                );

        kembali.setOnClickListener(
                v -> tampilkanAplikasi()
        );

        layout.addView(kembali);

        if (history.trim().isEmpty()) {

            TextView kosong =
                    new TextView(this);

            kosong.setText(
                    "\nBelum ada riwayat nota."
            );

            kosong.setTextSize(16);

            layout.addView(kosong);

        } else {

            String[] semua =
                    history.split(
                            "\n",
                            -1
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
                        nota.total - nota.dp;

                if (sisa < 0) {
                    sisa = 0;
                }

                TextView info =
                        new TextView(this);

                info.setText(
                        "\n"
                                + "👤 "
                                + nota.nama
                                + "\n"
                                + "📱 "
                                + nota.wa
                                + "\n"
                                + "📅 "
                                + nota.tanggal
                                + "\n"
                                + "🏍️ "
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

                info.setTextSize(14);

                layout.addView(info);

                Button edit =
                        buatTombol(
                                "✏️ EDIT NOTA"
                        );

                edit.setOnClickListener(
                        v -> editNota(nota)
                );

                layout.addView(edit);

                Button cetak =
                        buatTombol(
                                "🖨️ CETAK NOTA INI"
                        );

                cetak.setOnClickListener(
                        v -> cetakDataNota(nota)
                );

                layout.addView(cetak);

                Button cetakBT =
                        buatTombol(
                                "🔵 CETAK BLUETOOTH"
                        );

                cetakBT.setOnClickListener(
                        v -> cetakBluetooth(nota)
                );

                layout.addView(cetakBT);

                Button whatsapp =
                        buatTombol(
                                "📱 KIRIM VIA WHATSAPP"
                        );

                whatsapp.setOnClickListener(
                        v -> kirimWhatsAppNota(nota)
                );

                layout.addView(whatsapp);

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
                        10,
                        0,
                        10
                );

                garis.setLayoutParams(gp);

                layout.addView(garis);
            }
        }

        setContentView(scroll);

        scroll.post(
                () -> scroll.scrollTo(0, 0)
        );
    }

    // =========================================================
    // EDIT
    // =========================================================

    private void editNota(
            Nota nota
    ) {

        editingTimestamp =
                nota.timestamp;

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
                14,
                10,
                14,
                20
        );

        scroll.addView(utama);

        TextView judul =
                new TextView(this);

        judul.setText(
                "✏️ EDIT NOTA"
        );

        judul.setTextSize(20);

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

        itemTitle.setTextSize(16);

        itemTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        utama.addView(itemTitle);

        itemContainer =
                new LinearLayout(this);

        itemContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        utama.addView(itemContainer);

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
                String.valueOf(
                        nota.dp
                )
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

        Button cetak =
                buatTombol(
                        "🖨️ CETAK NOTA INI"
                );

        cetak.setOnClickListener(
                v -> cetakDataNota(
                        notaSementaraDariForm()
                )
        );

        utama.addView(cetak);

        Button cetakBT =
                buatTombol(
                        "🔵 CETAK BLUETOOTH"
                );

        cetakBT.setOnClickListener(
                v -> cetakBluetooth(
                        notaSementaraDariForm()
                )
        );

        utama.addView(cetakBT);

        Button batal =
                buatTombol(
                        "⬅️ BATAL / KEMBALI"
                );

        batal.setOnClickListener(
                v -> tampilkanRiwayat()
        );

        utama.addView(batal);

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
                                        .getChildAt(index);

                EditText nama =
                        (EditText)
                                baris.getChildAt(0);

                EditText jumlah =
                        (EditText)
                                baris.getChildAt(1);

                EditText harga =
                        (EditText)
                                baris.getChildAt(2);

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

        scroll.post(
                () -> scroll.scrollTo(0, 0)
        );
    }

    // =========================================================
    // NOTA SEMENTARA
    // =========================================================

    private Nota notaSementaraDariForm() {

        Nota nota =
                new Nota();

        nota.timestamp =
                editingTimestamp;

        nota.nama =
                namaInput.getText()
                        .toString()
                        .trim();

        nota.wa =
                waInput.getText()
                        .toString()
                        .trim();

        nota.tanggal =
                tanggalInput.getText()
                        .toString()
                        .trim();

        nota.motor =
                motorInput.getText()
                        .toString()
                        .trim();

        nota.dp =
                angka(dpInput);

        nota.status =
                statusPembayaran;

        nota.total = 0;

        for (
                int i = 0;
                i < itemContainer.getChildCount();
                i++
        ) {

            View view =
                    itemContainer
                            .getChildAt(i);

            if (
                    !(view instanceof LinearLayout)
            ) {
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

            Item item =
                    new Item();

            item.nama = n;

            item.jumlah =
                    angka(jumlah);

            item.harga =
                    angka(harga);

            item.subtotal =
                    item.jumlah
                            * item.harga;

            nota.total +=
                    item.subtotal;

            nota.items.add(item);
        }

        if (nota.dp > nota.total) {
            nota.dp = nota.total;
        }

        return nota;
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
                        p[6].split(";");

                for (
                        String itemData :
                        semuaItem
                ) {

                    String[] x =
                            itemData.split(
                                    "~",
                                    -1
                            );

                    if (x.length >= 3) {

                        Item item =
                                new Item();

                        item.nama =
                                decode(x[0]);

                        item.jumlah =
                                Long.parseLong(
                                        x[1]
                                );

                        item.harga =
                                Long.parseLong(
                                        x[2]
                                );

                        item.subtotal =
                                item.jumlah
                                        * item.harga;

                        nota.total +=
                                item.subtotal;

                        nota.items.add(item);
                    }
                }
            }

            if (
                    p.length >= 8
                            && !p[7].isEmpty()
            ) {

                nota.status =
                        decode(p[7]);

            } else {

                nota.status =
                        nota.dp >= nota.total
                                && nota.total > 0
                                ? "LUNAS"
                                : "BELUM LUNAS";
            }

            return nota;

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // HAPUS
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
                        "\n",
                        -1
                );

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

                long waktu =
                        Long.parseLong(
                                p[0]
                        );

                if (waktu == timestamp) {
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

        cetakDenganAndroid(
                buatIsiNota()
        );
    }

    private void cetakDataNota(
            Nota nota
    ) {

        cetakDenganAndroid(
                buatIsiNota(nota)
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
                    new PrintAttributes.MediaSize(
                            "RR_MOTOR_50MM",
                            "50 mm",
                            1969,
                            10000
                    );

            PrintAttributes attributes =
                    new PrintAttributes.Builder()
                            .setMediaSize(
                                    ukuran
                            )
                            .setMinMargins(
                                    PrintAttributes.Margins.NO_MARGINS
                            )
                            .build();

            printManager.print(
                    "Nota RR MOTOR",
                    new NotaPrintAdapter(
                            this,
                            isi
                    ),
                    attributes
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
                "--------------------------------\n"
        );

        sb.append(
                "           RR MOTOR\n"
        );

        sb.append(
                "         NOTA SERVIS\n"
        );

        sb.append(
                "--------------------------------\n"
        );

        sb.append(
                "Nama    : "
                        + namaInput.getText()
                        .toString()
                        .trim()
                        + "\n"
        );

        sb.append(
                "WA      : "
                        + waInput.getText()
                        .toString()
                        .trim()
                        + "\n"
        );

        sb.append(
                "Tanggal : "
                        + tanggalInput.getText()
                        .toString()
                        .trim()
                        + "\n"
        );

        String motor =
                motorInput.getText()
                        .toString()
                        .trim();

        if (!motor.isEmpty()) {

            sb.append(
                    "Motor   : "
                            + motor
                            + "\n"
            );
        }

        sb.append(
                "--------------------------------\n"
        );

        tambahkanBarangKeNota(sb);

        sb.append(
                "--------------------------------\n"
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
                "--------------------------------\n"
        );

        sb.append(
                "          Terima kasih\n\n\n"
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
                    itemContainer
                            .getChildAt(i);

            if (
                    !(view instanceof LinearLayout)
            ) {
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

            sb.append(n)
                    .append("\n");

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

    // =========================================================
    // ISI NOTA DARI RIWAYAT
    // =========================================================

    private String buatIsiNota(
            Nota nota
    ) {

        long sisa =
                nota.total - nota.dp;

        if (sisa < 0) {
            sisa = 0;
        }

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "--------------------------------\n"
        );

        sb.append(
                "           RR MOTOR\n"
        );

        sb.append(
                "         NOTA SERVIS\n"
        );

        sb.append(
                "--------------------------------\n"
        );

        sb.append(
                "Nama    : "
                        + nota.nama
                        + "\n"
        );

        sb.append(
                "WA      : "
                        + nota.wa
                        + "\n"
        );

        sb.append(
                "Tanggal : "
                        + nota.tanggal
                        + "\n"
        );

        if (
                nota.motor != null
                        && !nota.motor.isEmpty()
        ) {

            sb.append(
                    "Motor   : "
                            + nota.motor
                            + "\n"
            );
        }

        sb.append(
                "--------------------------------\n"
        );

        for (Item item :
                nota.items) {

            sb.append(
                    item.nama
                            + "\n"
            );

            sb.append(
                    item.jumlah
                            + " x "
                            + formatRupiah(
                                    item.harga
                            )
                            + " = "
                            + formatRupiah(
                                    item.subtotal
                            )
                            + "\n"
            );
        }

        sb.append(
                "--------------------------------\n"
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
                "--------------------------------\n"
        );

        sb.append(
                "          Terima kasih\n\n\n"
        );

        return sb.toString();
    }

    // =========================================================
    // BLUETOOTH - MULAI CETAK
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

    // =========================================================
    // CEK IZIN BLUETOOTH
    // =========================================================

    private boolean cekIzinBluetooth() {

        if (
                Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.S
        ) {

            boolean connect =
                    checkSelfPermission(
                            Manifest.permission.BLUETOOTH_CONNECT
                    )
                            ==
                            PackageManager.PERMISSION_GRANTED;

            boolean scan =
                    checkSelfPermission(
                            Manifest.permission.BLUETOOTH_SCAN
                    )
                            ==
                            PackageManager.PERMISSION_GRANTED;

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

    // =========================================================
    // HASIL PERMISSION
    // =========================================================

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

        if (
                requestCode !=
                        REQUEST_BLUETOOTH
        ) {
            return;
        }

        boolean semua =
                grantResults.length > 0;

        for (int hasil :
                grantResults) {

            if (
                    hasil !=
                            PackageManager.PERMISSION_GRANTED
            ) {

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

            if (
                    pendingBluetoothText
                            != null
            ) {

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
                    "Izin Perangkat di sekitar diperlukan.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // DAFTAR PRINTER
    // =========================================================

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

                startActivity(
                        new Intent(
                                BluetoothAdapter.ACTION_REQUEST_ENABLE
                        )
                );

                Toast.makeText(
                        this,
                        "Aktifkan Bluetooth lalu tekan CETAK BLUETOOTH lagi.",
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

        if (
                devices == null
                        || devices.isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Pasangkan printer terlebih dahulu di Pengaturan HP.",
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

                if (
                        device.getName()
                                != null
                ) {

                    nama =
                            device.getName();
                }

            } catch (
                    SecurityException ignored
            ) {
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
                        "BATAL",
                        null
                )
                .show();
    }

    // =========================================================
    // CETAK KE PRINTER BLUETOOTH
    // =========================================================

    private void cetakKePrinter(
            BluetoothDevice device,
            String isi
    ) {

        // Hilangkan keyboard/dialog sebelum proses
        // dan berikan informasi bahwa printer sedang bekerja.

        Toast.makeText(
                this,
                "🔵 Menghubungkan ke printer...",
                Toast.LENGTH_SHORT
        ).show();

        new Thread(() -> {

            boolean berhasil = false;

            String pesanError =
                    "Koneksi printer gagal.";

            for (
                    int percobaan = 1;
                    percobaan <= 3;
                    percobaan++
            ) {

                BluetoothSocket socket =
                        null;

                OutputStream output =
                        null;

                try {

                    // -------------------------------------------------
                    // CEK IZIN ANDROID 12+
                    // -------------------------------------------------

                    if (
                            Build.VERSION.SDK_INT
                                    >= Build.VERSION_CODES.S
                    ) {

                        if (
                                checkSelfPermission(
                                        Manifest.permission.BLUETOOTH_CONNECT
                                )
                                        !=
                                        PackageManager.PERMISSION_GRANTED
                        ) {

                            throw new SecurityException(
                                    "Izin BLUETOOTH_CONNECT belum diberikan."
                            );
                        }
                    }

                    BluetoothAdapter adapter =
                            BluetoothAdapter
                                    .getDefaultAdapter();

                    if (adapter == null) {

                        throw new Exception(
                                "Bluetooth tidak tersedia."
                        );
                    }

                    // -------------------------------------------------
                    // HENTIKAN DISCOVERY
                    // -------------------------------------------------

                    try {

                        if (
                                adapter.isDiscovering()
                        ) {

                            adapter.cancelDiscovery();
                        }

                    } catch (Exception ignored) {
                    }

                    // -------------------------------------------------
                    // COBA SOCKET SPP AMAN
                    // -------------------------------------------------

                    try {

                        socket =
                                device
                                        .createRfcommSocketToServiceRecord(
                                                SPP_UUID
                                        );

                        socket.connect();

                    } catch (Exception e) {

                        // -------------------------------------------------
                        // KALAU GAGAL, COBA INSECURE
                        // Banyak printer thermal murah menggunakan
                        // koneksi insecure.
                        // -------------------------------------------------

                        try {

                            if (socket != null) {
                                socket.close();
                            }

                        } catch (Exception ignored) {
                        }

                        socket =
                                device
                                        .createInsecureRfcommSocketToServiceRecord(
                                                SPP_UUID
                                        );

                        socket.connect();
                    }

                    // -------------------------------------------------
                    // BERIKAN WAKTU PRINTER SIAP
                    // -------------------------------------------------

                    Thread.sleep(500);

                    output =
                            socket.getOutputStream();

                    // -------------------------------------------------
                    // RESET PRINTER
                    // ESC @
                    // -------------------------------------------------

                    output.write(
                            new byte[]{
                                    0x1B,
                                    0x40
                            }
                    );

                    output.flush();

                    Thread.sleep(150);

                    // -------------------------------------------------
                    // FONT NORMAL
                    // ESC ! 0
                    // -------------------------------------------------

                    output.write(
                            new byte[]{
                                    0x1B,
                                    0x21,
                                    0x00
                            }
                    );

                    // ------------------------------------------------
