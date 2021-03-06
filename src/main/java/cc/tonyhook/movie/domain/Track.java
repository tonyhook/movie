package cc.tonyhook.movie.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idtrack;

    private Integer albumid;

    private Integer disc;

    private Integer track;

    private Integer start;

    private Integer end;

    private String name;

    private Boolean single;

    private String format;

    private Boolean lossless;

    private Boolean accurate;

    public Integer getIdtrack() {
        return idtrack;
    }

    public void setIdtrack(Integer idtrack) {
        this.idtrack = idtrack;
    }

    public Integer getAlbumid() {
        return albumid;
    }

    public void setAlbumid(Integer albumid) {
        this.albumid = albumid;
    }

    public Integer getDisc() {
        return disc;
    }

    public void setDisc(Integer disc) {
        this.disc = disc;
    }

    public Integer getTrack() {
        return track;
    }

    public void setTrack(Integer track) {
        this.track = track;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSingle() {
        return single;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Boolean getLossless() {
        return lossless;
    }

    public void setLossless(Boolean lossless) {
        this.lossless = lossless;
    }

    public Boolean getAccurate() {
        return accurate;
    }

    public void setAccurate(Boolean accurate) {
        this.accurate = accurate;
    }

}
