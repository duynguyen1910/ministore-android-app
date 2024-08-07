package api;

import static constants.collectionName.INVOICE_COLLECTION;
import static constants.collectionName.INVOICE_DETAIL_COLLECTION;
import static constants.collectionName.PRODUCT_COLLECTION;
import static constants.collectionName.VARIANT_COLLECTION;
import static constants.keyName.CREATE_AT;
import static constants.keyName.CUSTOMER_ID;
import static constants.keyName.INVOICE_ID;
import static constants.keyName.STATUS;
import static constants.keyName.STORE_ID;
import static constants.toastMessage.CONFIRMED_ORDER_SUCCESSFULLY;
import static constants.toastMessage.INTERNET_ERROR;
import static constants.toastMessage.ORDER_SUCCESSFULLY;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import interfaces.CreateDocumentCallback;
import interfaces.GetAggregateCallback;
import interfaces.GetCollectionCallback;
import interfaces.StatusCallback;
import interfaces.UpdateDocumentCallback;
import models.Invoice;
import models.InvoiceDetail;
import models.Product;
import models.Variant;
import utils.Cart.CartUtils;

public class invoiceApi {
    private FirebaseFirestore db;

    public invoiceApi() {
        db = FirebaseFirestore.getInstance();
    }

    public void createInvoiceApi(Map<String, Object> newInvoice, final CreateDocumentCallback callback) {
        db.collection(INVOICE_COLLECTION)
                .add(newInvoice)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        callback.onCreateSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onCreateFailure("Failed to create invoice: " + e.getMessage());
                    }
                });
    }

    public void createDetailInvoiceApi(ArrayList<InvoiceDetail> invoiceItems,
                                       final StatusCallback callback) {
        WriteBatch batch = db.batch();
        for (InvoiceDetail detail : invoiceItems) {
            DocumentReference detailRef = db.collection(INVOICE_DETAIL_COLLECTION).document();
            Map<String, Object> newDetail = new HashMap<>();
            newDetail.put("invoiceID", detail.getInvoiceID());
            newDetail.put("quantity", detail.getQuantity());

            if (detail.getVariantID() != null) {
                newDetail.put("variantID", detail.getVariantID());
            } else {
                // Add productID if variantID is null
                newDetail.put("productID", detail.getProductID());
            }
            batch.set(detailRef, newDetail);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess(ORDER_SUCCESSFULLY);
                    CartUtils.clearMyCart();
                } else {
                    callback.onFailure("Failed to create invoice details: " + task.getException().getMessage());
                }
            }
        });
    }


//    public void createDetailInvoiceApi(ArrayList<InvoiceDetail> invoiceItems,
//                                       final StatusCallback callback) {
//        WriteBatch batch = db.batch();
//        for (InvoiceDetail detail : invoiceItems) {
//            DocumentReference detailRef = db.collection(INVOICE_DETAIL_COLLECTION).document();
//            Map<String, Object> newDetail = new HashMap<>();
//            newDetail.put("invoiceID", detail.getInvoiceID());
//            newDetail.put("variantID", detail.getVariantID());
//            newDetail.put("quantity", detail.getQuantity());
//            batch.set(detailRef, newDetail);
//        }
//
//        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    callback.onSuccess(ORDER_SUCCESSFULLY);
//                    CartUtils.clearMyCart();
//                } else {
//                    callback.onFailure("Failed to create invoice details: " + task.getException().getMessage());
//                }
//            }
//        });
//    }

    public void getRevenueByStoreID(String storeID, GetAggregateCallback callback) {
        List<Integer> orderStatuses = new ArrayList<>();
        orderStatuses.add(2);
        orderStatuses.add(3);
        orderStatuses.add(4);
        db.collection(INVOICE_COLLECTION)
                .whereEqualTo(STORE_ID, storeID)
                .whereIn(STATUS, orderStatuses)
                .get()
                .addOnSuccessListener(task -> {
                    double sumTotal = 0;
                    for (DocumentSnapshot document : task.getDocuments()) {
                        Invoice invoice = document.toObject(Invoice.class);
                        double total = invoice.getTotal();
                        sumTotal += total;
                    }
                    callback.onSuccess(sumTotal);
                }).addOnFailureListener(e -> {
                    callback.onFailure(INTERNET_ERROR);
                });

    }

    public void getSpendingsByCustomerID(String customerID, GetAggregateCallback callback) {
        List<Integer> orderStatuses = new ArrayList<>();
        orderStatuses.add(2);
        orderStatuses.add(3);
        orderStatuses.add(4);
        db.collection(INVOICE_COLLECTION)
                .whereEqualTo(CUSTOMER_ID, customerID)
                .whereIn(STATUS, orderStatuses)
                .get()
                .addOnSuccessListener(task -> {
                    double spendings = 0;
                    for (DocumentSnapshot document : task.getDocuments()) {
                        Invoice invoice = document.toObject(Invoice.class);
                        double total = invoice.getTotal();
                        spendings += total;
                    }
                    callback.onSuccess(spendings);
                }).addOnFailureListener(e -> {
                    callback.onFailure(INTERNET_ERROR);
                });

    }

    public int getMonthInCalendar(int month) {
        int[] calendarMonths = {
                Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH,
                Calendar.APRIL, Calendar.MAY, Calendar.JUNE,
                Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER,
                Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER};
        return calendarMonths[month - 1];
    }

    public Timestamp[] getDayRange(int month) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2024, getMonthInCalendar(month), 1, 0, 0, 0); // Set to 00:00:00 of the 1st day
        Timestamp startDate = new Timestamp(startCalendar.getTime());

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(2024, getMonthInCalendar(month), endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59); // Set to 23:59:59 of the last day
        Timestamp endDate = new Timestamp(endCalendar.getTime());

        return new Timestamp[]{startDate, endDate};
    }


    public void getSpendingsInAMonthByCustomerID(String customerID, int month, GetAggregateCallback callback) {
        List<Integer> orderStatuses = new ArrayList<>();
        orderStatuses.add(2);
        orderStatuses.add(3);
        orderStatuses.add(4);
        db.collection(INVOICE_COLLECTION)
                .whereEqualTo(CUSTOMER_ID, customerID)
                .whereIn(STATUS, orderStatuses)
                .whereGreaterThanOrEqualTo(CREATE_AT, getDayRange(month)[0])
                .whereLessThanOrEqualTo(CREATE_AT, getDayRange(month)[1])
                .get()
                .addOnSuccessListener(task -> {
                    double spendings = 0;
                    for (DocumentSnapshot document : task.getDocuments()) {
                        Invoice invoice = document.toObject(Invoice.class);
                        double total = invoice.getTotal();
                        spendings += total;
                    }
                    callback.onSuccess(spendings);
                }).addOnFailureListener(e -> {
                    callback.onFailure(INTERNET_ERROR);
                });

    }

    public void getRevenueForAllMonthsByStoreID(String storeID, GetCollectionCallback<Double> callback) {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            // Tạo các task cho từng tháng
            tasks.add(getRevenueInAMonthTask(storeID, month));
        }

        // Sử dụng Tasks.whenAll để thực hiện tất cả các task và nhận kết quả
        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<Double> monthlyRevenues = new ArrayList<>();
                for (int i = 0; i < tasks.size(); i++) {
                    Task<QuerySnapshot> t = tasks.get(i);
                    double revenue = 0;
                    try {
                        for (DocumentSnapshot document : t.getResult().getDocuments()) {
                            Invoice invoice = document.toObject(Invoice.class);
                            double total = invoice.getTotal();
                            revenue += total;
                        }
                    } catch (Exception e) {
                        Log.d("getRevenueForAllMonthsByStoreID", "Error processing month " + (i + 1) + ": " + e.getMessage());
                    }
                    monthlyRevenues.add(revenue);
                }
                callback.onGetListSuccess(monthlyRevenues);
            } else {
                callback.onGetListFailure(INTERNET_ERROR);
            }
        });
    }

    private Task<QuerySnapshot> getRevenueInAMonthTask(String storeID, int month) {
        List<Integer> orderStatuses = Arrays.asList(2, 3, 4);
        return db.collection(INVOICE_COLLECTION)
                .whereEqualTo(STORE_ID, storeID)
                .whereIn(STATUS, orderStatuses)
                .whereGreaterThanOrEqualTo(CREATE_AT, getDayRange(month)[0])
                .whereLessThanOrEqualTo(CREATE_AT, getDayRange(month)[1])
                .get();
    }

    public void getRevenueInAMonthByStoreID(String storeID, int month, GetAggregateCallback callback) {
        List<Integer> orderStatuses = new ArrayList<>();
        orderStatuses.add(2);
        orderStatuses.add(3);
        orderStatuses.add(4);
        db.collection(INVOICE_COLLECTION)
                .whereEqualTo(STORE_ID, storeID)
                .whereIn(STATUS, orderStatuses)
                .whereGreaterThanOrEqualTo(CREATE_AT, getDayRange(month)[0])
                .whereLessThanOrEqualTo(CREATE_AT, getDayRange(month)[1])
                .get()
                .addOnSuccessListener(task -> {
                    double spendings = 0;
                    for (DocumentSnapshot document : task.getDocuments()) {
                        Invoice invoice = document.toObject(Invoice.class);
                        double total = invoice.getTotal();
                        spendings += total;
                    }
                    callback.onSuccess(spendings);
                }).addOnFailureListener(e -> {
                    callback.onFailure(INTERNET_ERROR);
                });

    }

    public void getInvoicesByStatusApi(String customerID, int invoiceStatus, final GetCollectionCallback<Invoice> callback) {
        db.collection(INVOICE_COLLECTION)
                .whereEqualTo(CUSTOMER_ID, customerID)
                .whereEqualTo(STATUS, invoiceStatus)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Invoice> invoices = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Invoice invoice = document.toObject(Invoice.class);
                                invoice.setBaseID(document.getId());
                                invoice.setStatus(document.getLong(STATUS).intValue());

                                invoices.add(invoice);
                            }

                            callback.onGetListSuccess(invoices);
                        } else {
                            callback.onGetListFailure("Failed to get invoices: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void getDeliveryInvoicesByStatusApi(int invoiceStatus, final GetCollectionCallback<Invoice> callback) {
        db.collection(INVOICE_COLLECTION)
                .whereEqualTo(STATUS, invoiceStatus)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Invoice> invoices = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Invoice invoice = document.toObject(Invoice.class);
                                invoice.setBaseID(document.getId());
                                invoice.setStatus(document.getLong(STATUS).intValue());

                                invoices.add(invoice);
                            }

                            callback.onGetListSuccess(invoices);
                        } else {
                            callback.onGetListFailure("Failed to get invoices: " + task.getException().getMessage());
                        }
                    }
                });
    }


    public void getInvoiceDetailApi(String invoiceID, final GetCollectionCallback<InvoiceDetail> callback) {
        final String[] storeID = {""};

        // Tìm trong bảng invoice để lấy storeID của cửa hàng chứa invoiceID này
        db.collection(INVOICE_COLLECTION)
                .document(invoiceID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Invoice invoice = documentSnapshot.toObject(Invoice.class);
                        storeID[0] = invoice.getStoreID();

                        // Tìm tất cả invoice Detail thỏa mãn hóa đơn muốn tìm

                        db.collection(INVOICE_DETAIL_COLLECTION)
                                .whereEqualTo(INVOICE_ID, invoiceID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            ArrayList<InvoiceDetail> invoiceDetails = new ArrayList<>();
                                            AtomicInteger remainingDetails = new AtomicInteger(task.getResult().size());

                                            // Task lấy invoice Detail chia thành 2 hướng
                                            // 1. Hướng lấy invoice detail đối với sản phẩm có variant
                                            // 2. Hướng lấy invoice detail đối với sản phẩm không có variant

                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                InvoiceDetail detail = document.toObject(InvoiceDetail.class);
                                                String variantID = detail.getVariantID();

                                                if (variantID != null) {
                                                    // Handle case with variantID
                                                    fetchVariantDetails(storeID[0], variantID, detail, invoiceDetails, remainingDetails, callback);
                                                } else {
                                                    // Handle case with no variantID (use productID if available)
                                                    String productID = detail.getProductID();
                                                    if (productID != null) {
                                                        fetchProductDetails(storeID[0], productID, detail, invoiceDetails, remainingDetails, callback);
                                                    } else {
                                                        // Handle the case where both variantID and productID are null, if needed
                                                        remainingDetails.decrementAndGet();
                                                    }
                                                }
                                            }

                                            // Check if no pending requests and notify callback
                                            if (remainingDetails.get() == 0) {
                                                callback.onGetListSuccess(invoiceDetails);
                                            }
                                        } else {
                                            callback.onGetListFailure("Failed to get invoice details: " + task.getException().getMessage());
                                        }
                                    }
                                });
                    }
                });
    }


    private void fetchVariantDetails(String storeID, String variantID, final InvoiceDetail detail,
                                     final ArrayList<InvoiceDetail> invoiceDetails, final AtomicInteger remainingDetails, final GetCollectionCallback<InvoiceDetail> callback) {
        db.collection(VARIANT_COLLECTION)
                .document(variantID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Variant variant = documentSnapshot.toObject(Variant.class);
                        if (variant != null) {
                            String productID = variant.getProductID();
                            db.collection(PRODUCT_COLLECTION)
                                    .document(productID)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Product product = documentSnapshot.toObject(Product.class);
                                            if (product != null) {
                                                detail.setProductName(product.getProductName());
                                            }
                                            detail.setProductImage(variant.getVariantImageUrl());
                                            detail.setVariantName(variant.getVariantName());
                                            detail.setNewPrice(variant.getNewPrice());
                                            detail.setOldPrice(variant.getOldPrice());
                                            detail.setStoreID(storeID);
                                            invoiceDetails.add(detail);
                                            if (remainingDetails.decrementAndGet() == 0) {
                                                callback.onGetListSuccess(invoiceDetails);
                                            }
                                        }
                                    });
                        } else {
                            // Handle case where variant is not found
                            remainingDetails.decrementAndGet();
                        }
                    }
                });
    }


    private void fetchProductDetails(String storeID, String productID, final InvoiceDetail detail,
                                     final ArrayList<InvoiceDetail> invoiceDetails, final AtomicInteger remainingDetails, final GetCollectionCallback<InvoiceDetail> callback) {
        db.collection(PRODUCT_COLLECTION)
                .document(productID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            detail.setProductName(product.getProductName());
                            detail.setProductImage(product.getProductImages().get(0));
                            detail.setNewPrice(product.getNewPrice());
                            detail.setOldPrice(product.getOldPrice());
                            detail.setStoreID(storeID);
                            invoiceDetails.add(detail);
                        }
                        if (remainingDetails.decrementAndGet() == 0) {
                            callback.onGetListSuccess(invoiceDetails);
                        }
                    }
                });
    }



//    public void getInvoiceDetailApi(String invoiceID, final GetCollectionCallback<InvoiceDetail> callback) {
//        db.collection(INVOICE_DETAIL_COLLECTION)
//                .whereEqualTo(INVOICE_ID, invoiceID)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            ArrayList<InvoiceDetail> invoiceDetails = new ArrayList<>();
//                            ArrayList<String> variantIDs = new ArrayList<>();
//
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                InvoiceDetail detail = document.toObject(InvoiceDetail.class);
//                                invoiceDetails.add(detail);
//                                variantIDs.add(detail.getVariantID());
//                            }
//
//                            getVariantsByListIDsApi(invoiceID, invoiceDetails, variantIDs, callback);
//                        } else {
//                            callback.onGetListFailure("Failed to get invoice details: " + task.getException().getMessage());
//                        }
//                    }
//                });
//    }
//
//
//    private void getVariantsByListIDsApi(String invoiceID, final ArrayList<InvoiceDetail> invoiceDetails, ArrayList<String> variantIDs, final GetCollectionCallback<InvoiceDetail> callback) {
//        final Map<String, Variant> variantMap = new HashMap<>();
//        final AtomicInteger pendingRequests = new AtomicInteger(variantIDs.size());
//
//        final String[] storeID = {""};
//
//        // Tìm trong bảng invoice để lấy storeID của cửa hàng chứa invoiceID này
//        db.collection(INVOICE_COLLECTION)
//                .document(invoiceID)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        Invoice invoice = documentSnapshot.toObject(Invoice.class);
//                        storeID[0] = invoice.getStoreID();
//                    }
//                });
//
//
//        // variantIDs là danh sách ID của các phân loại sản phẩm có trong hóa đơn
//        for (String variantID : variantIDs) {
//            db.collection(VARIANT_COLLECTION)
//                    .document(variantID)
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//                            Variant variant = documentSnapshot.toObject(Variant.class);
//                            if (variant != null) {
//                                String productID = variant.getProductID();
//                                db.collection(PRODUCT_COLLECTION)
//                                        .document(productID)
//                                        .get()
//                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                                Product product = documentSnapshot.toObject(Product.class);
//
//                                                if (product != null) {
//                                                    String productName = product.getProductName();
//                                                    variantMap.put(variantID, variant);
//
//                                                    if (pendingRequests.decrementAndGet() == 0) {
//                                                        for (InvoiceDetail detail : invoiceDetails) {
//                                                            Variant variant = variantMap.get(detail.getVariantID());
//                                                            if (variant != null) {
//                                                                detail.setProductName(productName);
//                                                                detail.setProductImage(variant.getVariantImageUrl());
//                                                                detail.setVariantName(variant.getVariantName());
//                                                                detail.setNewPrice(variant.getNewPrice());
//                                                                detail.setOldPrice(variant.getOldPrice());
//                                                                detail.setStoreID(storeID[0]);
//                                                            }
//                                                        }
//                                                        callback.onGetListSuccess(invoiceDetails);
//                                                    }
//                                                }
//                                            }
//                                        });
//                            }
//
//                        }
//                    });
//        }
//    }


    public void getInvoiceByStoreIDApi(String storeID, int invoiceStatus, final GetCollectionCallback<Invoice> callback) {
        db.collection(INVOICE_COLLECTION)
                .whereEqualTo(STORE_ID, storeID)
                .whereEqualTo(STATUS, invoiceStatus)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Invoice> invoices = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Invoice invoice = document.toObject(Invoice.class);
                                invoice.setBaseID(document.getId());
                                invoice.setStatus(document.getLong(STATUS).intValue());

                                invoices.add(invoice);
                            }

                            callback.onGetListSuccess(invoices);
                        } else {
                            callback.onGetListFailure("Failed to get invoices: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void updateStatusInvoiceApi(String invoiceID, Map<String, Object> invoiceUpdate, final UpdateDocumentCallback callback) {
        DocumentReference invoiceRef = db.collection(INVOICE_COLLECTION).document(invoiceID);
        invoiceRef.update(invoiceUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onUpdateSuccess(CONFIRMED_ORDER_SUCCESSFULLY);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onUpdateFailure(e.getMessage());
                    }
                });
    }


    public void countInvoices(Query query, final GetAggregateCallback callback) {
        query.get()
                .addOnSuccessListener(task -> {
                    int count = task.size();
                    callback.onSuccess(count);
                })
                .addOnFailureListener(e -> callback.onFailure(INTERNET_ERROR));
    }

    public void countRequestInvoicesByStoreIDAndStatus(String storeId, int status, final GetAggregateCallback callback) {
        Query query = db.collection(INVOICE_COLLECTION)
                .whereEqualTo(STORE_ID, storeId)
                .whereEqualTo(STATUS, status);
        countInvoices(query, callback);
    }

    public void countDeliveryInvoicesByStatus(int status, final GetAggregateCallback callback) {
        Query query = db.collection(INVOICE_COLLECTION)
                .whereEqualTo(STATUS, status);
        countInvoices(query, callback);
    }

}
