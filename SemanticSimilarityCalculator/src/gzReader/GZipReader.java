package gzReader;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


/**
 *
 * @author Attila
 */
public class GZipReader {
    
    public GZipReader() throws IOException{
        gunzipIt();
    }
    
    public void gunzipIt() throws IOException{

        byte[] buffer = new byte[1024];
        GZIPInputStream gzis = null;
        FileOutputStream out = null;
        try{

            gzis = new GZIPInputStream(new FileInputStream("C:\\Users\\Attila\\Documents\\NetBeansProjects\\SemanticSimilarityCalculator\\SemanticSimilarityCalculator\\gene_association.gonuts.gz"));

            out =  new FileOutputStream("C:\\Users\\Attila\\Documents\\NetBeansProjects\\SemanticSimilarityCalculator\\SemanticSimilarityCalculator\\gene_association.gonuts.txt");

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                   out.write(buffer, 0, len);
            }
            System.out.println("File converted!");

        }catch(IOException ex){
            ex.printStackTrace();
        } finally {
            gzis.close();
            out.close();
        }
    }
}

