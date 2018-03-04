package cc.tonyhook.movie.controller;

import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.movie.domain.Album;
import cc.tonyhook.movie.domain.AlbumRepository;

@RestController
public class CoverController {
    @Autowired
    private AlbumRepository albumRepository;

    @RequestMapping(value = "/movie/cover/{movieid}/{title}", method = RequestMethod.GET)
    public @ResponseBody String getCover(@PathVariable("movieid") int movieid,
            @PathVariable("title") String title, HttpServletResponse response) {
        try {
            List<Album> a = albumRepository.findAllByMovieidAndTitle(movieid, title);

            if ((a == null) || (a.get(0).getCover() == null)) {
                response.setStatus(404);
            } else {
                response.setHeader("Content-Disposition", "inline;filename=\"" + title + ".jpg\"");
                OutputStream out = response.getOutputStream();
                response.setContentType("image/jpeg");
                IOUtils.copy(a.get(0).getCover().getBinaryStream(), out);
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            response.setStatus(500);
        }

        return null;
    }

}
