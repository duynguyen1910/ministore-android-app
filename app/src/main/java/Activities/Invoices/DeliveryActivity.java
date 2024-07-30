package Activities.Invoices;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stores.R;
import com.example.stores.databinding.ActivityDeliveryBinding;

import com.example.stores.databinding.ItemTabLabelAndQuantityBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Objects;

import Activities.MainActivity;
import Adapters.ViewPager2Adapter;
import Fragments.Delivery.DeliveryAwaitPickUpFragment;
import Fragments.Delivery.DeliveryBeingTransportedFragment;
import Fragments.Delivery.DeliveryCancelledFragment;
import Fragments.Delivery.DeliveryCompletedFragment;
import utils.DecorateUtils;

public class DeliveryActivity extends AppCompatActivity {

    ActivityDeliveryBinding binding;
    int awaitPickUpQuantity = 0;
    int beingTransportedQuantity = 0;
    int completedQuantity = 0;
    int cancelledQuantity = 0;
    int countCompleted = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();
        setupUI();
        getCountOfInvoices();
        setupEvents();

    }

    private void getCountOfInvoices() {
        countCompleted = 3;
        // call api đếm số lượng invoices của từng loại sau đó update tabQuantity
        // xem MyProductsActivity để xem rõ hơn
        updateTabQuantity();
    }

    private void updateTabQuantity() {
        if (countCompleted >= 3) {
            updateTabLayout(0, awaitPickUpQuantity);
            updateTabLayout(1, beingTransportedQuantity);
            updateTabLayout(2, completedQuantity);
        }
    }

    private void updateTabLayout(int index, int quantity) {
        TabLayout.Tab tab = binding.tabLayout.getTabAt(index);
        if (tab != null && tab.getCustomView() != null) {
            TextView tabQuantity = tab.getCustomView().findViewById(R.id.tabQuantity);
            tabQuantity.setText("(" + quantity + ")");
        }
    }

    private void setupUI() {
        ViewPager2Adapter viewPager2Adapter = new ViewPager2Adapter(this);
        viewPager2Adapter.addFragment(new DeliveryAwaitPickUpFragment(), "Chờ lấy hàng");
        viewPager2Adapter.addFragment(new DeliveryBeingTransportedFragment(), "Đang vận chuyển");
        viewPager2Adapter.addFragment(new DeliveryCompletedFragment(), "Hoàn thành");
        viewPager2Adapter.addFragment(new DeliveryCancelledFragment(), "Đã hủy");

        binding.viewPager2.setAdapter(viewPager2Adapter);


        new TabLayoutMediator(binding.tabLayout, binding.viewPager2, (tab, position) -> {
            ItemTabLabelAndQuantityBinding tabLayoutBinding = ItemTabLabelAndQuantityBinding.inflate(getLayoutInflater());
            TextView tabLabel = tabLayoutBinding.tabLabel;
            tabLabel.setText(viewPager2Adapter.getPageTitle(position));
            tab.setCustomView(tabLayoutBinding.getRoot());
        }).attach();
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabLabel = customView.findViewById(R.id.tabLabel);
                    TextView tabQuantity = tab.getCustomView().findViewById(R.id.tabQuantity);
                    DecorateUtils.decorateSelectedTextViews(DeliveryActivity.this, tabLabel, tabQuantity);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabLabel = customView.findViewById(R.id.tabLabel);
                    TextView tabQuantity = tab.getCustomView().findViewById(R.id.tabQuantity);
                    DecorateUtils.decorateUnselectedTextViews(DeliveryActivity.this, tabLabel, tabQuantity);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        TabLayout.Tab tab = binding.tabLayout.getTabAt(0);
        if (tab != null && tab.getCustomView() != null) {
            TextView tabLabel = tab.getCustomView().findViewById(R.id.tabLabel);
            TextView tabQuantity = tab.getCustomView().findViewById(R.id.tabQuantity);
            DecorateUtils.decorateSelectedTextViews(DeliveryActivity.this, tabLabel, tabQuantity);
        }
    }


    private void setupEvents() {
        binding.imageBack.setOnClickListener(v -> finish());
        binding.imvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initUI() {
        getWindow().setStatusBarColor(Color.parseColor("#F04D7F"));
        Objects.requireNonNull(getSupportActionBar()).hide();


    }
}