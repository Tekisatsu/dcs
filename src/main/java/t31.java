import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;

public class t31 {
    public static void main (String[] args){
        try {
            Document doc = Jsoup.connect(args[0]).get();
            Elements links = doc.select("a[href]");
            System.out.println(links.size());

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}