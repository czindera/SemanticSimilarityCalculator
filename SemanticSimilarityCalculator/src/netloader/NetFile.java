package netloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class NetFile {
    static String urls = "http://geneontology.org/gene-associations/gene_association.gonuts.gz";
    public NetFile()throws IOException{
        URL url = verify(urls);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = null;
        String filename = url.getFile();
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        String path = new File(".").getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        System.out.println(path);
        FileOutputStream out = new FileOutputStream(path + File.separator + filename);
        in = connection.getInputStream();
        int read = -1;
        byte[] buffer = new byte[4096];
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
            System.out.println("[SYSTEM/INFO]: Downloading file...");
        }
        in.close();
        out.close();
        System.out.println("[SYSTEM/INFO]: File Downloaded!");
    }
    private static URL verify(String url){
        if(!url.toLowerCase().startsWith("http://")) {
            return null;
        }
        URL verifyUrl = null;

        try{
            verifyUrl = new URL(url);
        }catch(Exception e){
            e.printStackTrace();
        }
        return verifyUrl;
    }
}
