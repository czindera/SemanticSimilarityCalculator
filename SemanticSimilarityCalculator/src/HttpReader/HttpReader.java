/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HttpReader;

import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Attila
 */
public class HttpReader {
    ArrayList<String> geneassocList;
    public HttpReader(){
        //return this list
        this.geneassocList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("http://geneontology.org/gene-associations/").get();
            Elements links = doc.select("a");
            links.stream().filter((link) -> (link.text().endsWith(".gz"))).forEach((link) -> {
                geneassocList.add("http://geneontology.org/gene-associations/"+link.text());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
            }
    
}
