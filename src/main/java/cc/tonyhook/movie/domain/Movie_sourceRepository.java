package cc.tonyhook.movie.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface Movie_sourceRepository extends CrudRepository<Movie_source, Integer> {

    List<Movie_source> findAllByOfficialidAndSource(String officialid, Source source);
    List<Movie_source> findAllByMovieidIsNull();
    List<Movie_source> findAllBySource(Source source);

}
