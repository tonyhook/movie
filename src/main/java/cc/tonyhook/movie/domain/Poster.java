package cc.tonyhook.movie.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Poster {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idposter;

    private String imagetype;

    private String imagename;

    private String imagehash;

    private String imageurl;

    private Integer movieid;

    public Integer getIdposter() {
        return idposter;
    }

    public void setIdposter(Integer idposter) {
        this.idposter = idposter;
    }

    public String getImagetype() {
        return imagetype;
    }

    public void setImagetype(String imagetype) {
        this.imagetype = imagetype;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public String getImagehash() {
        return imagehash;
    }

    public void setImagehash(String imagehash) {
        this.imagehash = imagehash;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public Integer getMovieid() {
        return movieid;
    }

    public void setMovieid(Integer movieid) {
        this.movieid = movieid;
    }

}
