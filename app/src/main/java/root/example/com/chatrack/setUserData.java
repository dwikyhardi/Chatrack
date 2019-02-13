package root.example.com.chatrack;

public class setUserData {
    public String UserId;
    public String Nama;
    public String Alamat;
    public String TanggalLahir;
    public String Latitude;
    public String Longitude;
    public String JenisKelamin;

    public setUserData(String UserId, String Nama, String Alamat,
                       String TanggalLahir, String Latitude, String Logitude, String JenisKelamin){
        this.UserId = UserId;
        this.Nama = Nama;
        this.Alamat = Alamat;
        this.TanggalLahir = TanggalLahir;
        this.Latitude = Latitude;
        this.Longitude = Logitude;
        this.JenisKelamin = JenisKelamin;
    }
}
