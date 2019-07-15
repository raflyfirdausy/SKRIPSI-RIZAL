package id.rizal.skripsi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.scwang.wave.MultiWaveHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static String HOST_GET_DATA;
    private SwipeRefreshLayout refreshLayout;
    private ImageView logo;
    private MultiWaveHeader waveHeader;
    private ProgressBar progressBar;
    private AnyChartView cvTanah;
    private TextView tvWaktu;
    private TextView tvKelembaban;
    private TextView tvStatus;
    private TextView tvTindakan;
    private AlertDialog.Builder dialog;
    private LayoutInflater inflater;
    private View dialogView;
    private Context context = MainActivity.this;
    private List<DataEntry> listHasilTanah = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logo = findViewById(R.id.logo);
        waveHeader = findViewById(R.id.waveHeader);
        progressBar = findViewById(R.id.progress_bar);
        cvTanah = findViewById(R.id.cvTanah);
        tvWaktu = findViewById(R.id.tvWaktu);
        tvKelembaban = findViewById(R.id.tvKelembaban);
        tvStatus = findViewById(R.id.tvStatus);
        tvTindakan = findViewById(R.id.tvTindakan);
        refreshLayout = findViewById(R.id.refreshLayout);

        HOST_GET_DATA = Config.HOST + "/skripsi/read.php";

        logo.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View view) {
                dialog = new AlertDialog.Builder(MainActivity.this);
                inflater = getLayoutInflater();
                dialogView = inflater.inflate(R.layout.layout_ip, null);
                dialog.setView(dialogView);
                dialog.setCancelable(true);
                dialog.setTitle("Setting Server");

                @SuppressLint("CutPasteId") final EditText alamatServer = dialogView.findViewById(R.id.alamatServer);
                @SuppressLint("CutPasteId") Button btnOk = dialogView.findViewById(R.id.btnOk);
                alamatServer.setText(Config.HOST);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Config.HOST = alamatServer.getText().toString();
                        dialogInterface.dismiss();
                        startActivity(new Intent(context, MainActivity.class));
                        finish();

                    }
                });
                dialog.show();
            }
        });

        cvTanah.setProgressBar(progressBar);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cekData();
                handler.postDelayed(this, 1000);
                if (Config.jumlah_data_sekarang < Config.jumlah_data_terbaru) {
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                }
            }
        }, 1000);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getData();
    }

    private void cekData() {
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                HOST_GET_DATA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getInt("status") == 1) {
                                JSONArray jsonArray = object.getJSONArray("result");
                                Config.jumlah_data_terbaru = jsonArray.length();
                            } else {
                                Toast.makeText(context, object.getString("result"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(stringRequest);

    }

    private void getData() {
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                HOST_GET_DATA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getInt("status") == 1) {
                                listHasilTanah.clear();
                                JSONArray jsonArray = object.getJSONArray("result");
                                Config.jumlah_data_sekarang = jsonArray.length();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject data = jsonArray.getJSONObject(i);
                                    listHasilTanah.add(new DataModel(
                                            data.getString("tanggal"),
                                            data.getDouble("nilai")
                                    ));

                                    tvWaktu.setText(data.getString("tanggal"));
                                    tvKelembaban.setText(data.getString("nilai"));
                                    tvStatus.setText(data.getString("status"));
                                    tvTindakan.setText(data.getString("tindakan"));
                                }
                                makeChart(
                                        cvTanah,
                                        listHasilTanah,
                                        "Grafik Kelembaban Tanah",
                                        "Nilai kelembaban",
                                        "Waktu",
                                        "Nilai ");
                            } else {
                                Toast.makeText(context, object.getString("result"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        refreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refreshLayout.setRefreshing(false);
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);
    }

    private void makeChart(AnyChartView anyChartView,
                           List<DataEntry> dataEntryList,
                           String titleAtas,
                           String titleKiri,
                           String titleBawah,
                           String detailName) {

        APIlib.getInstance().setActiveAnyChartView(anyChartView);
        Cartesian chart = AnyChart.line();
        chart.animation(true);
        chart.padding(10d, 20d, 20d, 10d);
        chart.crosshair().enabled(true);
        chart.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null,
                        null,
                        null,
                        (String) null,
                        (String) null);

        chart.tooltip().positionMode(TooltipPositionMode.POINT);
        chart.title(titleAtas);
        chart.tooltip().title("Detail");
        chart.yAxis(0).title(titleKiri);
        chart.xAxis(0).title(titleBawah);
        chart.xScroller(true);

        Set dataSet = Set.instantiate();
        dataSet.data(dataEntryList);
        Mapping mappingValue = dataSet.mapAs("{ x: 'x', value: 'value' }");

        //TODO : Make Line
        makeLine(chart.line(mappingValue), detailName);

        chart.legend().enabled(false);
        anyChartView.setChart(chart);
        APIlib.getInstance().setActiveAnyChartView(null);
    }

    private void makeLine(Line line, String name) {
        line.name(name);
        line.markers().enabled();
        line.hovered().markers().enabled(true);
        line.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);

        line.tooltip()
                .title(true)
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);
    }

    @Override
    public void onRefresh() {
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }
}
