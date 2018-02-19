package cc.tonyhook.movie;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface SourceRepository extends CrudRepository<Source, Integer> {

    List<Source> findAllByOrderByNameAsc();

}
