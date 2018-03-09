package cc.tonyhook.movie.domain;

import java.sql.Blob;

import javax.persistence.Entity;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Coverimg {
    @Id
    private Integer idcoverimg;

    @JsonIgnore
    private Blob image;

    public Integer getIdcoverimg() {
        return idcoverimg;
    }

    public void setIdcoverimg(Integer idcoverimg) {
        this.idcoverimg = idcoverimg;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

}
