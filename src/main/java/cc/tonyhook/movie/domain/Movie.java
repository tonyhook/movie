package cc.tonyhook.movie.domain;

import java.sql.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idmovie;

    private String title;

    private Date releasedate;

    @ManyToOne
    @JoinColumn(name = "imdb")
    private Imdb imdb;

    private Integer video;

    private Integer audio;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "movieid")
    @OrderBy("sourceid ASC")
    private Set<Movie_source> movie_sources;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "movieid")
    private Set<Akatitle> akatitles;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "movieid")
    @OrderBy("companyid ASC")
    private Set<Movie_company> movie_companies;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "movieid")
    private Set<Poster> posters;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "movieid")
    private Set<Album> albums;

    public Integer getIdmovie() {
        return idmovie;
    }

    public void setIdmovie(Integer idmovie) {
        this.idmovie = idmovie;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getReleasedate() {
        return releasedate;
    }

    public void setReleasedate(Date releasedate) {
        this.releasedate = releasedate;
    }

    public Imdb getImdb() {
        return imdb;
    }

    public void setImdb(Imdb imdb) {
        this.imdb = imdb;
    }

    public Integer getVideo() {
        return video;
    }

    public void setVideo(Integer video) {
        this.video = video;
    }

    public Integer getAudio() {
        return audio;
    }

    public void setAudio(Integer audio) {
        this.audio = audio;
    }

    public Set<Movie_source> getMovie_sources() {
        return movie_sources;
    }

    public void setMovie_sources(Set<Movie_source> movie_sources) {
        this.movie_sources = movie_sources;
    }

    public Set<Akatitle> getAkatitles() {
        return akatitles;
    }

    public void setAkatitles(Set<Akatitle> akatitles) {
        this.akatitles = akatitles;
    }

    public Set<Movie_company> getMovie_companies() {
        return movie_companies;
    }

    public void setMovie_companies(Set<Movie_company> movie_companies) {
        this.movie_companies = movie_companies;
    }

    public Set<Poster> getPosters() {
        return posters;
    }

    public void setPosters(Set<Poster> posters) {
        this.posters = posters;
    }

    public Set<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(Set<Album> albums) {
        this.albums = albums;
    }

}
