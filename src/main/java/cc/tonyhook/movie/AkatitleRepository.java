package cc.tonyhook.movie;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface AkatitleRepository extends CrudRepository<Akatitle, Integer> {

    List<Akatitle> findAllByMovieidAndLanguage(Integer movieid, String language);

}
