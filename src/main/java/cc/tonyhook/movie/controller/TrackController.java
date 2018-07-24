package cc.tonyhook.movie.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bunjlabs.jecue.CueLoader;
import com.bunjlabs.jecue.entities.CueFileInfo;
import com.bunjlabs.jecue.entities.CueSheet;
import com.bunjlabs.jecue.entities.CueTrackInfo;

import cc.tonyhook.movie.domain.Album;
import cc.tonyhook.movie.domain.AlbumRepository;
import cc.tonyhook.movie.domain.Movie;
import cc.tonyhook.movie.domain.MovieRepository;
import cc.tonyhook.movie.domain.Movie_company;
import cc.tonyhook.movie.domain.Track;
import cc.tonyhook.movie.domain.TrackRepository;

@RestController
public class TrackController {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private TrackRepository trackRepository;
    @Autowired
    private MovieRepository movieRepository;

    private static String NAS_ADDRESS = "tonyhook.3322.org";
    private static int NAS_PORT = 4443;
    private static String NAS_SHARE = "Movie";

    private String getDefaultCompanyName(Movie movie) {
        if (movie.getMovie_companies() != null) {
            Set<Movie_company> movie_companies = movie.getMovie_companies();
            for (Movie_company movie_company : movie_companies) {
                if (movie_company.getPreferred() == 1) {
                    return movie_company.getCompany().getName();
                }
            }
        }
        return "";
    }

    @RequestMapping(value = "/music/track/album/{albumid}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Iterable<Track>> listTrackByAlbum(@PathVariable("albumid") Integer albumid) {
        Album album = albumRepository.findById(albumid).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Iterable<Track> tracks = trackRepository.findByAlbumidOrderByDiscAscTrackAsc(albumid);

        return new ResponseEntity<Iterable<Track>>(tracks, HttpStatus.OK);
    }

    @RequestMapping(value = "/music/track/album/{albumid}/nas", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Iterable<Track>> getNASTrack(@PathVariable("albumid") Integer albumid) {
        Album album = albumRepository.findById(albumid).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (album.getMovieid() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Integer movieid = album.getMovieid();
        Movie movie = movieRepository.findById(movieid).orElse(null);
        String companyFolder = getDefaultCompanyName(movie);
        String movieFolder = movie.getReleasedate().toString().replace("-", ".") + "." + movie.getTitle();
        String albumFolder = album.getTitle();
        if ((album.getLabel() != null) && (album.getLabel().length() > 0)) {
            albumFolder = albumFolder + " (" + album.getLabel();
            if ((album.getCat() != null) && (album.getCat().length() > 0))
                albumFolder = albumFolder + " " + album.getCat();
            albumFolder = albumFolder + ")";
        }

        ArrayList<Track> tracks = new ArrayList<Track>();

        try {
            for (int disc = 1; disc <= album.getDisc(); disc++) {
                String discName = album.getDisc() == 1 ? "" : " (CD " + disc + ")";

                String cueUrl = "https://" + NAS_ADDRESS + ":" + NAS_PORT
                        + "/" + NAS_SHARE + "/CD/"
                        + companyFolder + "/Soundtrack/"
                        + movieFolder + "/"
                        + albumFolder + "/"
                        + album.getTitle() + discName + ".cue";
                Response resp = Jsoup.connect(cueUrl).ignoreContentType(true).maxBodySize(0).execute();

                if (resp.statusCode() == 200) {
                    CueLoader cueLoader = new CueLoader(resp.bodyStream());
                    CueSheet cueSheet = cueLoader.load();

                    for (int fileno = 0; fileno < cueSheet.getFiles().size(); fileno++) {
                        CueFileInfo cfi = cueSheet.getFiles().get(fileno);

                        for (int trackno = 0; trackno < cfi.getTracks().size(); trackno++) {
                            CueTrackInfo cti = cfi.getTracks().get(trackno);

                            Track track = new Track();

                            track.setAlbumid(albumid);
                            track.setDisc(disc);
                            track.setTrack(cti.getNumber());
                            track.setName(cti.getTitle());

                            if (cueSheet.getFiles().size() == 1)
                                track.setSingle(true);
                            else
                                track.setSingle(false);

                            if (cfi.getFileName().endsWith("wav")
                                || cfi.getFileName().endsWith("ape")
                                || cfi.getFileName().endsWith("flac"))
                                track.setLossless(true);
                            else
                                track.setLossless(false);
 
                            track.setFormat(cfi.getFileName().split("\\.")[cfi.getFileName().split("\\.").length - 1]);

                            tracks.add(track);
                        }

                    }
                }

                String logUrl = "https://" + NAS_ADDRESS + ":" + NAS_PORT
                        + "/" + NAS_SHARE + "/CD/"
                        + companyFolder + "/Soundtrack/"
                        + movieFolder + "/"
                        + albumFolder + "/"
                        + album.getTitle() + discName + ".log";
                resp = Jsoup.connect(logUrl).ignoreContentType(true).maxBodySize(0).execute();

                if (resp.statusCode() == 200) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(resp.bodyStream(), StandardCharsets.UTF_16));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            line = line.trim().replace(" ", "");
                            if (line.split("\\|").length == 5) {
                                try {
                                    String[] trackinfo = line.split("\\|");
                                    Integer trackno = Integer.parseInt(trackinfo[0]);
                                    for (Track track : tracks) {
                                        if ((track.getDisc().equals(disc)) && (track.getTrack().equals(trackno))) {
                                            track.setStart(Integer.parseInt(trackinfo[3]));
                                            track.setEnd(Integer.parseInt(trackinfo[4]));
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                            if (line.split("\\|").length == 2) {
                                try {
                                    String[] trackinfo = line.split("\\|");
                                    Integer trackno = Integer.parseInt(trackinfo[0]);
                                    for (Track track : tracks) {
                                        if ((track.getDisc().equals(disc)) && (track.getTrack().equals(trackno))) {
                                            if (trackinfo[1].indexOf("Accuratelyripped") >= 0)
                                                track.setAccurate(true);
                                            else
                                                track.setAccurate(false);
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            }
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getStatusCode()));
        } catch (Throwable e) {
            System.out.println("NAS: failed to get " + albumid + "'s NAS CUE");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Iterable<Track>>(tracks, HttpStatus.OK);     
    }

    @RequestMapping(value = "/music/track/{idtrack}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Track> getTrack(@PathVariable("idtrack") Integer idtrack) {
        Track track = trackRepository.findById(idtrack).orElse(null);
        if (track == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Track>(track, HttpStatus.OK);
    }

    @RequestMapping(value = "/music/track/{idtrack}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8", produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Set<Track>> setTrack(@PathVariable("idtrack") Integer idtrack,
            @RequestBody Track input) {
        if (!idtrack.equals(input.getIdtrack())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Album album = albumRepository.findById(input.getAlbumid()).orElse(null);
        if (album == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Track track = null;

        for (Track existedTrack : album.getTracks()) {
            if (existedTrack.getIdtrack().equals(idtrack))
                track = existedTrack;
        }

        if (track == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        track.setDisc(input.getDisc());
        track.setTrack(input.getTrack());
        track.setName(input.getName());
        track.setStart(input.getStart());
        track.setEnd(input.getEnd());
        track.setSingle(input.getSingle());
        track.setFormat(input.getFormat());
        track.setLossless(input.getLossless());
        track.setAccurate(input.getAccurate());

        trackRepository.save(track);
        albumRepository.save(album);

        return new ResponseEntity<Set<Track>>(album.getTracks(), HttpStatus.OK);
    }

    @RequestMapping(value = "/music/track", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8", produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Set<Track>> addTrack(@RequestBody Track input) {
        Album album = albumRepository.findById(input.getAlbumid()).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Track track = trackRepository.save(input);

        album.getTracks().add(track);
        albumRepository.save(album);

        return new ResponseEntity<Set<Track>>(album.getTracks(), HttpStatus.OK);
    }

    @RequestMapping(value = "/music/track/{idtrack}", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Set<Track>> removeTrack(@PathVariable("idtrack") Integer idtrack) {
        Track track = trackRepository.findById(idtrack).orElse(null);
        if (track == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Integer albumid = track.getAlbumid();
        trackRepository.delete(track);

        Set<Track> tracks = albumRepository.findById(albumid).orElse(null).getTracks();
        return new ResponseEntity<Set<Track>>(tracks, HttpStatus.OK);
    }

}
