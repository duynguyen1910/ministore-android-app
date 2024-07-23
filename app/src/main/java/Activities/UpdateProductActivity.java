package Activities;

import static constants.keyName.CATEGORY_ID;
import static constants.keyName.CATEGORY_NAME;
import static constants.keyName.PRODUCT_DESC;
import static constants.keyName.PRODUCT_ID;
import static constants.keyName.PRODUCT_INSTOCK;
import static constants.keyName.PRODUCT_NAME;
import static constants.keyName.PRODUCT_NEW_PRICE;
import static constants.keyName.PRODUCT_OLD_PRICE;
import static constants.keyName.STORE_ID;
import static constants.toastMessage.CREATE_PRODUCT_SUCCESSFULLY;
import static constants.toastMessage.DEFAULT_REQUIRE;
import static constants.toastMessage.INTERNET_ERROR;
import static constants.toastMessage.UPDATE_PRODUCT_FAILED;
import static constants.toastMessage.UPDATE_PRODUCT_SUCCESSFULLY;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stores.databinding.ActivityUpdateProductBinding;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import interfaces.CreateDocumentCallback;
import interfaces.GetDocumentCallback;
import interfaces.UpdateDocumentCallback;
import models.Product;

public class UpdateProductActivity extends AppCompatActivity {

    ActivityUpdateProductBinding binding;
    String categoryId;
    String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUI();
        getBundle();
        setupEvents();


    }

    private void getBundle() {
        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra(PRODUCT_ID);
            if (productId != null) {
                Product product = new Product();
                product.getProductDetail(productId, new GetDocumentCallback() {
                    @Override
                    public void onGetDataSuccess(Map<String, Object> productDetail) {
                        binding.edtCategory.setText((CharSequence) productDetail.get(CATEGORY_NAME));
                        binding.edtDescription.setText((CharSequence) productDetail.get(PRODUCT_DESC));
                        binding.edtTitle.setText((CharSequence) productDetail.get(PRODUCT_NAME));

                        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                        double newPrice = (double) productDetail.get(PRODUCT_NEW_PRICE);
                        binding.edtPrice.setText(formatter.format(newPrice));

                        int inStock =   ((Long) Objects.requireNonNull(productDetail.get(PRODUCT_INSTOCK))).intValue();
                        binding.edtInStock.setText(String.valueOf(inStock));

                        categoryId = (String) productDetail.get(CATEGORY_ID);
                    }

                    @Override
                    public void onGetDataFailure(String errorMessage) {
                        showToast(INTERNET_ERROR);
                    }
                });
            }
        }
    }

    ActivityResultLauncher<Intent> launcherSelectCategory = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 1) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            String categoryName = bundle.getString(CATEGORY_NAME);
                            categoryId = bundle.getString(CATEGORY_ID);
                            binding.txtChooseCategory.setText("Ngành hàng: ");
                            binding.edtCategory.setText(categoryName);
                        }
                    }
                }
            });

    private void setupEvents() {
        binding.imageBack.setOnClickListener(v -> finish());
        binding.layoutDeliveryFee.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateProductActivity.this, DeliveryFeeActivity.class);
            startActivity(intent);
        });

        binding.btnSave.setOnClickListener(v -> {
            Map<String, Object> productData = validateForm();
            if (productData != null) {
                Product product = new Product();
                product.updateProduct(productData, productId, new UpdateDocumentCallback() {
                    @Override
                    public void onUpdateSuccess() {

                        showToast(UPDATE_PRODUCT_SUCCESSFULLY);
                        finish();
                    }

                    @Override
                    public void onUpdateFailure(String errorMessage) {
                        showToast(UPDATE_PRODUCT_FAILED);
                    }
                });
            }



        });

        binding.layoutCategory.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateProductActivity.this, SelectCategoryActivity.class);
            launcherSelectCategory.launch(intent);
        });

    }

    private void showToast(String message) {
        Toast.makeText(UpdateProductActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private Map<String, Object> validateForm() {
        String productName = Objects.requireNonNull(binding.edtTitle.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.edtDescription.getText()).toString().trim();

        String price = Objects.requireNonNull(binding.edtPrice.getText()).toString().trim().replace(".", "");
        String inStock = Objects.requireNonNull(binding.edtInStock.getText()).toString().trim();
        String categoryName = Objects.requireNonNull(binding.edtCategory.getText()).toString().trim();


        boolean isValid = true;

        if (productName.isEmpty()) {
            binding.edtTitle.setError(DEFAULT_REQUIRE);
            isValid = false;
        }

        if (description.isEmpty()) {
            binding.edtDescription.setError(DEFAULT_REQUIRE);
            isValid = false;
        }

        if (price.isEmpty()) {
            binding.edtPrice.setError(DEFAULT_REQUIRE);
            isValid = false;
        }

        if (inStock.isEmpty()) {
            binding.edtInStock.setError(DEFAULT_REQUIRE);
            isValid = false;
        }
        if (categoryName.isEmpty()) {
            binding.edtCategory.setError(DEFAULT_REQUIRE);
            isValid = false;
        }

        if (isValid) {
            Map<String, Object> newProduct = new HashMap<>();
            newProduct.put(PRODUCT_NAME, productName);
            newProduct.put(PRODUCT_DESC, description);
            newProduct.put(PRODUCT_NEW_PRICE, Double.parseDouble(price));
            newProduct.put(PRODUCT_OLD_PRICE, Double.parseDouble(price));
            newProduct.put(PRODUCT_INSTOCK, Integer.parseInt(inStock));
            newProduct.put(CATEGORY_ID, categoryId);
            newProduct.put(CATEGORY_NAME, categoryName);


            return newProduct;
        } else {
            return null;
        }
    }

    private void initUI() {
        getWindow().setStatusBarColor(Color.parseColor("#F04D7F"));
        Objects.requireNonNull(getSupportActionBar()).hide();

    }


}