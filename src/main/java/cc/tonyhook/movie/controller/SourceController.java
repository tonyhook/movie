package cc.tonyhook.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(value = "/movie/source/list", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Iterable<Source>> listSources() {
        Iterable<Source> sources = sourceRepository.findAllByOrderByNameAsc();

        for (Source source : sources) {
            source.setMovie_sources(null);
        }

        return new ResponseEntity<Iterable<Source>>(sources, HttpStatus.OK);
    }

}
