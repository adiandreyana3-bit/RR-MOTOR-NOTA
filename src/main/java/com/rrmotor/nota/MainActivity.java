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
import java.util.Set;

public class MainActivity extends Activity {

    private static final int REQUEST_BLUETOOTH = 1001;

    private LinearLayout itemContainer;
    private TextView totalText;
    private TextView sisaText;
    private TextView statusText;

    private EditText namaInput;
    private EditText waInput;
    private EditText tanggalInput;
    private EditText motorInput;
    private EditText dpInput;

    private final ArrayList<Item> items = new ArrayList<>();

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

        prefs = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

        bersihkanRiwayatLama();

        tampilkanAplikasi();
    }

    private void tampilkanAplikasi() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout utama = new LinearLayout(this);
        utama.setOrientation(LinearLayout.VERTICAL);
        utama.setPadding(25, 25, 25, 35);

        scroll.addView(utama);

        TextView judul = new TextView(this);
        judul.setText("🏍️ RR MOTOR NOTA");
        judul.setTextSize(26);
        judul.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        judul.setGravity(Gravity.CENTER);
        judul.setPadding(0, 10, 0, 25);

        utama.addView(judul);

        TextView subjudul = new TextView(this);
        subjudul.setText(
                "Nota Servis & Penjualan"
        );
        subjudul.setTextSize(16);
        subjudul.setGravity(Gravity.CENTER);

        utama.addView(subjudul);

        namaInput =
                buatInput("Nama pelanggan *");
        utama.addView(namaInput);

        waInput =
                buatInput("No. WhatsApp *");

        waInput.setInputType(
                InputType.TYPE_CLASS_PHONE
        );

        utama.addView(waInput);

        tanggalInput =
                buatInput("Tanggal nota *");

        tanggalInput.setFocusable(false);
        tanggalInput.setOnClickListener(
                v -> pilihTanggal()
        );

        utama.addView(tanggalInput);

        motorInput =
                buatInput("Tipe motor (opsional)");

        utama.addView(motorInput);

        TextView itemTitle = new TextView(this);
        itemTitle.setText(
                "🧾 DAFTAR BARANG / JASA"
        );
        itemTitle.setTextSize(19);
        itemTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        itemTitle.setPadding(0, 30, 0, 10);

        utama.addView(itemTitle);

        itemContainer =
                new LinearLayout(this);

        itemContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        utama.addView(itemContainer);

        Button tambahItem = new Button(this);
        tambahItem.setText(
                "+ TAMBAH BARANG / JASA"
        );

        tambahItem.setOnClickListener(
                v -> tambahBarisItem()
        );

        utama.addView(tambahItem);

        totalText =
                buatHasilText("TOTAL: Rp0");

        utama.addView(totalText);

        dpInput =
                buatInput(
                        "Uang Muka (DP) - opsional"
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

        Button simpan = new Button(this);
        simpan.setText(
                "💾 SIMPAN NOTA"
        );

        simpan.setOnClickListener(
                v -> simpanNota()
        );

        utama.addView(simpan);

        Button cetak = new Button(this);
        cetak.setText(
                "🖨️ CETAK NOTA"
        );

        cetak.setOnClickListener(
                v -> cetakNotaSekarang()
        );

        utama.addView(cetak);

        Button bluetooth = new Button(this);
        bluetooth.setText(
                "🔵 CETAK BLUETOOTH"
        );

        bluetooth.setOnClickListener(
                v -> cetakBluetoothSekarang()
        );

        utama.addView(bluetooth);

        Button riwayat = new Button(this);
        riwayat.setText(
                "📚 LIHAT RIWAYAT NOTA"
        );

        riwayat.setOnClickListener(
                v -> tampilkanRiwayat()
        );

        utama.addView(riwayat);

        Button whatsapp = new Button(this);
        whatsapp.setText(
                "📱 KIRIM VIA WHATSAPP"
        );

        whatsapp.setOnClickListener(
                v -> kirimWhatsApp()
        );

        utama.addView(whatsapp);

        tambahBarisItem();

        setContentView(scroll);
    }

    private EditText buatInput(String hint) {

        EditText edit =
                new EditText(this);

        edit.setHint(hint);
        edit.setTextSize(16);
        edit.setSingleLine(true);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(
                0,
                8,
                0,
                8
        );

        edit.setLayoutParams(lp);

        return edit;
    }

    private TextView buatHasilText(
            String text
    ) {

        TextView tv =
                new TextView(this);

        tv.setText(text);
        tv.setTextSize(19);

        tv.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        tv.setPadding(
                0,
                18,
                0,
                18
        );

        return tv;
    }

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

    private void tambahBarisItem() {

        LinearLayout baris =
                new LinearLayout(this);

        baris.setOrientation(
                LinearLayout.VERTICAL
        );

        baris.setPadding(
                0,
                10,
                0,
                10
        );

        EditText nama =
                buatInput(
                        "Nama barang / jasa"
                );

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

        subtotal.setText(
                "Subtotal: Rp0"
        );

        subtotal.setTextSize(16);

        Button hapus =
                new Button(this);

        hapus.setText(
                "Hapus item"
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

        hapus.setOnClickListener(v -> {

            itemContainer.removeView(
                    baris
            );

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

        totalText.setText(
                "TOTAL: "
                        + formatRupiah(total)
        );

        long dp = angka(dpInput);

        if (dp > total) {
            dp = total;
        }

        long sisa =
                total - dp;

        sisaText.setText(
                "SISA PEMBAYARAN: "
                        + formatRupiah(sisa)
        );

        if (total > 0 && sisa == 0) {

            statusText.setText(
                    "STATUS: LUNAS"
            );

        } else {

            statusText.setText(
                    "STATUS: BELUM LUNAS"
            );
        }

        return total;
    }

    private String formatRupiah(long angka) {

        return rupiah
                .format(angka)
                .replace(",00", "");
    }

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

        String data =
                buatDataNota();

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
    }

    private String buatDataNota() {

        long timestamp =
                System.currentTimeMillis();

        String nama =
                encode(
                        namaInput.getText().toString()
                );

        String wa =
                encode(
                        waInput.getText().toString()
                );

        String tanggal =
                encode(
                        tanggalInput.getText().toString()
                );

        String motor =
                encode(
                        motorInput.getText().toString()
                );

        long dp =
                angka(dpInput);

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
                + "|" + itemData;
    }

    private void tampilkanRiwayat() {

        bersihkanRiwayatLama();

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
                25,
                25,
                25,
                25
        );

        TextView judul =
                new TextView(this);

        judul.setText(
                "📚 RIWAYAT NOTA RR MOTOR"
        );

        judul.setTextSize(24);

        judul.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        layout.addView(judul);

        if (history.isEmpty()) {

            TextView kosong =
                    new TextView(this);

            kosong.setText(
                    "\nBelum ada riwayat nota."
            );

            kosong.setTextSize(18);

            layout.addView(kosong);

        } else {

            String[] semua =
                    history.split("\n");

            for (String data : semua) {

                if (data.trim().isEmpty()) {
                    continue;
                }

                Nota nota =
                        bacaNota(data);

                if (nota == null) {
                    continue;
                }

                TextView info =
                        new TextView(this);

                long sisa =
                        nota.total - nota.dp;

                if (sisa < 0) {
                    sisa = 0;
                }

                String status =
                        sisa <= 0
                                ? "LUNAS"
                                : "BELUM LUNAS";

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
                                + "\nUang Muka: "
                                + formatRupiah(
                                        nota.dp
                                )
                                + "\nSisa: "
                                + formatRupiah(
                                        sisa
                                )
                                + "\nStatus: "
                                + status
                );

                info.setTextSize(16);

                info.setPadding(
                        0,
                        10,
                        0,
                        5
                );

                layout.addView(info);

                Button cetak =
                        new Button(this);

                cetak.setText(
                        "🖨️ CETAK NOTA INI"
                );

                cetak.setOnClickListener(
                        v -> cetakDataNota(nota)
                );

                layout.addView(cetak);

                Button cetakBT =
                        new Button(this);

                cetakBT.setText(
                        "🔵 CETAK BLUETOOTH"
                );

                cetakBT.setOnClickListener(
                        v -> cetakBluetooth(nota)
                );

                layout.addView(cetakBT);

                View garis =
                        new View(this);

                LinearLayout.LayoutParams gp =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2
                        );

                gp.setMargins(
                        0,
                        20,
                        0,
                        20
                );

                garis.setLayoutParams(gp);

                layout.addView(garis);
            }
        }

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(layout);

        setContentView(scroll);
    }

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
                history.split("\n");

        for (String data : semua) {

            try {

                String[] p =
                        data.split(
                                "\\|",
                                -1
                        );

                long waktu =
                        Long.parseLong(p[0]);

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

            Nota nota =
                    new Nota();

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

            nota.items.clear();

            if (!p[6].isEmpty()) {

                String[] semuaItem =
                        p[6].split(";");

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
                                decode(x[0]);

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

                        nota.items.add(it);

                        nota.total +=
                                it.subtotal;
                    }
                }
            }

            return nota;

        } catch (Exception e) {

            return null;
        }
    }

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
                        + namaInput.getText()
                        .toString()
                        .trim()
                        + "\n"
        );

        sb.append(
                "WA   : "
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

        if (!motorInput.getText()
                .toString()
                .trim()
                .isEmpty()) {

            sb.append(
                    "Motor : "
                            + motorInput.getText()
                            .toString()
                            .trim()
                            + "\n"
            );
        }

        sb.append(
                "--------------------\n"
        );

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
                        + (
                        sisa == 0
                                ? "LUNAS"
                                : "BELUM LUNAS"
                )
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

    private String buatIsiNota(Nota nota) {

        long sisa =
                nota.total - nota.dp;

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

        for (Item it : nota.items) {

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
                        + (
                        sisa == 0
                                ? "LUNAS"
                                : "BELUM LUNAS"
                )
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

    private void cetakNotaSekarang() {

        String isi =
                buatIsiNota();

        PrintManager printManager =
                (PrintManager)
                        getSystemService(
                                PRINT_SERVICE
                        );

        if (printManager == null) {

            Toast.makeText(
                    this,
                    "Fitur cetak tidak tersedia",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        printManager.print(
                "Nota RR MOTOR",
                new NotaPrintAdapter(
                        this,
                        isi
                ),
                new PrintAttributes.Builder()
                        .setMediaSize(
                                PrintAttributes.MediaSize.ISO_A4
                        )
                        .build()
        );
    }

    private void cetakDataNota(Nota nota) {

        String isi =
                buatIsiNota(nota);

        PrintManager printManager =
                (PrintManager)
                        getSystemService(
                                PRINT_SERVICE
                        );

        if (printManager == null) {

            Toast.makeText(
                    this,
                    "Fitur cetak tidak tersedia",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        printManager.print(
                "Nota RR MOTOR",
                new NotaPrintAdapter(
                        this,
                        isi
                ),
                new PrintAttributes.Builder()
                        .setMediaSize(
                                PrintAttributes.MediaSize.ISO_A4
                        )
                        .build()
        );
    }

    private void cetakBluetoothSekarang() {

        if (!cekIzinBluetooth()) {
            return;
        }

        String isi =
                buatIsiNota();

        tampilkanDaftarPrinter(
                isi
        );
    }

    private void cetakBluetooth(Nota nota) {

        if (!cekIzinBluetooth()) {
            return;
        }

        String isi =
                buatIsiNota(nota);

        tampilkanDaftarPrinter(
                isi
        );
    }

    private boolean cekIzinBluetooth() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S) {

            if (checkSelfPermission(
                    Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
                    ||
                    checkSelfPermission(
                            Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED) {

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

        if (requestCode ==
                REQUEST_BLUETOOTH) {

            boolean semua = true;

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
                        "Bluetooth diizinkan. Tekan CETAK BLUETOOTH lagi.",
                        Toast.LENGTH_LONG
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Izin Bluetooth diperlukan untuk mencetak.",
                        Toast.LENGTH_LONG
                ).show();
            }
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

        if (!adapter.isEnabled()) {

            Intent intent =
                    new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE
                    );

            startActivity(intent);

            Toast.makeText(
                    this,
                    "Aktifkan Bluetooth lalu tekan CETAK BLUETOOTH lagi.",
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
                    "Izin Bluetooth belum diberikan.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (devices == null
                || devices.isEmpty()) {

            Toast.makeText(
                    this,
                    "Belum ada printer Bluetooth yang dipasangkan dengan HP.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        ArrayList<BluetoothDevice>
                daftar =
                new ArrayList<>(
                        devices
                );

        String[] namaPrinter =
                new String[
                        daftar.size()
                ];

        for (int i = 0;
             i < daftar.size();
             i++) {

            BluetoothDevice device =
                    daftar.get(i);

            String nama;

            try {

                nama =
                        device.getName();

            } catch (SecurityException e) {

                nama = "Printer Bluetooth";
            }

            if (nama == null
                    || nama.isEmpty()) {

                nama =
                        "Perangkat Bluetooth";
            }

            namaPrinter[i] =
                    nama;
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Pilih Printer Bluetooth"
                        )
                        .setItems(
                                namaPrinter,
                                (d, which) -> {

                                    BluetoothDevice
                                            device =
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
                        .create();

        dialog.show();
    }

    private void cetakKePrinter(
            BluetoothDevice device,
            String isi
    ) {

        new Thread(() -> {

            java.io.OutputStream output =
                    null;

            android.bluetooth.BluetoothSocket
                    socket =
                    null;

            try {

                if (Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.S) {

                    if (checkSelfPermission(
                            Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        this,
                                        "Izin Bluetooth belum diberikan.",
                                        Toast.LENGTH_LONG
                                ).show()
                        );

                        return;
                    }
                }

                java.util.UUID uuid =
                        java.util.UUID.fromString(
                                "00001101-0000-1000-8000-00805F9B34FB"
                        );

                socket =
                        device.createRfcommSocketToServiceRecord(
                                uuid
                        );

                socket.connect();

                output =
                        socket.getOutputStream();

                output.write(
                        new byte[]{0x1B, 0x40}
                );

                output.write(
                        isi.getBytes(
                                java.nio.charset.Charset.forName(
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

    private void kirimWhatsApp() {

        String nomor =
                waInput.getText()
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
                        "[^0-9]",
                        ""
                );

        if (nomor.startsWith("0")) {

            nomor =
                    "62"
                            + nomor.substring(1);
        }

        if (nomor.startsWith("+")) {

            nomor =
                    nomor.substring(1);
        }

        return nomor;
    }

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

        ArrayList<Item> items =
                new ArrayList<>();
    }
}
