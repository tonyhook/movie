package cc.tonyhook.movie.updater;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Blob;

import javax.sql.rowset.serial.SerialBlob;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cc.tonyhook.movie.Poster;
import cc.tonyhook.movie.PosterRepository;
import cc.tonyhook.movie.Posterimg;
import cc.tonyhook.movie.PosterimgRepository;

@Component
public class UpdaterPoster {
    @Autowired
    private PosterRepository posterRepository;
    @Autowired
    private PosterimgRepository posterimgRepository;

    public boolean updatePosterOnce(Poster poster) {
        try {
            String posterUrl = poster.getImageurl();
            Response resp;
            resp = Jsoup.connect(posterUrl).ignoreContentType(true).maxBodySize(0).execute();

            Blob blob = new SerialBlob(resp.bodyAsBytes());

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(resp.bodyAsBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            poster.setImagehash(hashtext);

            poster.setImagename((posterUrl.split("\\?")[0]).split("/")[posterUrl.split("/").length - 1]);
            poster.setImagetype(resp.contentType());
            posterRepository.save(poster);

            Posterimg posterimg = new Posterimg();
            posterimg.setIdPosterimg(poster.getIdposter());
            posterimg.setImage(blob);
            posterimgRepository.save(posterimg);

            return true;
        } catch (Throwable e) {
            System.out.println("Poster: failed to get " + poster.getImageurl() + ", try again");
            return false;
        }

    }

    public void updatePoster(Poster poster) {
        int count = 0;
        while (count < 3) {
            if (!updatePosterOnce(poster)) {
                count++;
            } else {
                return;
            }
        }
        System.out.println("Poster: failed to get " + poster.getImageurl() + ", give up");
    }

    @Scheduled(fixedRate = 500000, initialDelay = 0)
    public void updatePosterRepository() {
        Iterable<Poster> posters = posterRepository.findAll();

        for (Poster poster : posters) {
            if (posterimgRepository.findOne(poster.getIdposter()) == null) {
                updatePoster(poster);
            }
        }
    }

}
