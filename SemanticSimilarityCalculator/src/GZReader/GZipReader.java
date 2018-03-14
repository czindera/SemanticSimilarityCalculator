/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GZReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Attila
 */
public class GZipReader {
    byte[] buffer;
    StringBuilder sb;

    //read file to compress
GZipReader(){
    String read = readFile( "spanish.xml", Charset.defaultCharset());
    buffer = new byte[4096];
    sb = new StringBuilder();
    
    if( read != null )
    {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("spanish-new.xml");
            GZIPInputStream gzis = new GZIPInputStream(fis);
            int bytes = 0;
            while ((bytes = gzis.read(buffer)) != -1) {
                sb.append( new String( buffer ) );
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GZipReader.class.getName()).log(Level.SEVERE, null, ex);    
        } catch (IOException ex) {
            Logger.getLogger(GZipReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(GZipReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    }
    
    static String readFile(String path, Charset encoding) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch (IOException ex) {
            Logger.getLogger(GZipReader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

