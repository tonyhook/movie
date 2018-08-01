package cc.tonyhook.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.movie.domain.Album;
import cc.tonyhook.movie.domain.AlbumRepository;
import cc.tonyhook.movie.domain.Cover;
import cc.tonyhook.movie.domain.CoverRepository;
import cc.tonyhook.movie.domain.Coverimg;
import cc.tonyhook.movie.domain.CoverimgRepository;
import cc.tonyhook.movie.domain.Track;
import cc.tonyhook.movie.domain.TrackRepository;

@RestController
public class AlbumController {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private CoverRepository coverRepository;
    @Autowired
    private CoverimgRepository coverimgRepository;
    @Autowired
    private TrackRepository trackRepository;

    @RequestMapping(value = "/music/album/list", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Iterable<Album>> getAlbums() {
        Iterable<Album> albums = albumRepository.findByOrderByListdateAscIdalbumAsc();
        
        for (Album album : albums) {
            album.setTracks(null);
        }

        return new ResponseEntity<Iterable<Album>>(albums, HttpStatus.OK);
    }

    @RequestMapping(value = "/music/album/{idalbum}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Album> getAlbum(@PathVariable("idalbum") Integer idalbum) {
        Album album = albumRepository.findById(idalbum).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Album>(album, HttpStatus.OK);
    }

    @RequestMapping(value = "/music/album/{idalbum}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Album> setAlbum(@PathVariable("idalbum") Integer idalbum,
            @RequestBody Album input) {
        if (!idalbum.equals(input.getIdalbum())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Album album = albumRepository.findById(idalbum).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        album = input;

        albumRepository.save(album);

        return new ResponseEntity<Album>(album, HttpStatus.OK);
    }

    @RequestMapping(value = "/music/album", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Album> addAlbum(@RequestBody Album input) {
        Album album = albumRepository.save(input);

        return new ResponseEntity<Album>(album, HttpStatus.OK);
    }

    @RequestMapping(value = "/music/album/{idalbum}", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<?> removeAlbum(@PathVariable("idalbum") Integer idalbum) {
        Iterable<Cover> covers = coverRepository.findByAlbumidOrderBySequence(idalbum);
        Iterable<Track> tracks = trackRepository.findByAlbumidOrderByDiscAscTrackAsc(idalbum);

        for (Cover cover : covers) {
            Coverimg coverimg = coverimgRepository.findById(cover.getIdcover()).orElse(null);
            if (coverimg != null) {
                coverimgRepository.delete(coverimg);
            }

            coverRepository.delete(cover);
        }

        for (Track track : tracks) {
            trackRepository.delete(track);
        }

        Album album = albumRepository.findById(idalbum).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        albumRepository.delete(album);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
