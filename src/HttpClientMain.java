import com.ift.http.HttpMethod;
import com.ift.http.HttpRequest;
import com.ift.http.HttpResponse;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpClientMain {

    public static void main(String[] args) throws Exception{
        HttpResponse httpResponse = HttpRequest
                .createHttpRequest("http://support.infotech.co.kr/")
                .setHttpMethod(HttpMethod.GET)
//                .setBody("usrId=asdasd&usrPw=asdas")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .execute();


        System.out.println(httpResponse.statusCode);
        System.out.println(httpResponse.responseHeader);
        System.out.println(httpResponse.responseBody);

    }
}





