package cc.tonyhook.movie.updater;

import java.net.URI;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
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

    private static String NAS_ADDRESS = "tonyhook.3322.org";
    private static int NAS_PORT = 4443;
    private static String NAS_SHARE = "Movie";

    private Set<String> getAlbumsOnce(String companyName, String movieName) {
        Set<String> albums = new HashSet<String>();

        try {
            URI uri = new URI("https", null, NAS_ADDRESS, NAS_PORT,
                    "/" + NAS_SHARE + "/CD/" + companyName + "/Soundtrack/" + movieName + "/", null, null);
            URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", uri.getPath());
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
            System.out.println("Album: failed to get " + companyName + "/" + movieName + ", try again");
            return null;
        }

        return albums;
    }

    private Set<String> getAlbums(String companyName, String movieName) {
        int count = 0;
        Set<String> albums = null;
        while (count < 3) {
            albums = getAlbumsOnce(companyName, movieName);
            if (albums == null) {
                count++;
            } else {
                return albums;
            }
        }
        System.out.println("Album: failed to get " + companyName + "/" + movieName + ", give up");
        return null;
    }

    private Set<String> getMovieFoldersOnce(String mediaType, String defaultPath) {
        Set<String> movieFolders = new HashSet<String>();

        try {
            Iterable<Company> companies = companyRepository.findAll();

            for (Company company : companies) {
                URI uri = new URI("https", null, NAS_ADDRESS, NAS_PORT,
                        "/" + NAS_SHARE + "/" + mediaType + "/" + company.getName() + "/" + defaultPath + "/", null, null);
                URLConnection connection = uri.toURL().openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", uri.getPath());
                Elements elements;

                elements = doc.getElementsByTag("a");
                for (Element element : elements) {
                    String folderName = element.ownText().substring(0, element.ownText().length() - 1);

                    if (folderName.substring(0, 1).matches("[0-9]")) {
                        movieFolders.add(company.getName() + "/" + defaultPath + "/" + folderName);
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println("MovieFolder: failed to get " + mediaType + ", try again");
            return null;
        }

        return movieFolders;
    }

    private Set<String> getMovieFolders(String mediaType, String defaultPath) {
        int count = 0;
        Set<String> movieFolders = null;
        while (count < 3) {
            movieFolders = getMovieFoldersOnce(mediaType, defaultPath);
            if (movieFolders == null) {
                count++;
            } else {
                return movieFolders;
            }
        }
        System.out.println("MovieFolder: failed to get " + mediaType + " list, give up");
        return null;
    }

    private String getDefaultCompanyName(Movie movie) {
        if (movie.getMovie_companies() != null) {
            Set<Movie_company> movie_companies = movie.getMovie_companies();
            for (Movie_company movie_company : movie_companies) {
                if (movie_company.getPreferred() == 1) {
                    return movie_company.getCompany().getName();
                }
            }
        }
        return "";
    }

    public void UpdateAudioFile() {
        Set<String> movieFolders = getMovieFolders("CD", "Soundtrack");

        if (movieFolders == null)
            return;

        Iterable<Movie> movies = movieRepository.findAll();

        for (Movie movie : movies) {
            if (movie.getReleasedate() != null) {
                String companyName = getDefaultCompanyName(movie);

                String movieName = movie.getReleasedate().toString().replace("-", ".") + "."
                        + movie.getTitle().replace(":", "").replace("!", "").replace("?", "").replace("/", " ");
                if (movieName.endsWith(".")) {
                    movieName = movieName.substring(0, movieName.length() - 1);
                }
                String path = companyName + "/Soundtrack/" + movieName;

                if (movieFolders.contains(path)) {
                    movieFolders.remove(path);

                    Set<String> AlbumTitles = getAlbums(companyName, movieName);
                    if (AlbumTitles != null) {
                        movie.setAudio(AlbumTitles.size());

                        for (String albumTitle : AlbumTitles) {
                            if (albumRepository.findAllByMovieidAndTitle(movie.getIdmovie(), albumTitle).size() == 0) {
                                Album newAlbum = new Album();
                                newAlbum.setMovieid(movie.getIdmovie());
                                newAlbum.setTitle(albumTitle);
                                newAlbum.setType("Soundtrack");
                                albumRepository.save(newAlbum);
                            }
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
    }

    public void UpdateVideoFile() {
        Set<String> movieFolders = getMovieFolders("Movie", "000.All");

        if (movieFolders == null)
            return;

        Iterable<Movie> movies = movieRepository.findAll();

        for (Movie movie : movies) {
            if (movie.getReleasedate() != null) {
                String companyName = getDefaultCompanyName(movie);

                String title_cn = "";
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
    }

    @Scheduled(fixedRate = 86400000, initialDelay = 0)
    @Transactional
    public void UpdateMediaFile() {
        UpdateVideoFile();
        UpdateAudioFile();
    }

}
