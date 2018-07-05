package cc.tonyhook.movie.controller;

import java.util.List;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.movie.domain.Album;
import cc.tonyhook.movie.domain.AlbumRepository;
import cc.tonyhook.movie.domain.CoverimgRepository;

@RestController
public class CoverController {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private CoverimgRepository coverimgRepository;

    @RequestMapping(value = "/movie/cover/{movieid}/{title:.+}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getCover(@PathVariable("movieid") Integer movieid,
            @PathVariable("title") String title) {
        try {
            List<Album> a = albumRepository.findAllByMovieidAndTitle(movieid, title);

            if ((a.size() == 0) || (coverimgRepository.findById(a.get(0).getIdalbum()).orElse(null) == null)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                byte[] media = IOUtils.toByteArray(coverimgRepository.findById(a.get(0).getIdalbum()).orElse(null).getImage().getBinaryStream());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE));
                headers.setContentDisposition(ContentDisposition.builder("inline").filename(title + ".jpg").build());

                return new ResponseEntity<>(media, headers, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
