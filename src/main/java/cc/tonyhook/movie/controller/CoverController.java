package cc.tonyhook.movie.controller;

import java.net.URL;
import java.net.URLConnection;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.tonyhook.movie.domain.Album;
import cc.tonyhook.movie.domain.AlbumRepository;
import cc.tonyhook.movie.domain.Cover;
import cc.tonyhook.movie.domain.CoverRepository;
import cc.tonyhook.movie.domain.Coverimg;
import cc.tonyhook.movie.domain.CoverimgRepository;
import cc.tonyhook.movie.domain.Movie;
import cc.tonyhook.movie.domain.MovieRepository;
import cc.tonyhook.movie.domain.Movie_company;

@RestController
public class CoverController {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private CoverRepository coverRepository;
    @Autowired
    private CoverimgRepository coverimgRepository;
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

    @RequestMapping(value = "/music/cover/album/{albumid}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Iterable<Cover>> listCoverByAlbum(@PathVariable("albumid") Integer albumid) {
        Album album = albumRepository.findById(albumid).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Iterable<Cover> covers = coverRepository.findByAlbumidOrderBySequence(albumid);

        return new ResponseEntity<Iterable<Cover>>(covers, HttpStatus.OK);
    }


    @RequestMapping(value = "/music/cover/album/{albumid}/discogs", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getDiscogsCoverUrl(@PathVariable("albumid") Integer albumid) {
        Album album = albumRepository.findById(albumid).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if ((album.getDiscogsID() == null) || (album.getDiscogsID().length() == 0)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            String discogsUrl = "https://www.discogs.com/release/" + album.getDiscogsID();
            String coverUrl = "";

            URLConnection connection = new URL(discogsUrl).openConnection();
            connection.addRequestProperty("Accept-Language", "en-US");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", discogsUrl);
            Elements elements;

            // <div class="image_gallery image_gallery_large" data-images=
            elements = doc.getElementsByTag("div");
            for (Element element : elements) {
                String divClass = element.attr("class");

                if (divClass.equals("image_gallery image_gallery_large")) {
                    String dataImages = element.attr("data-images");
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode imageList = mapper.readTree(dataImages);
                    coverUrl = imageList.get(0).get("full").asText();
                }
            }

            if (coverUrl.length() > 0) {
                Response resp = Jsoup.connect(coverUrl).ignoreContentType(true).maxBodySize(0).execute();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE));
                headers.setContentDisposition(ContentDisposition.builder("inline").filename(
                        coverUrl.split("/")[coverUrl.split("/").length - 1]).build());

                return new ResponseEntity<byte[]>(resp.bodyAsBytes(), headers, HttpStatus.OK);             
            }
            else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Throwable e) {
            System.out.println("Discogs: failed to get " + albumid + "'s Discogs cover");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/music/cover/album/{albumid}/nas", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getNASCover(@PathVariable("albumid") Integer albumid) {
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

        try {
            String coverUrl = "https://" + NAS_ADDRESS + ":" + NAS_PORT
                    + "/" + NAS_SHARE + "/CD/"
                    + companyFolder + "/Soundtrack/"
                    + movieFolder + "/"
                    + albumFolder + "/"
                    + album.getTitle() + ".jpg";
            System.out.println(coverUrl);
            Response resp = Jsoup.connect(coverUrl).ignoreContentType(true).maxBodySize(0).execute();

            if (resp.statusCode() == 200) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE));
                headers.setContentDisposition(ContentDisposition.builder("inline").filename(
                        album.getTitle() + ".jpg").build());

                return new ResponseEntity<byte[]>(resp.bodyAsBytes(), headers, HttpStatus.OK);     
            } else
                return new ResponseEntity<>(HttpStatus.valueOf(resp.statusCode()));
        } catch (HttpStatusException e) {
            return new ResponseEntity<>(HttpStatus.valueOf(e.getStatusCode()));
        } catch (Throwable e) {
            System.out.println("NAS: failed to get " + albumid + "'s NAS cover");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/music/cover/{idcover}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getCover(@PathVariable("idcover") Integer idcover) {
        Cover cover = coverRepository.findById(idcover).orElse(null);
        if (cover == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Coverimg coverimg = coverimgRepository.findById(idcover).orElse(null);
        if (coverimg == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {            
            byte[] media = IOUtils.toByteArray(coverimg.getImage().getBinaryStream());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE));
            headers.setContentDisposition(ContentDisposition.builder("inline").filename(String.valueOf(idcover) + ".jpg").build());

            return new ResponseEntity<>(media, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    private void resortSequence(Album album) {
        if (album == null) {
            return;
        }

        ArrayList<Integer> coverids = new ArrayList<Integer>();
        ArrayList<Integer> coverseqs = new ArrayList<Integer>();
        for (Cover cover: album.getCovers()) {
            coverids.add(cover.getIdcover());
            coverseqs.add(cover.getSequence());
        }
        for (int i = 0; i < coverids.size() - 1; i++)
            for (int j = i + 1; j < coverids.size(); j++)
                if (coverseqs.get(i) > coverseqs.get(j)) {
                    Integer swap;
                    swap = coverids.get(i);
                    coverids.set(i, coverids.get(j));
                    coverids.set(j, swap);
                    swap = coverseqs.get(i);
                    coverseqs.set(i, coverseqs.get(j));
                    coverseqs.set(j, swap);
                }
        for (Cover cover: album.getCovers()) {
            cover.setSequence(coverids.indexOf(cover.getIdcover()) + 1);
            coverRepository.save(cover);
        }

        albumRepository.save(album);
    }

    private void makeWayForCover(Album album, Integer sequence) {
        if (album == null) {
            return;
        }

        for (Cover cover: album.getCovers()) {
            if (cover.getSequence() >= sequence) {
                cover.setSequence(cover.getSequence() + 1);
                coverRepository.save(cover);
            }
        }

        albumRepository.save(album);
    }

    @RequestMapping(value = "/music/cover/{idcover}", method = RequestMethod.POST, consumes = "multipart/form-data")
    public @ResponseBody ResponseEntity<Set<Cover>> setCover(@PathVariable("idcover") Integer idcover,
            @RequestPart(name = "cover") Cover input,
            @RequestPart(name = "file", required = false) MultipartFile file) {
        if (!idcover.equals(input.getIdcover())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Album album = albumRepository.findById(input.getAlbumid()).orElse(null);
        if (album == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Cover cover = null;

        for (Cover existedCover : album.getCovers()) {
            if (existedCover.getIdcover().equals(idcover))
                cover = existedCover;
        }

        if (cover == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        makeWayForCover(album, input.getSequence());

        cover.setSequence(input.getSequence());

        try {
            if (file != null) {
                Blob blob = new SerialBlob(IOUtils.toByteArray(file.getInputStream()));

                Coverimg coverimg = coverimgRepository.findById(idcover).orElse(null);
                if (coverimg == null) {
                    coverimg = new Coverimg();
                    coverimg.setIdcoverimg(idcover);
                }

                coverimg.setImage(blob);

                coverimgRepository.save(coverimg);
            } else {
                System.out.println("setCover: no cover submitted.");
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        coverRepository.save(cover);
        albumRepository.save(album);
        resortSequence(album);

        return new ResponseEntity<Set<Cover>>(album.getCovers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/music/cover", method = RequestMethod.PUT, consumes = "multipart/form-data")
    public @ResponseBody ResponseEntity<Set<Cover>> addCover(@RequestPart(name = "cover") Cover input,
            @RequestPart(name = "file", required = false) MultipartFile file) {
        Album album = albumRepository.findById(input.getAlbumid()).orElse(null);
        if (album == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        makeWayForCover(album, input.getSequence());

        Cover cover = coverRepository.save(input);

        try {
            if (file != null) {
                Blob blob = new SerialBlob(IOUtils.toByteArray(file.getInputStream()));

                Coverimg coverimg = coverimgRepository.findById(cover.getIdcover()).orElse(null);
                if (coverimg == null) {
                    coverimg = new Coverimg();
                    coverimg.setIdcoverimg(cover.getIdcover());
                }

                coverimg.setImage(blob);

                coverimgRepository.save(coverimg);
            } else {
                System.out.println("addCover: no cover submitted.");
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        album.getCovers().add(cover);
        resortSequence(album);
        return new ResponseEntity<Set<Cover>>(album.getCovers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/music/cover/{idcover}", method = RequestMethod.DELETE)
    public @ResponseBody ResponseEntity<Set<Cover>> removeCover(@PathVariable("idcover") Integer idcover) {
        Coverimg coverimg = coverimgRepository.findById(idcover).orElse(null);
        if (coverimg != null) {
            coverimgRepository.delete(coverimg);
        }

        Cover cover = coverRepository.findById(idcover).orElse(null);
        if (cover == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Integer albumid = cover.getAlbumid();
        coverRepository.delete(cover);

        resortSequence(albumRepository.findById(albumid).orElse(null));
        Set<Cover> covers = albumRepository.findById(albumid).orElse(null).getCovers();
        return new ResponseEntity<Set<Cover>>(covers, HttpStatus.OK);
    }

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
