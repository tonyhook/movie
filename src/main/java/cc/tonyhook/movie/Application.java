package cc.tonyhook.movie;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    private static ConfigurableApplicationContext ctx;

    private TrustManager[] get_trust_mgr() {
        TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String t) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String t) {
            }
        } };
        return certs;
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, get_trust_mgr(), new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String host, SSLSession sess) {
                    return true;
                }
            });
        } catch (Exception e) {
            System.out.println("Application: cannot init https context, terminated");
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        ctx = SpringApplication.run(Application.class, args);
    }
}
