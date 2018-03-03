package cc.tonyhook.movie.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface MovieRepository extends CrudRepository<Movie, Integer> {

    List<Movie> findAllByOrderByReleasedateAsc();
    List<Movie> findAllByImdb(Imdb imdb);

}
