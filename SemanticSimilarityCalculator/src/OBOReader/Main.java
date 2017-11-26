package OBOReader;

import java.util.Scanner;

import AnnotationReader.Reader;

/**
 * @author zbvd935
 *
 */
public class Main {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Reader notationReader = new Reader();
		String input = null;
		boolean function = true;
		Scanner sc = new Scanner(System.in);
		//System.out.println("Please feed me a command!");
		while (function){
			System.out.println("CHOICES:");
			System.out.println("'1' or 'chooseTerm' a term");
			System.out.println("'2' or 'printDag' to print BP/CC/MF Dag");
			System.out.println("'3' or 'exit' to exit program");
			input = sc.nextLine();
			
			switch (input){
			
			case "1": case "chooseTerm":  {
				System.out.println("Enter Term ID! 'GO:xxxxxxx'");
				input=sc.nextLine();
				String term = input;
				
				System.out.println("CHOICES:");
				System.out.println("'1' or 'check' gets info about the Term and checks if it is in the up propagated DAG");
				System.out.println("'2' or 'getAncestors' to list ancestors of a Term");
				System.out.println("'3' or 'getAllAncestors' to list ancestors of a Term");
				input = sc.nextLine();
				
				switch (input){
				case "1": case "check":{
					
					break;
				}
				case "2": case "getAncestors":{
					notationReader.listAncestors(term);
					break;
				}
				default: {
					System.out.println("Command not understood, please try again.");
					break;
				}
				}
				break;
			}
			case "2": case "printDag":{
				System.out.println("CHOICES:");
				System.out.println("'1' or 'BP' to chose Biological Process DAG");
				System.out.println("'2' or 'MF' to chose Molecular Function DAG");
				System.out.println("'3' or 'CC' to chose Cellular Component DAG");
				input=sc.nextLine();
				if(input.equals("1") || input.equals("2") || input.equals("3") || input.equals("BP") || input.equals("MF") || input.equals("CC")){
					notationReader.printEdges(input);
				} else {
					System.out.println("Command not understood, please try again.");
				}
				break;
			}
			case "3": case "exit":{
				function = false;
				break;
			}
			default: {
				System.out.println("Command not understood, please try again.");
				break;
			}
			}
		}
		sc.close();
	}

}
