package cc.tonyhook.movie;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PosterRepository extends CrudRepository<Poster, Integer> {

    List<Poster> findByImagename(String imagename);
    List<Poster> findByImageurl(String imageurl);

}
