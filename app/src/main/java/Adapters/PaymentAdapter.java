package Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stores.databinding.ItemPaymentBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import Models.CartItem;
import Models.Product;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<CartItem> list;

    public PaymentAdapter(Context context, ArrayList<CartItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentBinding binding = ItemPaymentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ItemPaymentBinding binding;

        public ViewHolder(ItemPaymentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CartItem cartItem = list.get(holder.getBindingAdapterPosition());
        ArrayList<Product> listProducts = cartItem.getListProducts();

        holder.binding.txtStoreName.setText(cartItem.getStoreName());
        ProductsListAdapterForPaymentItem adapter = new ProductsListAdapterForPaymentItem(context, cartItem.getListProducts());

        holder.binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
        holder.binding.recyclerViewProducts.setAdapter(adapter);

        holder.binding.txtQuantityProducts.setText("Tổng số tiền (" + listProducts.size() + " sản phẩm)");

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.binding.txtTotal.setText("đ" + formatter.format(getCartItemFee(cartItem)));
    }

    private double getCartItemFee(CartItem cartItem) {
        double fee = 0;
        for (Product product : cartItem.getListProducts()) {
            fee += (product.getPrice() * (1 - product.getSaleoff() / 100) * product.getNumberInCart());
        }
        return fee;
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}
