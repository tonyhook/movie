package cc.tonyhook.movie.updater;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.sql.rowset.serial.SerialBlob;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cc.tonyhook.movie.domain.Album;
import cc.tonyhook.movie.domain.AlbumRepository;
import cc.tonyhook.movie.domain.Coverimg;
import cc.tonyhook.movie.domain.CoverimgRepository;
import cc.tonyhook.movie.domain.Movie;
import cc.tonyhook.movie.domain.MovieRepository;
import cc.tonyhook.movie.domain.Movie_company;

@Component
public class UpdaterCover {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private CoverimgRepository coverimgRepository;

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

    public Blob getCover(String coverUrl) {
        try {
            Response resp;
            resp = Jsoup.connect(coverUrl).ignoreContentType(true).maxBodySize(0).execute();

            return new SerialBlob(resp.bodyAsBytes());
        } catch (Throwable e) {
            System.out.println("Cover: failed to get " + coverUrl);
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateCoverOnce(Album album) {
        try {
            Movie movie = movieRepository.findOne(album.getMovieid());

            String companyName = null;
            if (movie.getMovie_companies() != null) {
                Set<Movie_company> movie_companies = movie.getMovie_companies();
                for (Movie_company movie_company : movie_companies) {
                    if (movie_company.getPreferred() == 1) {
                        companyName = movie_company.getCompany().getName();
                    }
                }
            }
            String movieName = movie.getReleasedate().toString().replace("-", ".") + "."
                    + movie.getTitle().replace(":", "").replace("!", "").replace("?", "").replace("/", " ");
            if (movieName.endsWith(".")) {
                movieName = movieName.substring(0, movieName.length() - 1);
            }
            String albumTitle = album.getTitle();

            Blob image = getCover("https://tonyhook.3322.org:4443/Movie/CD/"
                    + companyName + "/Soundtrack/" + movieName + "/"
                    + albumTitle + "/" + albumTitle + ".jpg");

            if (image != null) {
                Coverimg coverimg = new Coverimg();
                coverimg.setIdcoverimg(album.getIdalbum());
                coverimg.setImage(image);
                coverimgRepository.save(coverimg);
                return true;
            } else
                return false;
        } catch (Throwable e) {
            System.out.println("Cover: failed to get " + album.getTitle() + ", try again");
            e.printStackTrace();
            return false;
        }
    }

    public void updateCover(Album album) {
        int count = 0;
        while (count < 3) {
            if (!updateCoverOnce(album)) {
                count++;
            } else {
                return;
            }
        }
        System.out.println("Cover: failed to get " + album.getTitle() + ", give up");
    }

    @Scheduled(fixedRate = 500000, initialDelay = 0)
    @Transactional
    public void updateCoverRepository() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, get_trust_mgr(), new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String host, SSLSession sess) {
                    return true;
                }
            });
    
            Iterable<Album> albums = albumRepository.findAll();
    
            for (Album album : albums) {
                if (coverimgRepository.findOne(album.getIdalbum()) == null) {
                    updateCover(album);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
