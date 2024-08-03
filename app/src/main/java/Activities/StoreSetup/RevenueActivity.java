package Activities.StoreSetup;
import static constants.keyName.STORE_ID;
import static constants.keyName.USER_INFO;
import static utils.Cart.CartUtils.showToast;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.stores.R;
import com.example.stores.databinding.ActivityRevenueBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.Objects;
import api.invoiceApi;
import api.productApi;
import interfaces.GetAggregateCallback;
import interfaces.GetCollectionCallback;
import models.Product;
import utils.Chart.CustomMarkerView;
import utils.Chart.CustomValueMoney2Formatter;
import utils.Chart.CustomValueMoneyFormatter;
import utils.Chart.CustomValueSoldFormatter;
import utils.FormatHelper;

public class RevenueActivity extends AppCompatActivity {


    private ActivityRevenueBinding binding;

    private String storeId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRevenueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();
        getStoreID();
        setupEvents();


    }

    private void setupEvents() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRevenue();
        getBestSeller();
        getHighesRevenue();
    }

    private void initUI() {
        getWindow().setStatusBarColor(Color.parseColor("#F04D7F"));
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    private void getStoreID() {
        SharedPreferences sharedPreferences = getSharedPreferences(USER_INFO, MODE_PRIVATE);
        storeId = sharedPreferences.getString(STORE_ID, null);
    }

    private void setupRevenue() {
        invoiceApi invoiceApi = new invoiceApi();
        invoiceApi.getRevenueByStoreID(storeId, new GetAggregateCallback() {
            @Override
            public void onSuccess(double sumTotal) {
                binding.txtRevenue.setText(FormatHelper.formatVND(sumTotal));
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }

    private void getBestSeller() {
        productApi productApi = new productApi();
        productApi.getTopBestSellerByStoreID(storeId, 5, new GetCollectionCallback<Product>() {
            @Override
            public void onGetListSuccess(ArrayList<Product> bestSellers) {
                ArrayList<String> productNames = new ArrayList<>();
                for (Product product : bestSellers) {
                    productNames.add(product.getProductName());
                }
                setupBestSellerChart(productNames, bestSellers);
            }

            @Override
            public void onGetListFailure(String errorMessage) {
                showToast(RevenueActivity.this, errorMessage);
            }
        });
    }
    private void getHighesRevenue() {
        productApi productApi = new productApi();
        productApi.getHighestRevenueByStoreID(storeId, 5, new GetCollectionCallback<Product>() {
            @Override
            public void onGetListSuccess(ArrayList<Product> highestRevenueList) {

                ArrayList<String> productNames = new ArrayList<>();
                for (Product product : highestRevenueList) {
                    productNames.add(product.getProductName());
                }
                setupHighestRevenueChart(productNames, highestRevenueList);
            }

            @Override
            public void onGetListFailure(String errorMessage) {
                showToast(RevenueActivity.this, errorMessage);
            }
        });
    }

    private void setupHighestRevenueChart(ArrayList<String> productNames, ArrayList<Product> products) {
        BarChart barChart1 = binding.highestRevenueChart;
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            entries.add(new BarEntry(i + 1, (float) (products.get(i).getSold() * products.get(i).getNewPrice())));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Products");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new CustomValueMoney2Formatter());

        BarData barData = new BarData(dataSet);
        barChart1.setData(barData);
        barData.setBarWidth(0.4f);

        barChart1.setFitBars(true);
        barChart1.getDescription().setEnabled(false);
        barChart1.animateY(2000);

        setupBarChart(barChart1, productNames);

        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        for (int i = 0; i < productNames.size(); i++) {
            LegendEntry entry = new LegendEntry();
            entry.label = productNames.get(i);
            entry.formColor = ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length];
            legendEntries.add(entry);
        }
        barChart1.getLegend().setCustom(legendEntries);
        barChart1.setExtraOffsets(10f, 80f, 10f, 40f);

        YAxis yAxis = barChart1.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setValueFormatter(new CustomValueMoneyFormatter());


        barChart1.invalidate();
    }


    private void setupBestSellerChart(ArrayList<String> productNames, ArrayList<Product> products) {
        BarChart barChart1 = binding.bestSellerChart;
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            entries.add(new BarEntry(i + 1, (float) products.get(i).getSold()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Products");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barChart1.setData(barData);
        barData.setBarWidth(0.4f);

        barChart1.setFitBars(true);
        barChart1.getDescription().setEnabled(false);
        barChart1.animateY(2000);

        setupBarChart(barChart1, productNames);

        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        for (int i = 0; i < productNames.size(); i++) {
            LegendEntry entry = new LegendEntry();
            entry.label = productNames.get(i).substring(0, 30);
            entry.formColor = ColorTemplate.MATERIAL_COLORS[i % ColorTemplate.MATERIAL_COLORS.length];
            legendEntries.add(entry);
        }
        barChart1.getLegend().setCustom(legendEntries);
        barChart1.setExtraOffsets(10f, 80f, 10f, 40f);

        YAxis yAxis = barChart1.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setValueFormatter(new CustomValueSoldFormatter());


        barChart1.invalidate();
    }


    private void setupBarChart(BarChart barChart, ArrayList<String> productNames) {
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setWordWrapEnabled(true);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setYOffset(0f);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setDrawInside(false);

        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(2000);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setDrawZeroLine(true);
        yAxisLeft.setDrawAxisLine(true);

        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.layout_marker_view, productNames);
        barChart.setMarker(markerView);
        markerView.setTextAlignment(CustomMarkerView.TEXT_ALIGNMENT_TEXT_START);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Đặt trục X ở phía dưới
        xAxis.setGranularity(1f); // Đảm bảo các nhãn được phân bố đều
        xAxis.setLabelRotationAngle(20f); // Xoay nhãn để tránh chồng chéo
        xAxis.setLabelCount(5, true); // Thiết lập số lượng nhãn
        xAxis.setTextSize(12f);


        barChart.getAxisRight().setEnabled(false);
    }

}