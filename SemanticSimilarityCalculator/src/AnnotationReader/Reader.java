package AnnotationReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
	private BufferedReader in;
	private String buffer;
	
	public Reader(){
		in=null;
		try {
			parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String next() throws IOException {
		if(buffer!=null) {
			String s=buffer;
			buffer=null;
			return s;
		}
		return in.readLine();
	}
	
	private void parse() throws IOException {
	ClassLoader cl = getClass().getClassLoader();
	File annFile = new File(cl.getResource("./AnnotationFiles/gene_association.ecocyc").getFile());
    FileReader fr = new FileReader(annFile);
	in=new BufferedReader(fr);
	String line;
	while((line=next())!=null) {
		if(!line.startsWith("!")) {
			String[] words = line.split("\\s+");
			//System.out.println(words[3]); //GO IDs
		}
	}
	}
}
