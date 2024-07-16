package Activities;
import static constants.keyName.STORE_NAME;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.stores.databinding.ActivityStoreOwnerBinding;

import java.util.Map;
import java.util.Objects;

import interfaces.GetStoreDataCallback;
import models.Store;

public class StoreOwnerActivity extends AppCompatActivity {

    ActivityStoreOwnerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoreOwnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUI();
        setupEvents();




    }

    private void setupEvents(){
        binding.imageBack.setOnClickListener(v -> finish());
        binding.btnViewStore.setOnClickListener(v -> {
            Intent intent = new Intent(StoreOwnerActivity.this, ViewMyStoreActivity.class);
            startActivity(intent);
        });

        binding.layoutProducts.setOnClickListener(v -> {
            Intent intent = new Intent(StoreOwnerActivity.this, MyProductsActivity.class);
            startActivity(intent);
        });

    }
    private void initUI(){
        getWindow().setStatusBarColor(Color.parseColor("#F04D7F"));
        Objects.requireNonNull(getSupportActionBar()).hide();

        binding.progressBarStoreName.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        if (intent != null){
            String storeId = intent.getStringExtra("storeId");
            // lấy thông tin avatar, invoice


            if (storeId != null){
                Store store = new Store();
                store.onGetStoreData(storeId, new GetStoreDataCallback() {
                    @Override
                    public void onGetDataSuccess(Map<String, Object> data) {
                        binding.progressBarStoreName.setVisibility(View.GONE);
                        binding.txtStoreName.setText((CharSequence) data.get(STORE_NAME));

                        // set up UI avatar, invoice

                    }

                    @Override
                    public void onGetDataFailure(String errorMessage) {
                        Toast.makeText(StoreOwnerActivity.this, "Uiii, lỗi mạng rồi :(((", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }

    }


}