package cc.tonyhook.movie.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface TrackRepository extends CrudRepository<Track, Integer> {

    List<Track> findByAlbumidOrderByDiscAscTrackAsc(Integer albumid);

}
