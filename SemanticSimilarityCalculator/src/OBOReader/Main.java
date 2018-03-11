package OBOReader;


import AnnotationReader.Reader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author zbvd935
 *
 */
public class Main extends Application{

	public void start (Stage primaryStage){
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add("application/application.css");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Reader notationReader = new Reader();
		launch(args);
		
		/*String input = null;
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
				System.out.println("'1' or 'info' gets info about the Term");
				System.out.println("'2' or 'getAncestors' to list ancestors of a Term");
				System.out.println("'3' or 'calcSim' to calculate similarity with another term using Wu and Palmer method");
				input = sc.nextLine();
				
				switch (input){
				case "1": case "info":{
					notationReader.printTermInfo(term);
					break;
				}
				case "2": case "getAncestors":{
					notationReader.listAncestors(term);
					break;
				}
				case "3": case "calcSim":{
					System.out.println("Enter second Term ID! 'GO:xxxxxxx'");
					input=sc.nextLine();
					String term2 = input;
					double result = notationReader.WuPalmerSim(term, term2);
					if (result!=-1) System.out.println("There is a similarity with "+result);
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
		sc.close();*/
	}

}
