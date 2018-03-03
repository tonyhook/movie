package cc.tonyhook.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.movie.domain.Source;
import cc.tonyhook.movie.domain.SourceRepository;

@RestController
public class SourceController {
    @Autowired
    private SourceRepository sourceRepository;

    @RequestMapping(value = "/movie/source/list", method = RequestMethod.GET)
    public @ResponseBody Iterable<Source> getCompanies() {
        Iterable<Source> sources = sourceRepository.findAllByOrderByNameAsc();

        for (Source source : sources) {
            source.setMovie_sources(null);
        }

        return sources;
    }

}
