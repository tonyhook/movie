package cc.tonyhook.movie.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cc.tonyhook.movie.domain.Akatitle;
import cc.tonyhook.movie.domain.AkatitleRepository;
import cc.tonyhook.movie.domain.Company;
import cc.tonyhook.movie.domain.CompanyRepository;
import cc.tonyhook.movie.domain.Imdb;
import cc.tonyhook.movie.domain.ImdbRepository;
import cc.tonyhook.movie.domain.Movie;
import cc.tonyhook.movie.domain.MovieGeneralInfo;
import cc.tonyhook.movie.domain.MovieRepository;
import cc.tonyhook.movie.domain.Movie_company;
import cc.tonyhook.movie.domain.Movie_companyRepository;
import cc.tonyhook.movie.domain.Movie_source;
import cc.tonyhook.movie.domain.Movie_sourceRepository;
import cc.tonyhook.movie.domain.Poster;
import cc.tonyhook.movie.domain.PosterRepository;
import cc.tonyhook.movie.domain.SourceRepository;
import cc.tonyhook.movie.updater.UpdaterImdb;
import cc.tonyhook.movie.updater.UpdaterPoster;

@RestController
public class MovieController {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ImdbRepository imdbRepository;
    @Autowired
    private AkatitleRepository akatitleRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private Movie_sourceRepository movie_sourceRepository;
    @Autowired
    private Movie_companyRepository movie_companyRepository;
    @Autowired
    private PosterRepository posterRepository;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private UpdaterPoster updaterPoster;
    @Autowired
    private UpdaterImdb updaterImdb;

    @RequestMapping(value = "/movie/movie/company/{name}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Iterable<MovieGeneralInfo>> getMovieByCompany(@PathVariable("name") String name) {
        ArrayList<MovieGeneralInfo> movieGeneralInfos = new ArrayList<MovieGeneralInfo>();

        Iterable<Movie> movies = movieRepository.findAllByOrderByReleasedateAsc();
        for (Movie movie : movies) {
            boolean companyIsMatched = false;

            if (!name.equals("all")) {
                Set<Movie_company> movie_companies = movie.getMovie_companies();
                for (Movie_company movie_company : movie_companies) {
                    if (movie_company.getCompany().getName().equals(name)) {
                        companyIsMatched = true;
                    }
                }
            }
            if ((!companyIsMatched) && (!name.equals("all")))
                continue;

            MovieGeneralInfo movieGeneralInfo = new MovieGeneralInfo();

            movieGeneralInfo.setIdmovie(movie.getIdmovie());
            movieGeneralInfo.setTitle_en(movie.getTitle());
            movieGeneralInfo.setReleasedate(movie.getReleasedate());
            movieGeneralInfo.setAudio(movie.getAudio());
            movieGeneralInfo.setVideo(movie.getVideo());
            if (movie.getImdb() != null) {
                movieGeneralInfo.setImdb(movie.getImdb().getImdb());
            }

            Set<Integer> companies = new HashSet<Integer>();
            Set<Movie_company> movie_companies = movie.getMovie_companies();
            for (Movie_company movie_company : movie_companies) {
                companies.add(movie_company.getCompany().getIdcompany());
                if (movie_company.getPreferred() == 1)
                    movieGeneralInfo.setDefaultcompany(movie_company.getCompany().getIdcompany());
            }
            movieGeneralInfo.setCompany(companies);

            Set<Akatitle> akatitles = movie.getAkatitles();
            for (Akatitle akatitle : akatitles) {
                if (akatitle.getLanguage().equals("Chinese")) {
                    movieGeneralInfo.setTitle_cn(akatitle.getTitle());
                }
            }

            Set<Poster> posters = movie.getPosters();
            if (posters.size() > 0) {
                movieGeneralInfo.setPoster(((Poster) (posters.toArray()[0])).getIdposter() + "/"
                        + ((Poster) (posters.toArray()[0])).getImagename());
            }

            movieGeneralInfos.add(movieGeneralInfo);
        }

        return new ResponseEntity<Iterable<MovieGeneralInfo>>(movieGeneralInfos, HttpStatus.OK);
    }

    @RequestMapping(value = "/movie/movie/{idmovie}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Movie> getMovieById(@PathVariable("idmovie") Integer idmovie) {
        Movie movie = movieRepository.findById(idmovie).orElse(null);
        if (movie == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (movie.getImdb() != null) {
            String posterUrl = movie.getImdb().getPoster();
            List<Poster> posters = posterRepository.findByImageurl(posterUrl);
            if (posters.size() > 0) {
                movie.getImdb()
                        .setPoster(String.valueOf(posters.get(0).getIdposter()) + "/" + posters.get(0).getImagename());
            }
        }

        if (movie.getMovie_sources() != null) {
            for (Movie_source movie_source : movie.getMovie_sources()) {
                String posterUrl = movie_source.getPoster();
                List<Poster> posters = posterRepository.findByImageurl(posterUrl);
                if (posters.size() > 0) {
                    movie_source.setPoster(
                            String.valueOf(posters.get(0).getIdposter()) + "/" + posters.get(0).getImagename());
                }
            }
        }

        return new ResponseEntity<Movie>(movie, HttpStatus.OK);
    }

    @RequestMapping(value = "/movie/movie/{idmovie}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Movie> setMovieById(@PathVariable("idmovie") Integer idmovie,
            @RequestBody MovieGeneralInfo input) {
        if (!idmovie.equals(input.getIdmovie())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Movie movie = movieRepository.findById(input.getIdmovie()).orElse(null);
        if (movie == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        movie.setTitle(input.getTitle_en());
        movie.setReleasedate(input.getReleasedate());

        Imdb imdb = imdbRepository.findById(input.getImdb()).orElse(null);
        if (imdb != null) {
            movie.setImdb(imdb);
        }
        if ((imdb == null) && (input.getImdb().length() > 0)) {
            imdb = new Imdb();
            imdb.setImdb(input.getImdb());
            imdb = imdbRepository.save(imdb);
            movie.setImdb(imdb);
        }

        movieRepository.save(movie);
        if (input.getImdb().length() > 0)
            updaterImdb.updateImdb(imdb);

        List<Akatitle> akatitles = akatitleRepository.findAllByMovieidAndLanguage(input.getIdmovie(), "Chinese");
        Akatitle akatitle = null;
        if (input.getTitle_cn().length() > 0) {
            if (akatitles.size() == 0) {
                akatitle = new Akatitle();
                akatitle.setMovieid(input.getIdmovie());
                akatitle.setLanguage("Chinese");
                akatitle.setTitle(input.getTitle_cn());
                akatitleRepository.save(akatitle);
            } else {
                akatitle = akatitles.get(0);
                akatitle.setTitle(input.getTitle_cn());
                akatitleRepository.save(akatitle);
            }
        } else {
            if (akatitles.size() > 0) {
                akatitle = akatitles.get(0);
                akatitleRepository.delete(akatitle);
            }
        }

        Set<Integer> companies = input.getCompany();

        List<Movie_company> movie_companies = movie_companyRepository.findAllByMovieid(input.getIdmovie());
        for (Movie_company movie_company : movie_companies) {
            movie_companyRepository.delete(movie_company);
        }

        for (Integer companyid : companies) {
            Company company = companyRepository.findById(companyid).orElse(null);
            Movie_company movie_company = new Movie_company();
            movie_company.setMovieid(input.getIdmovie());
            movie_company.setCompany(company);
            if (companyid == input.getDefaultcompany()) {
                movie_company.setPreferred(1);
            } else {
                movie_company.setPreferred(0);
            }
            movie_companyRepository.save(movie_company);
        }

        return new ResponseEntity<Movie>(movie, HttpStatus.OK);
    }

    private boolean shouldIgnore(String word) {
        String[] ignorewords = new String[] { "The", "A", "Of", "I", "2", "2:", "II", "II:", "In", "And", "&", "For",
                "To", "He" };

        for (int i = 0; i < ignorewords.length; i++) {
            if (word.toUpperCase().equals(ignorewords[i].toUpperCase()))
                return true;
        }

        return false;
    }

    private boolean matches(String title, ArrayList<String> keywords) {
        String[] titleWords = title.split(" ");

        for (int i = 0; i < titleWords.length; i++) {
            for (String keyword : keywords) {
                if (titleWords[i].toUpperCase().equals(keyword.toUpperCase()))
                    return true;
            }
        }

        return false;
    }

    @RequestMapping(value = "/movie/movie/find/{keywords:.+}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Iterable<Movie>> findMovie(@PathVariable("keywords") String keywords) {
        Iterable<Movie> movies = movieRepository.findAll();

        String[] words = keywords.replace("/", " ").replace("-", " ").replace("·", " ").split(" ");
        ArrayList<String> validwords = new ArrayList<String>();
        for (int i = 0; i < words.length; i++) {
            if (!shouldIgnore(words[i])) {
                validwords.add(words[i]);
            }
        }

        ArrayList<Movie> possibleMovies = new ArrayList<Movie>();
        for (Movie movie : movies) {
            if (matches(movie.getTitle().replace("/", " ").replace("-", " ").replace("·", " "), validwords))
                possibleMovies.add(movie);
        }

        return new ResponseEntity<Iterable<Movie>>(possibleMovies, HttpStatus.OK);
    }

    @RequestMapping(value = "/movie/movie/orphan", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Iterable<Movie_source>> getOrphanMovie() {
        Iterable<Movie_source> movie_sources = movie_sourceRepository.findAllByMovieidIsNull();

        return new ResponseEntity<Iterable<Movie_source>>(movie_sources, HttpStatus.OK);
    }

    @RequestMapping(value = "/movie/movie/merge/{idmovie_source}/{idmovie}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<?> mergeMovie(@PathVariable("idmovie_source") Integer idmovie_source,
            @PathVariable("idmovie") Integer idmovie) {
        Movie_source movie_source = movie_sourceRepository.findById(idmovie_source).orElse(null);
        if (movie_source == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (movie_source.getMovieid() != null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        movie_source.setMovieid(idmovie);
        movie_sourceRepository.save(movie_source);

        if ((movie_source.getPoster() != null) && (movie_source.getPoster().length() != 0)) {
            if (posterRepository.findByImageurl(movie_source.getPoster()).size() == 0) {
                Poster poster = new Poster();
                poster.setImageurl(movie_source.getPoster());
                poster.setMovieid(idmovie);
                poster = posterRepository.save(poster);
                updaterPoster.updatePoster(poster);
            } else {
                System.out.println("Merge Movie: " + movie_source.getPoster() + "exists, to be careful");
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/movie/movie/new/{idmovie_source}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody ResponseEntity<Movie> newMovie(@PathVariable("idmovie_source") Integer idmovie_source) {
        Movie_source movie_source = movie_sourceRepository.findById(idmovie_source).orElse(null);
        if (movie_source == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (movie_source.getMovieid() != null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Movie movie = new Movie();
        movie.setTitle(movie_source.getTitle());
        movie = movieRepository.save(movie);

        movie_source.setMovieid(movie.getIdmovie());
        movie_sourceRepository.save(movie_source);

        if ((movie_source.getPoster() != null) && (movie_source.getPoster().length() != 0)) {
            if (posterRepository.findByImageurl(movie_source.getPoster()).size() == 0) {
                Poster poster = new Poster();
                poster.setImageurl(movie_source.getPoster());
                poster.setMovieid(movie.getIdmovie());
                poster = posterRepository.save(poster);
                updaterPoster.updatePoster(poster);
            } else {
                System.out.println("New Movie: " + movie_source.getPoster() + "exists, to be careful");
            }
        }

        return new ResponseEntity<Movie>(movie, HttpStatus.OK);
    }

    private String getQualifiedMovieName(String title) {
        return title.replace(":", "").replace("!", "").replace(",", "").replace("'", "").replace(" ", ".").replace("-",
                ".");
    }

    public void newMovie_source(Movie_source newMovie_source) {
        List<Movie_source> movie_sources = movie_sourceRepository
                .findAllByOfficialidAndSource(newMovie_source.getOfficialid(), newMovie_source.getSource());
        if (movie_sources.size() > 0) {
            Movie_source existedMovie_source = movie_sources.get(0);
            existedMovie_source.setOfficialsite(newMovie_source.getOfficialsite());
            existedMovie_source.setPoster(newMovie_source.getPoster());
            existedMovie_source.setTitle(newMovie_source.getTitle());
            if (newMovie_source.getReleasedate() != null)
                existedMovie_source.setReleasedate(newMovie_source.getReleasedate());
            movie_sourceRepository.save(existedMovie_source);

            if ((existedMovie_source.getMovieid() != null) && (existedMovie_source.getPoster() != null)
                    && (existedMovie_source.getPoster().length() != 0)) {
                if (posterRepository.findByImageurl(existedMovie_source.getPoster()).size() == 0) {
                    Poster poster = new Poster();
                    poster.setImageurl(existedMovie_source.getPoster());
                    poster.setMovieid(existedMovie_source.getMovieid());
                    poster = posterRepository.save(poster);
                    updaterPoster.updatePoster(poster);
                }
            }
        } else {
            movie_sourceRepository.save(newMovie_source);
        }
    }

    @RequestMapping(value = "/movie/movie/manualupdate/{idsource}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> downloadMovieList(@PathVariable("idsource") Integer idsource) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(out),
                    CSVFormat.DEFAULT.withHeader("title", "officialid", "officialsite", "poster", "releasedate"));
            DateFormat dt = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);

            List<Movie_source> movie_sources = movie_sourceRepository
                    .findAllBySource(sourceRepository.findById(idsource).orElse(null));
            for (Movie_source movie_source : movie_sources) {
                String releasedate = "";
                if (movie_source.getReleasedate() != null)
                    releasedate = dt.format(movie_source.getReleasedate());
                csvPrinter.printRecord(movie_source.getTitle(), movie_source.getOfficialid(),
                        movie_source.getOfficialsite(), movie_source.getPoster(), releasedate);
            }
            csvPrinter.flush();
            csvPrinter.close();

            out.flush();
            out.close();

            byte[] media = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(String.valueOf(idsource) + ".csv").build());

            return new ResponseEntity<>(media, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/movie/movie/manualupdate", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<?> uploadMovieList(@RequestParam("idsource") Integer idsource,
            @RequestParam("action") String action, @RequestParam("file") MultipartFile file) {
        if (action.equals("upload")) {
            try {
                CSVParser records = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(file.getInputStream()));
                List<CSVRecord> csvRecords = records.getRecords();
                for (int i = 0; i < csvRecords.size(); i++) {
                    CSVRecord record = csvRecords.get(i);

                    Movie_source movie_source = new Movie_source();
                    movie_source.setSource(sourceRepository.findById(idsource).orElse(null));
                    if (record.get("officialid").length() == 0)
                        movie_source.setOfficialid(getQualifiedMovieName(record.get("title")));
                    else
                        movie_source.setOfficialid(record.get("officialid"));
                    movie_source.setTitle(record.get("title"));
                    movie_source.setOfficialsite(record.get("officialsite"));
                    movie_source.setPoster(record.get("poster"));
                    if (record.get("releasedate").length() > 0) {
                        try {
                            String date_s = record.get("releasedate");
                            DateFormat dt = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
                            Date date = dt.parse(date_s);
                            movie_source.setReleasedate(new java.sql.Date(date.getTime()));
                        } catch (ParseException e) {
                        }
                    }

                    newMovie_source(movie_source);
                }
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
