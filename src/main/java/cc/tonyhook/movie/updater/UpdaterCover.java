package cc.tonyhook.movie.updater;

import java.sql.Blob;
import java.util.Set;

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
    
    private static String NAS_ADDRESS = "https://tonyhook.3322.org:4443/Movie/";

    public Blob getCover(String coverUrl) {
        try {
            Response resp = Jsoup.connect(coverUrl).ignoreContentType(true).maxBodySize(0).execute();
            return new SerialBlob(resp.bodyAsBytes());
        } catch (Throwable e) {
            System.out.println("Cover: failed to get " + coverUrl);
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateCoverOnce(Album album) {
        try {
            Movie movie = movieRepository.findById(album.getMovieid()).orElse(null);

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

            Blob image = getCover(NAS_ADDRESS + "CD/"
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
            Iterable<Album> albums = albumRepository.findAll();
    
            for (Album album : albums) {
                if (coverimgRepository.findById(album.getIdalbum()).orElse(null) == null) {
                    updateCover(album);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
