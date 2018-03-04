package cc.tonyhook.movie.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface AlbumRepository extends CrudRepository<Album, Integer> {

    List<Album> findAllByMovieidAndTitle(Integer movieid, String title);

}
