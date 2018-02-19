package cc.tonyhook.movie;

import java.sql.Date;
import java.util.Set;

public class MovieGeneralInfo {
    private Integer idmovie;

    private String title_en;

    private String title_cn;

    private Date releasedate;

    private String poster;

    private String imdb;

    private Set<Integer> company;

    private Integer defaultcompany;

    private Integer video;

    private Integer audio;

    public Integer getIdmovie() {
        return idmovie;
    }

    public void setIdmovie(Integer idmovie) {
        this.idmovie = idmovie;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getTitle_cn() {
        return title_cn;
    }

    public void setTitle_cn(String title_cn) {
        this.title_cn = title_cn;
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

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public Set<Integer> getCompany() {
        return company;
    }

    public void setCompany(Set<Integer> company) {
        this.company = company;
    }

    public Integer getDefaultcompany() {
        return defaultcompany;
    }

    public void setDefaultcompany(Integer defaultcompany) {
        this.defaultcompany = defaultcompany;
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
}
