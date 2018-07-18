package cc.tonyhook.movie.controller;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

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
import org.springframework.web.bind.annotation.RequestParam;
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
            @PathVariable("imagename") String imagename,
            @RequestParam(name = "height", defaultValue = "0") int height,
            @RequestParam(name = "width", defaultValue = "0") int width) {
        try {
            Poster p = posterRepository.findById(posterid).orElse(null);

            if ((p == null) || (p.getImagename() == null) || (p.getImagename().length() == 0)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (p.getImagename().equals(imagename)) {
                byte[] media = IOUtils.toByteArray(posterimgRepository.findById(p.getIdposter()).orElse(null).getImage().getBinaryStream());

                if ((height > 0) || (width > 0)) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(media));
                    int oheight = img.getHeight();
                    int owidth = img.getWidth();
                    float rheight = (float) (height * 1.0) / oheight;
                    float rwidth = (float) (width * 1.0) / owidth;
                    float r = (float) 1.0;
                    if ((height != 0) && (width != 0))
                        r = rheight < rwidth ? rheight : rwidth;
                    if ((height == 0) && (width != 0))
                        r = rwidth;
                    if ((height != 0) && (width == 0))
                        r = rheight;
                    int theight = Math.round(oheight * r);
                    int twidth = Math.round(owidth * r);

                    BufferedImage timg = new BufferedImage(twidth, theight, img.getType());
                    Graphics2D g2d = timg.createGraphics();

                    g2d.setComposite(AlphaComposite.Src);

                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.drawImage(img, 0, 0, twidth, theight, null);
                    g2d.dispose();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(timg, "jpg", baos);
                    baos.flush();
                    media = baos.toByteArray();
                }

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
