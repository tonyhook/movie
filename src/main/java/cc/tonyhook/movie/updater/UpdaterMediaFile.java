package cc.tonyhook.movie.updater;

import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.util.HashSet;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cc.tonyhook.movie.domain.Akatitle;
import cc.tonyhook.movie.domain.Album;
import cc.tonyhook.movie.domain.AlbumRepository;
import cc.tonyhook.movie.domain.Company;
import cc.tonyhook.movie.domain.CompanyRepository;
import cc.tonyhook.movie.domain.Movie;
import cc.tonyhook.movie.domain.MovieRepository;
import cc.tonyhook.movie.domain.Movie_company;

@Component
public class UpdaterMediaFile {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private AlbumRepository albumRepository;

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
            return null;
        }

    }

    private Set<String> getAlbums(String companyName, String movieName) {
        Set<String> albums = new HashSet<String>();

        try {
            String path = companyName + "/Soundtrack/" + movieName;
            URI uri = new URI("https", "//tonyhook.3322.org:4443/Movie/CD/" + path + "/", null);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, get_trust_mgr(), new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection connection = (HttpsURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String host, SSLSession sess) {
                    return true;
                }
            });

            Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8",
                    "https://tonyhook.3322.org:4443/Movie/CD/" + path + "/");
            Elements elements = doc.getElementsByTag("tr");
            for (Element element : elements) {
                if (element.getElementsByTag("img").size() == 0)
                    continue;
                Element img = element.getElementsByTag("img").get(0);
                if (img.attr("src").equals("/icons/folder.gif")) {
                    String albumName = element.getElementsByTag("a").get(0).ownText();
                    albumName = albumName.substring(0, albumName.length() - 1);
                    albums.add(albumName);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return albums;
    }

    public void UpdateAudioFile() {
        try {
            String url = "https://tonyhook.3322.org:4443/Movie/CD/";
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, get_trust_mgr(), new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            Set<String> movieFolders = new HashSet<String>();
            Iterable<Company> companies = companyRepository.findAll();

            for (Company company : companies) {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url + company.getName() + "/Soundtrack/").openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String host, SSLSession sess) {
                        return true;
                    }
                });

                Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", url + company.getName() + "/Soundtrack/");
                Elements elements;

                elements = doc.getElementsByTag("a");
                for (Element element : elements) {
                    String folderName = element.ownText().substring(0, element.ownText().length() - 1);

                    if (folderName.substring(0, 1).matches("[0-9]")) {
                        movieFolders.add(company.getName() + "/Soundtrack/" + folderName);
                    }
                }
            }

            Iterable<Movie> movies = movieRepository.findAll();

            for (Movie movie : movies) {
                if (movie.getReleasedate() != null) {
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
                    String path = companyName + "/Soundtrack/" + movieName;

                    if (movieFolders.contains(path)) {
                        movieFolders.remove(path);

                        Set<String> AlbumTitles = getAlbums(companyName, movieName);
                        movie.setAudio(AlbumTitles.size());

                        for (String albumTitle : AlbumTitles) {
                            if (albumRepository.findAllByMovieidAndTitle(movie.getIdmovie(), albumTitle).size() == 0) {
                                Album newAlbum = new Album();
                                newAlbum.setMovieid(movie.getIdmovie());
                                newAlbum.setTitle(albumTitle);
                                newAlbum.setType("Soundtrack");
                                newAlbum.setCover(getCover("https://tonyhook.3322.org:4443/Movie/CD/" + path + "/"
                                        + albumTitle + "/" + albumTitle + ".jpg"));
                                albumRepository.save(newAlbum);
                            }
                        }
                    } else {
                        movie.setAudio(0);
                    }

                    movieRepository.save(movie);
                }
            }

            for (String movieFolder : movieFolders) {
                System.out.println("Audio: no movie matches " + movieFolder);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void UpdateVideoFile() {
        try {
            String url = "https://tonyhook.3322.org:4443/Movie/Movie/";
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, get_trust_mgr(), new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            Set<String> movieFolders = new HashSet<String>();
            Iterable<Company> companies = companyRepository.findAll();

            for (Company company : companies) {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url + company.getName() + "/000.All/").openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String host, SSLSession sess) {
                        return true;
                    }
                });

                Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", url + company.getName() + "/000.All/");
                Elements elements;

                elements = doc.getElementsByTag("a");
                for (Element element : elements) {
                    String folderName = element.ownText().substring(0, element.ownText().length() - 1);

                    if (folderName.substring(0, 1).matches("[0-9]")) {
                        movieFolders.add(company.getName() + "/000.All/" + folderName);
                    }
                }
            }

            Iterable<Movie> movies = movieRepository.findAll();

            for (Movie movie : movies) {
                if (movie.getReleasedate() != null) {
                    String companyName = null;
                    String title_cn = "";

                    if (movie.getMovie_companies() != null) {
                        Set<Movie_company> movie_companies = movie.getMovie_companies();
                        for (Movie_company movie_company : movie_companies) {
                            if (movie_company.getPreferred() == 1) {
                                companyName = movie_company.getCompany().getName();
                            }
                        }
                    }

                    if (movie.getAkatitles() != null) {
                        Set<Akatitle> akatitles = movie.getAkatitles();
                        for (Akatitle akatitle : akatitles) {
                            if (akatitle.getLanguage().equals("Chinese")) {
                                title_cn = akatitle.getTitle();
                            }
                        }
                    }

                    String path = companyName + "/000.All/"
                            + movie.getReleasedate().toString().replace("-", ".") + "."
                            + movie.getTitle().replace(":", "").replace("!", "").replace("?", "").replace("/", " ")
                            + "【" + title_cn + "】";
                    if (movieFolders.contains(path)) {
                        movieFolders.remove(path);
                        movie.setVideo(1);
                    } else {
                        movie.setVideo(0);
                    }

                    movieRepository.save(movie);
                }
            }

            for (String movieFolder : movieFolders) {
                System.out.println("Video: no movie matches " + movieFolder);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 86400000, initialDelay = 0)
    @Transactional
    public void UpdateMediaFile() {
        UpdateVideoFile();
        UpdateAudioFile();
    }

}
