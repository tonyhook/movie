package cc.tonyhook.movie.updater;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.tonyhook.movie.controller.MovieController;
import cc.tonyhook.movie.domain.Movie_source;
import cc.tonyhook.movie.domain.Source;
import cc.tonyhook.movie.domain.SourceRepository;

@Component
public class UpdaterDisneyMovieAll {
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private MovieController movieController;

    final String SOURCE_DISNEY_ALL = "Disney Movies (All)";

    @Scheduled(fixedRate = 86400000, initialDelay = 3600000)
    public void UpdateDisneyMovieAll() {
        int offset = 0;
        int lengthReturned = 1;
        Integer idsource = 0;

        Iterable<Source> sources = sourceRepository.findAll();
        for (Source source : sources) {
            if (source.getName().equals(SOURCE_DISNEY_ALL))
                idsource = source.getIdsource();
        }

        if (idsource == 0)
            return;

        try {
            String allMovieUrl = "http://movies.disney.com/_grill/more/all-movies?r=1-7&l=25&o=";
            while (lengthReturned > 0) {
                String allMovieInfo = allMovieUrl + String.valueOf(offset);
                System.out.println("DisneyMovieAll: Syncing " + allMovieInfo);
                URLConnection connection = new URL(allMovieInfo).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode movieList = mapper.readTree(connection.getInputStream());

                lengthReturned = movieList.size();
                for (int i = 0; i < lengthReturned; i++) {
                    JsonNode movie = movieList.get(i);
                    Movie_source movie_source = new Movie_source();
                    movie_source.setSource(sourceRepository.findById(idsource).orElse(null));
                    movie_source.setTitle(movie.get("title").asText());
                    movie_source.setOfficialid(movie.get("id").asText());
                    movie_source.setOfficialsite(movie.get("href").asText());
                    if (movie.get("poster") != null)
                        movie_source.setPoster(movie.get("poster").asText().split("\\?")[0]);
                    if (movie.get("release") != null) {
                        try {
                            String date_s = movie.get("release").asText();
                            DateFormat dt = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
                            Date date = dt.parse(date_s);
                            movie_source.setReleasedate(new java.sql.Date(date.getTime()));
                        } catch (ParseException e) {
                        }
                    }

                    movieController.newMovie_source(movie_source);
                }
                offset += lengthReturned;
            }

            String[] typenames = { "All", "Adventure", "Animation", "Comedy", "Classics", "Documentary", "Drama",
                    "Family", "Fantasy", "Live Action", "Musicals", "Preschool", "Romance", "Science Fiction", "Sports",
                    "Suspense" };
            for (String typename : typenames) {
                offset = 0;
                lengthReturned = 1;

                String allTypedMovieUrl = "http://movies.disney.com/all-filter.json?f="
                        + URLEncoder.encode(typename, "UTF-8") + "&l=25&o=";
                while (lengthReturned > 0) {
                    String allTypedMovieInfo = allTypedMovieUrl + String.valueOf(offset);
                    System.out.println("DisneyMovieAll: Syncing " + allTypedMovieInfo);
                    URLConnection connection = new URL(allTypedMovieInfo).openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode movieList = mapper.readTree(connection.getInputStream());

                    lengthReturned = movieList.size();
                    for (int i = 0; i < lengthReturned; i++) {
                        JsonNode movie = movieList.get(i);
                        Movie_source movie_source = new Movie_source();
                        movie_source.setSource(sourceRepository.findById(idsource).orElse(null));
                        movie_source.setTitle(movie.get("title").asText());
                        movie_source.setOfficialid(movie.get("id").asText());
                        movie_source.setOfficialsite(movie.get("href").asText());
                        if (movie.get("poster") != null)
                            movie_source.setPoster(movie.get("poster").asText().split("\\?")[0]);
                        if (movie.get("release") != null) {
                            try {
                                String date_s = movie.get("release").asText();
                                DateFormat dt = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
                                Date date = dt.parse(date_s);
                                movie_source.setReleasedate(new java.sql.Date(date.getTime()));
                            } catch (ParseException e) {
                            }
                        }

                        movieController.newMovie_source(movie_source);
                    }
                    offset += lengthReturned;
                }
            }
        } catch (Throwable e) {
            System.out.println("DisneyMovieAll: failed to sync, try next time");
        }
    }

}
