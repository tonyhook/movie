package cc.tonyhook.movie.controller;

import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.movie.Poster;
import cc.tonyhook.movie.PosterRepository;
import cc.tonyhook.movie.PosterimgRepository;

@RestController
public class PosterController {
    @Autowired
    private PosterRepository posterRepository;
    @Autowired
    private PosterimgRepository posterimgRepository;

    @RequestMapping(value = "/movie/poster/{posterid}/{imagename:.+}", method = RequestMethod.GET)
    public @ResponseBody String getPoster(@PathVariable("posterid") int posterid,
            @PathVariable("imagename") String imagename, HttpServletResponse response) {
        try {
            Poster p = posterRepository.findOne(posterid);

            if ((p == null) || (p.getImagename() == null) || (p.getImagename().length() == 0)) {
                response.setStatus(404);
            } else if (p.getImagename().equals(imagename)) {
                response.setHeader("Content-Disposition", "inline;filename=\"" + p.getImagename() + "\"");
                OutputStream out = response.getOutputStream();
                response.setContentType(p.getImagetype());
                IOUtils.copy(posterimgRepository.findOne(p.getIdposter()).getImage().getBinaryStream(), out);
                out.flush();
                out.close();
            } else {
                response.setStatus(404);
            }
        } catch (Exception e) {
            response.setStatus(500);
        }

        return null;
    }

}
