package cc.tonyhook.movie.controller;

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

import cc.tonyhook.movie.domain.Poster;
import cc.tonyhook.movie.domain.PosterRepository;
import cc.tonyhook.movie.domain.PosterimgRepository;

@RestController
public class PosterController {
    @Autowired
    private PosterRepository posterRepository;
    @Autowired
    private PosterimgRepository posterimgRepository;

    @RequestMapping(value = "/movie/poster/{posterid}/{imagename:.+}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getPoster(@PathVariable("posterid") Integer posterid,
            @PathVariable("imagename") String imagename) {
        try {
            Poster p = posterRepository.findById(posterid).orElse(null);

            if ((p == null) || (p.getImagename() == null) || (p.getImagename().length() == 0)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (p.getImagename().equals(imagename)) {
                byte[] media = IOUtils.toByteArray(posterimgRepository.findById(p.getIdposter()).orElse(null).getImage().getBinaryStream());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(p.getImagetype()));
                headers.setContentDisposition(ContentDisposition.builder("inline").filename(p.getImagename()).build());

                return new ResponseEntity<>(media, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
