package cc.tonyhook.movie;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface Movie_companyRepository extends CrudRepository<Movie_company, Integer> {

    List<Movie_company> findAllByMovieid(Integer movieid);

}
