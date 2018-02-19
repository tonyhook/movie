package cc.tonyhook.movie.updater;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.tonyhook.movie.Movie_source;
import cc.tonyhook.movie.Source;
import cc.tonyhook.movie.SourceRepository;
import cc.tonyhook.movie.controller.MovieController;

@Component
public class UpdaterDisneyMovieSeries {
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private MovieController movieController;

    final String SOURCE_DISNEY_PIRATES = "Pirates of Caribbean";
    final String SOURCE_DISNEY_CARS = "Disney Cars";
    final String SOURCE_DISNEY_NATURE = "Disneynature";
    final String SOURCE_DISNEY_WINNIETHEPOOH = "Winnie the Pooh";
    final String SOURCE_DISNEY_BUDDIES = "Disney Buddies";
    final String SOURCE_DISNEY_FAIRIES = "Disney Fairies";
    final String SOURCE_DISNEY_PRINCESS = "Disney Princess";
    final String SOURCE_DISNEY_TOYSTORY = "Toy Story";
    final String SOURCE_DISNEY_MUPPETS = "The Muppets";
    final String SOURCE_DISNEY_FROZEN = "Frozen";
    final String SOURCE_DISNEY_STARWARS = "Star Wars";

    private void updateDisneyMovieSeriesByName(String name) {
        int idsource = 0;
        String url = "";

        Iterable<Source> sources = sourceRepository.findAll();
        for (Source source : sources) {
            if (source.getName().equals(name)) {
                idsource = source.getIdsource();
                url = source.getUrl() + "?intoverride=true";
            }
        }

        if (idsource == 0)
            return;

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.addRequestProperty("Accept-Language", "en-US");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", url);
            Elements elements;

            elements = doc.getElementsByTag("script");
            for (Element element : elements) {
                String script = element.data();
                if (script.startsWith("this.Grill?Grill.burger=")) {
                    String json = script.substring(24, script.indexOf(":(function()"));
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode movieList = mapper.readTree(json);
                    for (int i = 0; i < movieList.get("stack").size(); i++) {
                        for (int j = 0; j < movieList.get("stack").get(i).get("data").size(); j++) {
                            if (movieList.get("stack").get(i).get("type").asText().equals("movie") && (movieList
                                    .get("stack").get(i).get("view").asText().equals("slider")
                                    || movieList.get("stack").get(i).get("view").asText().equals("display")
                                    || movieList.get("stack").get(i).get("view").asText().equals("stream")
                                    || movieList.get("stack").get(i).get("view").asText().equals("featured_movie"))) {
                                JsonNode movie = movieList.get("stack").get(i).get("data").get(j);
                                Movie_source movie_source = new Movie_source();
                                movie_source.setSource(sourceRepository.findOne(idsource));
                                movie_source.setTitle(movie.get("title").asText());
                                movie_source.setOfficialid(movie.get("id").asText());
                                movie_source.setOfficialsite(movie.get("href").asText());
                                if (movie.get("poster") != null)
                                    movie_source.setPoster(movie.get("poster").asText().split("\\?")[0]);
                                if ((movie.get("release") != null) && !movie.get("release").asText().equals("null")) {
                                    try {
                                        String date_s = movie.get("release").asText();
                                        DateFormat dt = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
                                        Date date = null;
                                        date = dt.parse(date_s);
                                        movie_source.setReleasedate(new java.sql.Date(date.getTime()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                movieController.newMovie_source(movie_source);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println("DisneyMovieSeries: failed to sync, try next time");
        }
    }

    @Scheduled(fixedRate = 86400000, initialDelay = 3600000)
    public void UpdateDisneyMovieSeries() {
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_PIRATES);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_CARS);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_NATURE);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_WINNIETHEPOOH);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_BUDDIES);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_FAIRIES);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_PRINCESS);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_TOYSTORY);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_MUPPETS);
        // updateDisneyMovieSeriesByName(SOURCE_DISNEY_FROZEN);
        updateDisneyMovieSeriesByName(SOURCE_DISNEY_STARWARS);
    }

}
