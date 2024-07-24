package Activities;

import static constants.keyName.PRODUCT_DESC;
import static constants.keyName.PRODUCT_ID;
import static constants.keyName.PRODUCT_INSTOCK;
import static constants.keyName.PRODUCT_NAME;
import static constants.keyName.PRODUCT_NEW_PRICE;
import static constants.keyName.PRODUCT_OLD_PRICE;
import static constants.keyName.STORE_ID;
import static constants.keyName.STORE_NAME;
import static constants.toastMessage.INTERNET_ERROR;
import static utils.AnimationUtils.translateAnimation;
import static utils.CartUtils.MYCART;
import static utils.CartUtils.showToast;
import static utils.CartUtils.updateQuantityInCart;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stores.R;
import com.example.stores.databinding.ActivityProductDetailBinding;
import com.example.stores.databinding.DialogAddToCartBinding;
import com.example.stores.databinding.DialogBuyNowBinding;
import com.example.stores.databinding.DialogProductImageExpandBinding;
import com.example.stores.databinding.LayoutProductDetailBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import interfaces.GetDocumentCallback;
import models.CartItem;
import models.Product;
import models.Store;

public class ProductDetailActivity extends AppCompatActivity {

    ActivityProductDetailBinding binding;
    private String productId;
    private String storeId;
    Product thisProduct;
    private String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();
        getBundle();
        setupProductInfo();
        setupStoreInfo();
        setupEvents();
    }

    private void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            productId = bundle.getString(PRODUCT_ID, null);
            storeId = bundle.getString(STORE_ID, null);
        }
    }


    private void setupProductInfo() {
        binding.layoutProductsInfo.setVisibility(View.GONE);
        binding.progressBarProduct.setVisibility(View.VISIBLE);

        if (storeId != null && productId != null) {
            Product product = new Product();
            product.getProductDetail(productId, new GetDocumentCallback() {
                @Override
                public void onGetDataSuccess(Map<String, Object> productDetail) {
                    thisProduct = new Product(
                            (String) productDetail.get(PRODUCT_NAME),
                            (String) productDetail.get(PRODUCT_DESC),
                            (double) productDetail.get(PRODUCT_NEW_PRICE),
                            (double) productDetail.get(PRODUCT_OLD_PRICE),
                            ((Long) productDetail.get(PRODUCT_INSTOCK)).intValue(),
                            storeId,
                            1);
                    thisProduct.setBaseID(productId);
                    binding.progressBarProduct.setVisibility(View.GONE);
                    binding.layoutProductsInfo.setVisibility(View.VISIBLE);
                    // setup product information
                    binding.txtTitle.setText(thisProduct.getProductName());
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    String formattedOldPrice = formatter.format(thisProduct.getOldPrice());
                    binding.txtOldPrice.setText("đ" + formattedOldPrice);
                    binding.txtOldPrice.setPaintFlags(binding.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    String formattedNewPrice = formatter.format(thisProduct.getNewPrice());
                    binding.txtNewPrice.setText(formattedNewPrice);
                    binding.ratingBar.setRating(4.5F);
                    binding.txtRating.setText(4.5 + " / 5");
                    binding.txtSold.setText("Đã bán " + 200);
                    binding.txtProdctDescription.setText(thisProduct.getDescription());
                    binding.txtNewPriceInBuyNow.setText(formattedNewPrice);


                    // setup productImages
//                    ArrayList<SliderItem> sliderItems = new ArrayList<>();
//                    for (int i = 0; i < data.getProductImages().size(); i++) {
//                        sliderItems.add(new SliderItem(object.getProductImages().get(i)));
//                    }
//
//                    binding.viewPager2Slider.setAdapter(new SliderAdapterForProductDetail(ProductDetailActivity.this, sliderItems));
//                    binding.viewPager2Slider.setOffscreenPageLimit(2);
//                    binding.indicator.attachTo(binding.viewPager2Slider);
                }

                @Override
                public void onGetDataFailure(String errorMessage) {
                    Toast.makeText(ProductDetailActivity.this, INTERNET_ERROR, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


    private void setupStoreInfo() {
        binding.progressBarStore.setVisibility(View.VISIBLE);
        // lấy thông tin avatar, invoice
        if (storeId != null) {
            Store store = new Store();
            store.onGetStoreDetail(storeId, new GetDocumentCallback() {
                @Override
                public void onGetDataSuccess(Map<String, Object> data) {
                    storeName = (String) data.get(STORE_NAME);
                    binding.progressBarStore.setVisibility(View.GONE);
                    binding.txtStoreName.setText(storeName);

                    // set up UI avatar, invoice

                }

                @Override
                public void onGetDataFailure(String errorMessage) {
                    Toast.makeText(ProductDetailActivity.this, "Uiii, lỗi mạng rồi :(((", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addToCart(String storeName, Product product, int quantity) {
        if (storeName == null || product == null) {
            // Handle error or throw exception
            return;
        }

        boolean storeFound = false;
        boolean productFound = false;

        for (CartItem cartItem : MYCART) {
            if (storeName.equals(cartItem.getStoreName())) { // storeName cannot be null here
                storeFound = true;
                for (Product pr : cartItem.getListProducts()) {
                    if (product.getBaseID() != null && product.getBaseID().equals(pr.getBaseID())) { // Ensure product baseID is not null
                        int currQuantity = pr.getNumberInCart();
                        pr.setNumberInCart(currQuantity + quantity);
                        productFound = true;
                        break;
                    }
                }
                if (!productFound) {
                    product.setNumberInCart(quantity);
                    cartItem.getListProducts().add(product);
                }
                break;
            }
        }

        if (!storeFound) {
            ArrayList<Product> products = new ArrayList<>();
            product.setNumberInCart(quantity);
            products.add(product);
            MYCART.add(new CartItem(storeName, products));
        }
    }


    private void setupEvents() {
        binding.btnAddToCart.setOnClickListener(v -> {
            popUpAddToCartDialog();
        });
        binding.imageBack.setOnClickListener(v -> finish());

        binding.iconCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });

        binding.txtProductDetail.setOnClickListener(v -> popUpProductDetailDialog());

        binding.btnViewStore.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, ViewMyStoreActivity.class);
            intent.putExtra(STORE_ID, storeId);
            startActivity(intent);
        });

        binding.layoutBuyNow.setOnClickListener(v -> popUpBuyNowDialog());
    }

    private void popUpAddToCartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddToCartBinding dialogBinding = DialogAddToCartBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_edit_text_border);
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);

            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            dialogBinding.getRoot().startAnimation(slideUp);
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedOldPrice = formatter.format(thisProduct.getOldPrice());
        dialogBinding.txtOldPrice.setText("đ" + formattedOldPrice);
        dialogBinding.txtOldPrice.setPaintFlags(binding.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        String formattedNewPrice = formatter.format(thisProduct.getNewPrice());

        dialogBinding.txtNewPrice.setText(formattedNewPrice);
        dialogBinding.txtInStock.setText("Kho: " + thisProduct.getInStock());

        //reset số lượng trên dialog sau mỗi lần popup dialog

        dialogBinding.txtQuantity.setText(String.valueOf(1));

        dialogBinding.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(dialogBinding.txtQuantity.getText().toString().trim());
                if (quantity < 99) {
                    dialogBinding.txtQuantity.setText(String.valueOf(quantity + 1));
                }

            }
        });

        dialogBinding.imageExpand.setOnClickListener(v -> popUpProductImageExpandDialog());
        dialogBinding.btnMinus.setOnClickListener(v -> {
            int quantity = Integer.parseInt(dialogBinding.txtQuantity.getText().toString().trim());
            if (quantity > 1) {
                dialogBinding.txtQuantity.setText(String.valueOf(quantity - 1));
            }

        });

        dialogBinding.imageClose.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnAddToCart.setOnClickListener(v -> {
            if (thisProduct.getNumberInCart() > thisProduct.getInStock()) {
                showToast(ProductDetailActivity.this, "Uiii, số lượng sản phẩm không đủ!");
            } else {
                int quantity = Integer.parseInt(dialogBinding.txtQuantity.getText().toString().trim());
                addToCart(storeName, thisProduct, quantity);
                showToast(ProductDetailActivity.this, "Đã thêm sản phẩm vào giỏ hàng");
                updateQuantityInCart(binding.txtQuantityInCart);
                dialog.dismiss();



            }

        });
    }

    private void popUpBuyNowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogBuyNowBinding dialogBinding = DialogBuyNowBinding.inflate(getLayoutInflater());
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_edit_text_border);
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);

            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            dialogBinding.getRoot().startAnimation(slideUp);
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedOldPrice = formatter.format(thisProduct.getOldPrice());
        dialogBinding.txtOldPrice.setText("đ" + formattedOldPrice);
        dialogBinding.txtOldPrice.setPaintFlags(binding.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        String formattedNewPrice = formatter.format(thisProduct.getNewPrice());

        dialogBinding.txtNewPrice.setText(formattedNewPrice);
        dialogBinding.txtInStock.setText("Kho: " + thisProduct.getInStock());

        //reset số lượng trên dialog sau mỗi lần popup dialog

        dialogBinding.txtQuantity.setText(String.valueOf(1));

        dialogBinding.btnPlus.setOnClickListener(v -> {
            int quantity = Integer.parseInt(dialogBinding.txtQuantity.getText().toString().trim());
            if (quantity < 99) {
                dialogBinding.txtQuantity.setText(String.valueOf(quantity + 1));
            }

        });

        dialogBinding.imageExpand.setOnClickListener(v -> popUpProductImageExpandDialog());
        dialogBinding.btnMinus.setOnClickListener(v -> {
            int quantity = Integer.parseInt(dialogBinding.txtQuantity.getText().toString().trim());
            if (quantity > 1) {
                dialogBinding.txtQuantity.setText(String.valueOf(quantity - 1));
            }

        });

        dialogBinding.imageClose.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnBuyNow.setOnClickListener(v -> {
            if (thisProduct.getNumberInCart() > thisProduct.getInStock()) {
                showToast(ProductDetailActivity.this, "Uiii, số lượng sản phẩm không đủ!");
            } else {
                Intent intent = new Intent(ProductDetailActivity.this, PaymentActivity.class);
                ArrayList<CartItem> payment = new ArrayList<>();
                CartItem cartItem = new CartItem();
                ArrayList<Product> listProducts = new ArrayList<>();
                int quantity = Integer.parseInt(dialogBinding.txtQuantity.getText().toString().trim());
                thisProduct.setNumberInCart(quantity);

                listProducts.add(thisProduct);
                cartItem.setStoreName(storeName);
                cartItem.setListProducts(listProducts);
                payment.add(cartItem);

                intent.putExtra("payment", payment);
                startActivity(intent);
            }

        });
    }


    private void popUpProductImageExpandDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        DialogProductImageExpandBinding dialogBinding = DialogProductImageExpandBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        dialogBinding.getRoot().startAnimation(slideUp);

        dialogBinding.imageClose.setOnClickListener(v -> dialog.dismiss());
    }


    private void popUpProductDetailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutProductDetailBinding detailBinding = LayoutProductDetailBinding.inflate(getLayoutInflater());
        builder.setView(detailBinding.getRoot());
        AlertDialog dialog = builder.create();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_edit_text_border);
        dialog.show();


        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);

            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            detailBinding.getRoot().startAnimation(slideUp);
        }


        detailBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    private void initUI() {
        getWindow().setStatusBarColor(Color.parseColor("#F04D7F"));
        getWindow().setNavigationBarColor(Color.parseColor("#EFEFEF"));
        Objects.requireNonNull(getSupportActionBar()).hide();
        updateQuantityInCart(binding.txtQuantityInCart);
    }
}