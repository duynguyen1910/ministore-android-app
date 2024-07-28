package models.Address;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiAddressService {
    @GET("api-tinhthanh/1/0.htm")
    Call<TinhThanhResponse> getTinhThanh();

    @GET("api-tinhthanh/2/{idtinh}.htm")
    Call<QuanHuyenResponse> getQuanHuyen(@Path("idtinh") String idtinh);

    @GET("api-tinhthanh/3/{idquan}.htm")
    Call<PhuongXaResponse> getPhuongXa(@Path("idquan") String idquan);
}