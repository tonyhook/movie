package cc.tonyhook.movie.updater;

import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cc.tonyhook.movie.Imdb;
import cc.tonyhook.movie.ImdbRepository;
import cc.tonyhook.movie.Movie;
import cc.tonyhook.movie.MovieRepository;
import cc.tonyhook.movie.Poster;
import cc.tonyhook.movie.PosterRepository;

@Component
public class UpdaterImdb {
    @Autowired
    private ImdbRepository imdbRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private PosterRepository posterRepository;
    @Autowired
    private UpdaterPoster updaterPoster;

    public boolean updateImdbOnce(Imdb imdb) {
        try {
            String imdbUrl = "http://www.imdb.com/title/" + imdb.getImdb() + "/";
            String title = "";
            String releaseDate = "";
            String posterUrl = "";
            String oldPosterUrl = "";
            if (imdbRepository.findOne(imdb.getImdb()) != null)
                oldPosterUrl = imdbRepository.findOne(imdb.getImdb()).getPoster();

            URLConnection connection = new URL(imdbUrl).openConnection();
            connection.addRequestProperty("Accept-Language", "en-US");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", imdbUrl);
            Elements elements;

            // <h1 itemprop="name" class="">Coco&nbsp;<span id="titleYear">(<a
            // href="/year/2017/?ref_=tt_ov_inf">2017</a>)</span></h1>
            elements = doc.getElementsByTag("h1");
            for (Element element : elements) {
                String itemProp = element.attr("itemprop");

                if (itemProp.equals("name")) {
                    title = element.ownText();

                    imdb.setTitle(title);
                }
            }

            // <a href="XXX" title="See more release dates">21 November 2017 (USA)<meta
            // itemprop="datePublished" content="2017-11-21" /></a>
            elements = doc.getElementsByTag("a");
            for (Element element : elements) {
                String linkTitle = element.attr("title");

                if (linkTitle.equals("See more release dates")) {
                    Elements metas = element.getElementsByTag("meta");
                    if (metas.size() > 0) {
                        releaseDate = metas.get(0).attr("content");

                        if (releaseDate.length() == 4)
                            releaseDate = releaseDate + "-01-01";
                        if (releaseDate.length() == 7)
                            releaseDate = releaseDate + "-01";

                        imdb.setReleasedate(Date.valueOf(releaseDate));
                    }
                }
            }

            // <img alt="Coco Poster" title="Coco Poster"
            // src="https://images-na.ssl-images-amazon.com/images/M/MV5BYjQ5NjM0Y2YtNjZkNC00ZDhkLWJjMWItN2QyNzFkMDE3ZjAxXkEyXkFqcGdeQXVyODIxMzk5NjA@._V1_UY268_CR3,0,182,268_AL_.jpg"
            // itemprop="image" />
            elements = doc.getElementsByTag("div");
            for (Element element : elements) {
                String divClass = element.attr("class");

                if (divClass.equals("poster")) {
                    Elements imgs = element.getElementsByTag("img");

                    posterUrl = imgs.get(0).attr("src");
                    Integer lastDot = posterUrl.lastIndexOf(".");
                    String ext = posterUrl.substring(lastDot, posterUrl.length());
                    posterUrl = posterUrl.substring(0, lastDot);
                    lastDot = posterUrl.lastIndexOf(".");
                    posterUrl = posterUrl.substring(0, lastDot) + ext;

                    imdb.setPoster(posterUrl);
                }
            }

            if (imdbRepository.findOne(imdb.getImdb()) == null) {
                imdbRepository.save(imdb);

                if (posterUrl.length() > 0) {
                    Iterable<Movie> movies = movieRepository.findAllByImdb(imdb);
                    for (Movie movie : movies) {
                        Poster poster = new Poster();
                        poster.setImageurl(posterUrl);
                        poster.setMovieid(movie.getIdmovie());
                        poster = posterRepository.save(poster);
                        updaterPoster.updatePoster(poster);
                    }
                }
            } else {
                imdbRepository.save(imdb);

                if ((posterUrl.length() > 0) && !(posterUrl.equals(oldPosterUrl))) {
                    Iterable<Movie> movies = movieRepository.findAllByImdb(imdb);
                    for (Movie movie : movies) {
                        Poster poster = new Poster();
                        poster.setImageurl(posterUrl);
                        poster.setMovieid(movie.getIdmovie());
                        poster = posterRepository.save(poster);
                        updaterPoster.updatePoster(poster);
                    }
                }
            }

            return true;
        } catch (Throwable e) {
            System.out.println("IMDB: failed to get " + imdb.getImdb() + ", try again");
            return false;
        }
    }

    public void updateImdb(Imdb imdb) {
        int count = 0;
        while (count < 3) {
            if (!updateImdbOnce(imdb)) {
                count++;
            } else {
                return;
            }
        }
        System.out.println("IMDB: failed to get " + imdb.getImdb() + ", give up");
    }

    @Scheduled(fixedRate = 86400000, initialDelay = 3600000)
    public void updateImdbRepository() {
        Iterable<Imdb> imdbs = imdbRepository.findAll();

        for (Imdb imdb : imdbs) {
            updateImdb(imdb);
        }
    }

}
