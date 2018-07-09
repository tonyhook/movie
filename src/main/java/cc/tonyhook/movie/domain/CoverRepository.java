package cc.tonyhook.movie.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface CoverRepository extends CrudRepository<Cover, Integer> {

    List<Cover> findByAlbumidOrderBySequence(Integer albumid);

}
