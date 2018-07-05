package cc.tonyhook.movie.updater;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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
public class UpdaterMoviesAnywhere {
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private MovieController movieController;

    final String SOURCE_DISNEY_DISNEYMOVIESANYWHERE = "MoviesAnywhere";

    @Scheduled(fixedRate = 86400000, initialDelay = 3600000)
    public void UpdateMoviesAnywhere() {
        Integer idsource = 0;

        Iterable<Source> sources = sourceRepository.findAll();
        for (Source source : sources) {
            if (source.getName().equals(SOURCE_DISNEY_DISNEYMOVIESANYWHERE))
                idsource = source.getIdsource();
        }

        if (idsource == 0)
            return;

        try {
            String url = "https://cognito.moviesanywhere.com/graphql";
            String query = "query=+fragment+ActionsFragment+on+Action+%7B+__typename+id+subtitle+title+...+on+DetailAction+%7B+actionContext%3A+actionContextV2+target+%7D+...+on+FavoriteAction+%7B+favoriteState+%7B+key+state+%7D+%7D+...+on+FollowAction+%7B+followed+%7B+key+state+%7D+playable+%7B+id+title+__typename+%7D+%7D+...+on+InternalLinkAction+%7B+actionContext%3A+actionContextV2+target+%7D+...+on+LinkAccountAction+%7B+providerId+title+%7D+...+on+LockedAction+%7B+message+%7D+...+on+PlayAction+%7B+actionContext%3A+actionContextV2+playerData+%7B+...PlayerDataFragment+%7D+%7D+...+on+SignInAction+%7B+title+%7D+...+on+SignUpAction+%7B+title+%7D+...+on+StoreAction+%7B+actionContext%3A+actionContextV2+typeCode+umidEdition+retailers+%7B+id+isLinked+name+purchaseUrl+fullColorImage+%7B+url+%7D+%7D+%7D+...+on+URLLinkAction+%7B+title+url+%7D+%7D+fragment+AuthorizationHackFragment+on+Query+%7B+authHackInfo%3A+search%28q%3A+%24authorization%29+%7B+slug+%7D+%7D+fragment+BoxArtItemFragment+on+SliderComponentItem+%7B+id+title+image+%7B+url+%7D+primaryAction+%7B+...ActionsFragment+%7D+%7D+fragment+CollectionItemFragment+on+SliderComponentItem+%7B+id+title+image+%7B+url+%7D+primaryAction+%7B+...ActionsFragment+%7D+%7D+fragment+PlayerDataFragment+on+PlayerData+%7B+playerCookie+playable+%7B+__typename+duration+guid+id+title+bif%3A+assets%28tags%3A+%22low-res%22%29+%7B+...+on+Biff+%7B+src%3A+url+%7D+%7D+chromecast%3A+assets%28tags%3A+%22dash%22%2C+sort%3A+%22-height%22%2C+first%3A+1%29+%7B+...+on+Video+%7B+url+widevineLAURL+%7D+%7D+dash%3A+assets%28tags%3A+%22dash%22%2C+sort%3A+%22-height%22%29+%7B+...+on+Video+%7B+url+playreadyLAURL+widevineLAURL+%7D+%7D+hls%3A+assets%28tags%3A+%22hls%22%2C+sort%3A+%22-height%22%2C+first%3A+1%29+%7B+...+on+Video+%7B+fairplayCertURL+fairplayLAURL+url+%7D+%7D+...+on+Feature+%7B+chapters%3A+chaptersWebVTT+%7B+src%3A+url+%7D+%7D+keyArtImage+%7B+aspectRatioFractions+alt+url+%7D+heroImage+%7B+alt+url+%7D+%7D+profileInfo+%7B+bookmark+%7B+percentage+position+%7D+%7D+%7D+fragment+VideoItemFragment+on+SliderComponentItem+%7B+id+title+image+%7B+url+%7D+primaryAction+%7B+...ActionsFragment+%7D+%7D+query+%28%24q%3A+String%2C+%24authorization%3A+String%29+%7B+search%28q%3A+%24q%29+%7B+components+%7B+__typename+id+...+on+BoxArtGridComponent+%7B+title+itemsMeta+%7B+defaultSortOption+%7B+code+name+%7D+sortOptions+%7B+code+name+%7D+%7D+paginatedItems%3A+items+%7B+...BoxArtItemFragment+%7D+%7D+...+on+BoxArtSliderComponent+%7B+title+items+%7B+...BoxArtItemFragment+%7D+%7D+...+on+CollectionSliderComponent+%7B+title+items+%7B+...CollectionItemFragment+%7D+%7D+...+on+MessagingComponent+%7B+title+text+actions+%7B+...ActionsFragment+%7D+image+%7B+alt+url+%7D+%7D+...+on+VideoGridComponent+%7B+title+items+%7B+...VideoItemFragment+%7D+%7D+%7D+%7D+...AuthorizationHackFragment+%40include%28if%3A+false%29+%7D+&variables=%7B%22q%22%3A%22Disney%22%7D";
            System.out.println("MoviesAnywhere: Syncing " + url);
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(query);
            writer.flush();
            writer.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode movieList = mapper.readTree(connection.getInputStream());
            movieList = movieList.get("data").get("search").get("components").get(0);

            for (int i = 0; i < movieList.get("paginatedItems").size(); i++) {
                JsonNode movie = movieList.get("paginatedItems").get(i);
                Movie_source movie_source = new Movie_source();
                movie_source.setSource(sourceRepository.findById(idsource).orElse(null));
                movie_source.setTitle(movie.get("title").asText());
                movie_source.setOfficialid(movie.get("id").asText());
                if ((movie.get("primaryAction") != null) && (movie.get("primaryAction").get("target") != null)
                        && (movie.get("primaryAction").get("target").asText().length() > 0))
                    movie_source.setOfficialsite(
                            "https://moviesanywhere.com" + movie.get("primaryAction").get("target").asText());
                if ((movie.get("image") != null) && (movie.get("image").get("url") != null))
                    movie_source.setPoster("https:" + movie.get("image").get("url").asText() + ".jpg?h=600&resize=fit&w=400");

                movieController.newMovie_source(movie_source);
            }
        } catch (Exception e) {
            System.out.println("MoviesAnywhere: failed to sync, try next time");
        }
    }

}
