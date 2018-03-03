package cc.tonyhook.movie.domain;

import java.sql.Blob;

import javax.persistence.Entity;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Posterimg {
    @Id
    private Integer idposterimg;

    @JsonIgnore
    private Blob image;

    public Integer getIdPosterimg() {
        return idposterimg;
    }

    public void setIdPosterimg(Integer idposterimg) {
        this.idposterimg = idposterimg;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

}
