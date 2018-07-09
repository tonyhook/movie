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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idalbum;

    private Integer movieid;

    private String title;

    private Date releasedate;

    private Date listdate;

    private String genre;

    private String label;

    private String cat;

    private Integer disc;

    private String region;

    private Integer digital;

    private String barcode;

    private String discogsID;

    private String ASIN;

    private String status;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "albumid")
    @OrderBy("sequence ASC")
    private Set<Cover> covers;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "albumid")
    @OrderBy("disc ASC, track ASC")
    private Set<Track> tracks;

    public Integer getIdalbum() {
        return idalbum;
    }

    public void setIdalbum(Integer idalbum) {
        this.idalbum = idalbum;
    }

    public Integer getMovieid() {
        return movieid;
    }

    public void setMovieid(Integer movieid) {
        this.movieid = movieid;
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

    public Date getListdate() {
        return listdate;
    }

    public void setListdate(Date listdate) {
        this.listdate = listdate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public Set<Cover> getCovers() {
        return covers;
    }

    public void setCovers(Set<Cover> covers) {
        this.covers = covers;
    }

    public Set<Track> getTracks() {
        return tracks;
    }

    public void setTracks(Set<Track> tracks) {
        this.tracks = tracks;
    }

    public Integer getDisc() {
        return disc;
    }

    public void setDisc(Integer disc) {
        this.disc = disc;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getDigital() {
        return digital;
    }

    public void setDigital(Integer digital) {
        this.digital = digital;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDiscogsID() {
        return discogsID;
    }

    public void setDiscogsID(String discogsID) {
        this.discogsID = discogsID;
    }

    public String getASIN() {
        return ASIN;
    }

    public void setASIN(String aSIN) {
        ASIN = aSIN;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
