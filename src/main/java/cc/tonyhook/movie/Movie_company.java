package cc.tonyhook.movie;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Movie_company {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idmovie_company;

    private Integer movieid;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "companyid")
    @JsonIgnoreProperties("movie_companies")
    private Company company;

    private Integer preferred;

    public Integer getMovieid() {
        return movieid;
    }

    public void setMovieid(Integer movieid) {
        this.movieid = movieid;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Integer getPreferred() {
        return preferred;
    }

    public void setPreferred(Integer preferred) {
        this.preferred = preferred;
    }

}
