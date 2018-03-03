package cc.tonyhook.movie.updater;

import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class UpdaterDisneyMoviesAnywhere {
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private MovieController movieController;

    final String SOURCE_DISNEY_DISNEYMOVIESANYWHERE = "DisneyMoviesAnywhere";

    @Scheduled(fixedRate = 86400000, initialDelay = 3600000)
    public void UpdateDisneyMoviesAnywhere() {
        int idsource = 0;

        Iterable<Source> sources = sourceRepository.findAll();
        for (Source source : sources) {
            if (source.getName().equals(SOURCE_DISNEY_DISNEYMOVIESANYWHERE))
                idsource = source.getIdsource();
        }

        if (idsource == 0)
            return;

        String[] typenames = { "all", "disney-pixar", "marvel", "star-wars", "disney-nature", "classics", "animation",
                "action-and-adventure", "disney-fairies", "comedies", "song-and-dance", "princess", "sports", "romance",
                "muppets", "true-story", "dogs", "mickey-mouse", "winnie-the-pooh", "documentaries", "westerns",
                "scifi" };
        for (String typename : typenames) {
            try {
                String url = "http://prod-northstar-domain.disneymoviesanywhere.com/domain/categories/" + typename;
                System.out.println("DisneyMoviesAnywhere: Syncing " + url);
                URLConnection connection = new URL(url).openConnection();
                connection.addRequestProperty("Device", "Web");
                connection.addRequestProperty("API-Version", "1.49");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode movieList = mapper.readTree(connection.getInputStream());

                for (int i = 0; i < movieList.get("items").size(); i++) {
                    JsonNode movie = movieList.get("items").get(i);
                    Movie_source movie_source = new Movie_source();
                    movie_source.setSource(sourceRepository.findOne(idsource));
                    movie_source.setTitle(movie.get("title").asText());
                    movie_source.setOfficialid(movie.get("guid").asText());
                    movie_source.setOfficialsite(movie.get("action").asText());
                    if (movie.get("imageUrl") != null)
                        movie_source.setPoster(movie.get("imageUrl").asText());
                    if ((movie.get("sortDate") != null) && (movie.get("sortDate").asText().length() > 0)) {
                        try {
                            String date_s = movie.get("sortDate").asText();
                            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = dt.parse(date_s);
                            movie_source.setReleasedate(new java.sql.Date(date.getTime()));
                        } catch (ParseException e) {
                        }
                    }

                    movieController.newMovie_source(movie_source);
                }
            } catch (Exception e) {
                System.out.println("DisneyMoviesAnywhere: failed to sync, try next time");
            }
        }
    }

}
