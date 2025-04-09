package MafiaG;

//TestOkhttp.java
import okhttp3.OkHttpClient;

public class TestOkhttp {
 public static void main(String[] args) {
     OkHttpClient client = new OkHttpClient.Builder().build();
     System.out.println("OkHttp 정상 작동");
 }
}
