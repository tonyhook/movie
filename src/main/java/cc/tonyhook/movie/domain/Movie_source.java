package cc.tonyhook.movie.domain;

import java.sql.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Movie_source {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idmovie_source;

    private Integer movieid;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "sourceid")
    @JsonIgnoreProperties("movie_sources")
    private Source source;

    private String title;

    private String officialid;

    private String officialsite;

    private Date releasedate;

    private String poster;

    public Integer getIdmovie_source() {
        return idmovie_source;
    }

    public void setIdmovie_source(Integer idmovie_source) {
        this.idmovie_source = idmovie_source;
    }

    public Integer getMovieid() {
        return movieid;
    }

    public void setMovieid(Integer movieid) {
        this.movieid = movieid;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOfficialid() {
        return officialid;
    }

    public void setOfficialid(String officialid) {
        this.officialid = officialid;
    }

    public String getOfficialsite() {
        return officialsite;
    }

    public void setOfficialsite(String officialsite) {
        this.officialsite = officialsite;
    }

    public Date getReleasedate() {
        return releasedate;
    }

    public void setReleasedate(Date releasedate) {
        this.releasedate = releasedate;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

}
