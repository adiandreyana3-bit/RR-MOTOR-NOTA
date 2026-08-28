package com.rrmotor.nota;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

        prefs = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

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
        judul.setGravity(Gravity.CENTER);
        judul.setPadding(0, 10, 0, 25);

        utama.addView(judul);

        TextView subjudul = new TextView(this);
        subjudul.setText("Nota Servis & Penjualan");
        subjudul.setTextSize(17);
        subjudul.setGravity(Gravity.CENTER);

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
        itemTitle.setTextSize(19);
        itemTitle.setPadding(0, 25, 0, 10);
        utama.addView(itemTitle);

        itemContainer = new LinearLayout(this);
        itemContainer.setOrientation(LinearLayout.VERTICAL);
        utama.addView(itemContainer);

        Button tambahItem = new Button(this);
        tambahItem.setText("+ TAMBAH BARANG / JASA");
        tambahItem.setOnClickListener(v -> tambahBarisItem());
        utama.addView(tambahItem);

        totalText = buatHasilText("TOTAL: Rp0");
        utama.addView(totalText);

        dpInput = buatInput("Uang Muka (DP) - opsional");
        dpInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        dpInput.setOnFocusChangeListener(
                (v, focus) -> {
                    if (!focus) hitungTotal();
                }
        );
        utama.addView(dpInput);

        sisaText = buatHasilText("SISA PEMBAYARAN: Rp0");
        utama.addView(sisaText);

        statusText = buatHasilText("STATUS: BELUM LUNAS");
        utama.addView(statusText);

        Button simpan = new Button(this);
        simpan.setText("💾 SIMPAN NOTA");
        simpan.setOnClickListener(v -> simpanNota());
        utama.addView(simpan);

        Button riwayat = new Button(this);
        riwayat.setText("📚 LIHAT RIWAYAT NOTA");
        riwayat.setOnClickListener(v -> tampilkanRiwayat());
        utama.addView(riwayat);

        Button whatsapp = new Button(this);
        whatsapp.setText("📱 KIRIM VIA WHATSAPP");
        whatsapp.setOnClickListener(v -> kirimWhatsApp());
        utama.addView(whatsapp);

        tambahBarisItem();

        setContentView(scroll);
    }

    private EditText buatInput(String hint) {

        EditText edit = new EditText(this);

        edit.setHint(hint);
        edit.setTextSize(16);
        edit.setSingleLine(true);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMargins(0, 8, 0, 8);

        edit.setLayoutParams(lp);

        return edit;
    }

    private TextView buatHasilText(String text) {

        TextView tv = new TextView(this);

        tv.setText(text);
        tv.setTextSize(19);
        tv.setPadding(0, 15, 0, 15);

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
        baris.setPadding(0, 10, 0, 10);

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
        subtotal.setTextSize(16);

        Button hapus =
                new Button(this);

        hapus.setText("Hapus item");

        baris.addView(nama);
        baris.addView(jumlah);
        baris.addView(harga);
        baris.addView(subtotal);
        baris.addView(hapus);

        itemContainer.addView(baris);

        View.OnFocusChangeListener listener =
                (v, focus) -> {
                    if (!focus) hitungTotal();
                };

        jumlah.setOnFocusChangeListener(listener);
        harga.setOnFocusChangeListener(listener);

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

        if (teks.isEmpty()) return 0;

        try {
            return Long.parseLong(teks);
        } catch (Exception e) {
            return 0;
        }
    }

    private long hitungTotal() {

        long total = 0;

        for (int i = 0;
             i < itemContainer.getChildCount();
             i++) {

            View view =
                    itemContainer.getChildAt(i);

            if (!(view instanceof LinearLayout))
                continue;

            LinearLayout baris =
                    (LinearLayout) view;

            if (baris.getChildCount() < 5)
                continue;

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

        totalText.setText(
                "TOTAL: "
                        + formatRupiah(total)
        );

        long dp = angka(dpInput);

        if (dp > total)
            dp = total;

        long sisa = total - dp;

        sisaText.setText(
                "SISA PEMBAYARAN: "
                        + formatRupiah(sisa)
        );

        if (total > 0 && sisa == 0) {
            statusText.setText("STATUS: LUNAS");
        } else {
            statusText.setText("STATUS: BELUM LUNAS");
        }

        return total;
    }

    private String formatRupiah(long angka) {

        return rupiah
                .format(angka)
                .replace(",00", "");
    }

    private void simpanNota() {

        if (namaInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Nama pelanggan wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (waInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "No. WhatsApp wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (tanggalInput.getText().toString().trim().isEmpty()) {
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

        String lama =
                prefs.getString(
                        KEY_HISTORY,
                        ""
                );

        if (!lama.isEmpty())
            lama += "\n";

        lama += data;

        prefs.edit()
                .putString(KEY_HISTORY, lama)
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
                .append(encode(
                        namaInput.getText().toString()
                ));

        data.append("|")
                .append(encode(
                        waInput.getText().toString()
                ));

        data.append("|")
                .append(encode(
                        tanggalInput.getText().toString()
                ));

        data.append("|")
                .append(encode(
                        motorInput.getText().toString()
                ));

        data.append("|")
                .append(angka(dpInput));

        data.append("|");

        for (int i = 0;
             i < itemContainer.getChildCount();
             i++) {

            View view =
                    itemContainer.getChildAt(i);

            if (!(view instanceof LinearLayout))
                continue;

            LinearLayout baris =
                    (LinearLayout) view;

            if (baris.getChildCount() < 5)
                continue;

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

            if (n.isEmpty())
                continue;

            if (data.charAt(data.length() - 1) != '|')
                data.append(";");

            data.append(
                    encode(n)
            );

            data.append("~")
                    .append(angka(jumlah));

            data.append("~")
                    .append(angka(harga));
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
                25, 25, 25, 25
        );

        TextView judul =
                new TextView(this);

        judul.setText(
                "📚 RIWAYAT NOTA RR MOTOR"
        );

        judul.setTextSize(24);

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

                Nota nota =
                        bacaNota(data);

                if (nota == null)
                    continue;

                long sisa =
                        nota.total - nota.dp;

                if (sisa < 0)
                    sisa = 0;

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
                                + "\nDP: "
                                + formatRupiah(
                                        nota.dp
                                )
                                + "\nSisa: "
                                + formatRupiah(
                                        sisa
                                )
                                + "\nStatus: "
                                + (
                                sisa == 0
                                        ? "LUNAS"
                                        : "BELUM LUNAS"
                        )
                );

                info.setTextSize(16);

                layout.addView(info);
            }
        }

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

            if (p.length < 7)
                return null;

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

                String[] semua =
                        p[6].split(";");

                for (String item : semua) {

                    String[] x =
                            item.split(
                                    "~",
                                    -1
                            );

                    if (x.length < 3)
                        continue;

                    Item it = new Item();

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

    private void kirimWhatsApp() {

        String nomor =
                waInput.getText()
                        .toString()
                        .trim();

        if (nomor.isEmpty()) {

            Toast.makeText(
                    this,
                    "No. WhatsApp wajib diisi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        nomor =
                bersihkanNomor(nomor);

        String pesan =
                buatPesanWhatsApp();

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
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "WhatsApp tidak dapat dibuka",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String buatPesanWhatsApp() {

        StringBuilder sb =
                new StringBuilder();

        sb.append("*RR MOTOR*\n");
        sb.append("*NOTA SERVIS*\n\n");

        sb.append("Nama: ")
                .append(
                        namaInput.getText()
                                .toString()
                                .trim()
                )
                .append("\n");

        sb.append("Tanggal: ")
                .append(
                        tanggalInput.getText()
                                .toString()
                                .trim()
                )
                .append("\n");

        if (!motorInput.getText()
                .toString()
                .trim()
                .isEmpty()) {

            sb.append("Motor: ")
                    .append(
                            motorInput.getText()
                                    .toString()
                                    .trim()
                    )
                    .append("\n");
        }

        sb.append("\n");

        for (int i = 0;
             i < itemContainer.getChildCount();
             i++) {

            View view =
                    itemContainer.getChildAt(i);

            if (!(view instanceof LinearLayout))
                continue;

            LinearLayout baris =
                    (LinearLayout) view;

            if (baris.getChildCount() < 5)
                continue;

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

            if (n.isEmpty())
                continue;

            long q = angka(jumlah);
            long h = angka(harga);

            sb.append(n)
                    .append(" - ")
                    .append(q)
                    .append(" x ")
                    .append(formatRupiah(h))
                    .append("\n");
        }

        long total = hitungTotal();
        long dp = angka(dpInput);

        if (dp > total)
            dp = total;

        long sisa = total - dp;

        sb.append("\nTOTAL: ")
                .append(formatRupiah(total))
                .append("\n");

        sb.append("DP: ")
                .append(formatRupiah(dp))
                .append("\n");

        sb.append("SISA: ")
                .append(formatRupiah(sisa))
                .append("\n");

        sb.append("\nTerima kasih 🙏");

        return sb.toString();
    }

    private String bersihkanNomor(String nomor) {

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

        return nomor;
    }

    private String encode(String teks) {

        try {

            return URLEncoder.encode(
                    teks,
                    "UTF-8"
            );

        } catch (Exception e) {

            return teks;
        }
    }

    private String decode(String teks) {

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
