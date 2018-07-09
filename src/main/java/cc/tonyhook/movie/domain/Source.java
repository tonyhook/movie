package cc.tonyhook.movie.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idsource;

    private String name;

    private String url;

    private Integer automatic;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "sourceid")
    private Set<Movie_source> movie_sources;

    public Integer getIdsource() {
        return idsource;
    }

    public void setIdsource(Integer idsource) {
        this.idsource = idsource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getAutomatic() {
        return automatic;
    }

    public void setAutomatic(Integer automatic) {
        this.automatic = automatic;
    }

    public Set<Movie_source> getMovie_sources() {
        return movie_sources;
    }

    public void setMovie_sources(Set<Movie_source> movie_sources) {
        this.movie_sources = movie_sources;
    }

}
