package cc.tonyhook.movie.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Cover {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcover;

    private Integer albumid;

    private Integer sequence;

    public Integer getIdcover() {
        return idcover;
    }

    public void setIdcover(Integer idcover) {
        this.idcover = idcover;
    }

    public Integer getAlbumid() {
        return albumid;
    }

    public void setAlbumid(Integer albumid) {
        this.albumid = albumid;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

}
