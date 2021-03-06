package controller;

import AnnotationReader.Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.Callback;

import org.controlsfx.control.textfield.TextFields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Attila
 */

public class Controller {
    //ArrayList<String> geneassocList = new ArrayList<>();
    ObservableList<String> methodList = FXCollections.observableArrayList("Resnik","Lin","Jiang");
    LinkedHashSet<String> geneAssocSet;
    final String dir;
    Reader annotReader;
    private final static Logger LOGGER = Logger.getLogger(Controller.class.getName());
    ObservableList<String> organismList;
    ObservableList<String> termListBP;
    ObservableList<String> termListMF;
    ObservableList<String> termListCC;
    ObservableList<String> geneList;
    
    public Controller(){
        annotReader = new Reader();
    	this.geneAssocSet = new LinkedHashSet<>();
        geneAssocSet.add("E.Coli(local)");
        this.organismList = FXCollections.observableArrayList(geneAssocSet);
        
        dir = System.getProperty("user.dir");  
    }
    
    /**
     * Creates a list of online available annotations.
     * The combobox will be filled with these elements.
     */
    private void updateOrganismList(){        
        try {
            Document doc = Jsoup.connect("http://geneontology.org/gene-associations/").get();
            Elements links = doc.select("a");
            links.stream().filter((link) -> (link.text().endsWith(".gz"))).forEach((link) -> {
                geneAssocSet.add(link.text());
            });
            //System.out.println(geneassocList.toString());
            organismList.clear();
            geneAssocSet.forEach((s) -> {
                organismList.add(s);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private ComboBox<String> bpTerms1;
    @FXML
    private ComboBox<String> bpTerms2;
    @FXML
    private ComboBox<String> mfTerms1;
    @FXML
    private ComboBox<String> mfTerms2;
    @FXML
    private ComboBox<String> ccTerms1;
    @FXML
    private ComboBox<String> ccTerms2;
    @FXML
    private ComboBox<String> organism;
    @FXML
    private ChoiceBox<String> simMethod;
    @FXML
    private ComboBox<String> genes1;
    @FXML
    private ComboBox<String> genes2;
    @FXML
    private Label resultLabel;
    @FXML
    private RadioButton termwise;
    @FXML
    private RadioButton bestMatch;
    @FXML
    private RadioButton average;
    @FXML
    private Text BP;
    @FXML
    private Text CC;
    @FXML
    private Text MF;
    
    /**
     * The functionality of the update button
     * @param event
     */
    @FXML
    private void updateList(ActionEvent event){
        updateOrganismList();
        organism.setItems(organismList);
        organism.setValue("gene_association.ecocyc.gz");
    }
    
    /**
     * The funcionality of the calculate button
     */
    @FXML
    private void calculate(){
    	boolean termCalculation = termwise.isSelected();
    	boolean isBestMatch = bestMatch.isSelected();
    	boolean isDiShIn = DiShIn.isSelected();
    	if (termCalculation){
			String bpterm1 = bpTerms1.getSelectionModel().getSelectedItem();
			String bpterm2 = bpTerms2.getSelectionModel().getSelectedItem();
			String ccterm1 = ccTerms1.getSelectionModel().getSelectedItem();
			String ccterm2 = ccTerms2.getSelectionModel().getSelectedItem();
			String mfterm1 = mfTerms1.getSelectionModel().getSelectedItem();
			String mfterm2 = mfTerms2.getSelectionModel().getSelectedItem();
			String bpresult="",mfresult="",ccresult="";
			switch (simMethod.getSelectionModel().getSelectedIndex()){
				case 0: 
					bpresult = String.valueOf(annotReader.ResnikSim(bpterm1, bpterm2,isDiShIn));
			    	mfresult = String.valueOf(annotReader.ResnikSim(mfterm1, mfterm2,isDiShIn));
			    	ccresult = String.valueOf(annotReader.ResnikSim(ccterm1, ccterm2,isDiShIn));
			    	break;
				case 1:
					bpresult = String.valueOf(annotReader.DekangLinSim(bpterm1, bpterm2,isDiShIn));
			    	mfresult = String.valueOf(annotReader.DekangLinSim(mfterm1, mfterm2,isDiShIn));
			    	ccresult = String.valueOf(annotReader.DekangLinSim(ccterm1, ccterm2,isDiShIn));
					break;
				case 2:
					bpresult = String.valueOf(annotReader.JiangConrathSim(bpterm1, bpterm2,isDiShIn));
			    	mfresult = String.valueOf(annotReader.JiangConrathSim(mfterm1, mfterm2,isDiShIn));
			    	ccresult = String.valueOf(annotReader.JiangConrathSim(ccterm1, ccterm2,isDiShIn));
					break;
				default:
					bpresult = String.valueOf(annotReader.ResnikSim(bpterm1, bpterm2,isDiShIn));
			    	mfresult = String.valueOf(annotReader.ResnikSim(mfterm1, mfterm2,isDiShIn));
			    	ccresult = String.valueOf(annotReader.ResnikSim(ccterm1, ccterm2,isDiShIn));
			    	break;
			}
			String result = "The selected (BP) terms have a similarity value of: "+bpresult+"\n";
			result += "The selected (MF) terms have a similarity value of: "+mfresult+"\n";
			result += "The selected (CC) terms have a similarity value of: "+ccresult+"\n";
			resultLabel.setText(result);
    	
    	} else {
    		String selectedGene1 = genes1.getSelectionModel().getSelectedItem();
    		String selectedGene2 = genes2.getSelectionModel().getSelectedItem();
    		String result ="";
    		switch (simMethod.getSelectionModel().getSelectedIndex()){
    			case 0:
    				result = "Selected genes Resnik similarity: "+annotReader.geneResnikSim(selectedGene1,selectedGene2,isBestMatch,isDiShIn);
    				break;
    			case 1:
    				result = "Selected genes DekangLin similarity: "+annotReader.geneDekangLinSim(selectedGene1,selectedGene2,isBestMatch,isDiShIn);
    				break;
    			case 2:
    				result = "Selected genes JiangConrath similarity: "+annotReader.geneJiangConrathSim(selectedGene1,selectedGene2,isBestMatch,isDiShIn);
    				break;
    			case 3:
    				result = "Selected genes simUI value: "+annotReader.simUI(selectedGene1,selectedGene2);
    				break;
    			case 4:
					result = "Selected genes simGIC value: "+annotReader.simGIC(selectedGene1,selectedGene2);
					break;
				default:
					result = "Selected genes Resnik similarity: "+annotReader.geneResnikSim(selectedGene1,selectedGene2,isBestMatch,isDiShIn);
    				break;
    		}
    		resultLabel.setText(result);
    	}
    }
    
    /**
     * Action on setting gene wise calculation
     * @param event
     */
    @FXML
    private void setGeneOperation(ActionEvent event){
    	bpTerms1.setVisible(false);
    	bpTerms2.setVisible(false);
    	mfTerms1.setVisible(false);
    	mfTerms2.setVisible(false);
    	ccTerms1.setVisible(false);
    	ccTerms2.setVisible(false);
    	genes1.setVisible(true);
    	genes2.setVisible(true);
    	average.setVisible(true);
    	bestMatch.setVisible(true);
    	BP.setVisible(false);
    	CC.setVisible(false);
    	MF.setVisible(false);
    	methodList.clear();
    	methodList = FXCollections.observableArrayList("Resnik","Lin","Jiang","simUI","simGIC");
    	simMethod.setItems(methodList);
        simMethod.setValue("Resnik");
    }
    
    /**
     * Action on setting term wise calculation
     * @param event
     */
    @FXML
    private void setTermOperation(ActionEvent event){
    	bpTerms1.setVisible(true);
    	bpTerms2.setVisible(true);
    	mfTerms1.setVisible(true);
    	mfTerms2.setVisible(true);
    	ccTerms1.setVisible(true);
    	ccTerms2.setVisible(true);
    	genes1.setVisible(false);
    	genes2.setVisible(false);
    	average.setVisible(false);
    	bestMatch.setVisible(false);
    	BP.setVisible(true);
    	CC.setVisible(true);
    	MF.setVisible(true);
    	methodList.clear();
    	methodList = FXCollections.observableArrayList("Resnik","Lin","Jiang");
    	simMethod.setItems(methodList);
        simMethod.setValue("Resnik");
    }
    
    
   /**
    * To init and update comboboxes
    */
    private void initAndUpdateAllCombobox(){
    	termListBP = FXCollections.observableArrayList(annotReader.getBPterms());
    	bpTerms1.setItems(termListBP);
        TextFields.bindAutoCompletion(bpTerms1.getEditor(), bpTerms1.getItems());
        bpTerms2.setItems(termListBP);
        TextFields.bindAutoCompletion(bpTerms2.getEditor(), bpTerms2.getItems());
        bpTerms1.setTooltip(new Tooltip());
        
        bpTerms1.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    @Override public ListCell<String> call(ListView<String> param) {
                        final ListCell<String> cell = new ListCell<String>() {
                            {
                                super.setPrefWidth(100);
                            }    
                            @Override public void updateItem(String item, 
                                boolean empty) {
                                    super.updateItem(item, empty);
                                    if (item != null) {
                                        setText(item);    
                                        Tooltip tt = new Tooltip();
                                        String value = annotReader.getName(item)+"\n"+annotReader.getDef(item); 
                                        tt.setText(value);
                                        setTooltip(tt);
                                    }
                                    else {
                                        setText(null);
                                    }
                                }
                    };
                    return cell;
                }
            });
        
        bpTerms2.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    @Override public ListCell<String> call(ListView<String> param) {
                        final ListCell<String> cell = new ListCell<String>() {
                            {
                                super.setPrefWidth(100);
                            }    
                            @Override public void updateItem(String item, 
                                boolean empty) {
                                    super.updateItem(item, empty);
                                    if (item != null) {
                                        setText(item);    
                                        Tooltip tt = new Tooltip();
                                        String value = annotReader.getName(item)+"\n"+annotReader.getDef(item); 
                                        tt.setText(value);
                                        setTooltip(tt);
                                    }
                                    else {
                                        setText(null);
                                    }
                                }
                    };
                    return cell;
                }
            });
        
        termListCC = FXCollections.observableArrayList(annotReader.getCCterms());
    	ccTerms1.setItems(termListCC);
        TextFields.bindAutoCompletion(ccTerms1.getEditor(), ccTerms1.getItems());
        ccTerms2.setItems(termListCC);
        TextFields.bindAutoCompletion(ccTerms2.getEditor(), ccTerms2.getItems());
        
        ccTerms1.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    @Override public ListCell<String> call(ListView<String> param) {
                        final ListCell<String> cell = new ListCell<String>() {
                            {
                                super.setPrefWidth(100);
                            }    
                            @Override public void updateItem(String item, 
                                boolean empty) {
                                    super.updateItem(item, empty);
                                    if (item != null) {
                                        setText(item);    
                                        Tooltip tt = new Tooltip();
                                        String value = annotReader.getName(item)+"\n"+annotReader.getDef(item); 
                                        tt.setText(value);
                                        setTooltip(tt);
                                    }
                                    else {
                                        setText(null);
                                    }
                                }
                    };
                    return cell;
                }
            });
        
        ccTerms2.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    @Override public ListCell<String> call(ListView<String> param) {
                        final ListCell<String> cell = new ListCell<String>() {
                            {
                                super.setPrefWidth(100);
                            }    
                            @Override public void updateItem(String item, 
                                boolean empty) {
                                    super.updateItem(item, empty);
                                    if (item != null) {
                                        setText(item);    
                                        Tooltip tt = new Tooltip();
                                        String value = annotReader.getName(item)+"\n"+annotReader.getDef(item); 
                                        tt.setText(value);
                                        setTooltip(tt);
                                    }
                                    else {
                                        setText(null);
                                    }
                                }
                    };
                    return cell;
                }
            });
        
        termListMF = FXCollections.observableArrayList(annotReader.getMFterms());
    	mfTerms1.setItems(termListMF);
        TextFields.bindAutoCompletion(mfTerms1.getEditor(), mfTerms1.getItems());
        mfTerms2.setItems(termListMF);
        TextFields.bindAutoCompletion(mfTerms2.getEditor(), mfTerms2.getItems());
        
        mfTerms1.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    @Override public ListCell<String> call(ListView<String> param) {
                        final ListCell<String> cell = new ListCell<String>() {
                            {
                                super.setPrefWidth(100);
                            }    
                            @Override public void updateItem(String item, 
                                boolean empty) {
                                    super.updateItem(item, empty);
                                    if (item != null) {
                                        setText(item);    
                                        Tooltip tt = new Tooltip();
                                        String value = annotReader.getName(item)+"\n"+annotReader.getDef(item); 
                                        tt.setText(value);
                                        setTooltip(tt);
                                    }
                                    else {
                                        setText(null);
                                    }
                                }
                    };
                    return cell;
                }
            });
        mfTerms2.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    @Override public ListCell<String> call(ListView<String> param) {
                        final ListCell<String> cell = new ListCell<String>() {
                            {
                                super.setPrefWidth(100);
                            }    
                            @Override public void updateItem(String item, 
                                boolean empty) {
                                    super.updateItem(item, empty);
                                    if (item != null) {
                                        setText(item);    
                                        Tooltip tt = new Tooltip();
                                        String value = annotReader.getName(item)+"\n"+annotReader.getDef(item); 
                                        tt.setText(value);
                                        setTooltip(tt);
                                    }
                                    else {
                                        setText(null);
                                    }
                                }
                    };
                    return cell;
                }
            });
        
        geneList = FXCollections.observableArrayList(annotReader.getGeneList());
        genes1.setItems(geneList);
        TextFields.bindAutoCompletion(genes1.getEditor(), genes1.getItems());
        genes2.setItems(geneList);
        TextFields.bindAutoCompletion(genes2.getEditor(), genes2.getItems());
    }
    
    /**
     * this method calls the initAndUpdateAllCombobox
     * @param event
     * @throws IOException
     */
    @FXML
    private void buildListOfItems(ActionEvent event) throws IOException{
        String selected = organism.getSelectionModel().getSelectedItem().toString();       
        String selectedtxt = selected;
    	selectedtxt.substring(0, selectedtxt.length() - 3);
        selectedtxt = selectedtxt+".txt";
        String location = dir+File.separator+selected;
        
        if (selected.equals("E.Coli(local)")){
        	annotReader.updateReader("E.Coli(local)", getSelectedECodes());
        	initAndUpdateAllCombobox();
        } else{
        	if(Files.isRegularFile(Paths.get(location))) {
                createAlert("File exists on system, using the "+selected+" file to build the DAG.");
                //System.out.println("selected: "+selected);
                //getSelectedECodes().forEach( x -> System.out.println(x));
                
                annotReader.updateReader(selectedtxt, getSelectedECodes());
                initAndUpdateAllCombobox();
                
            } else {
                createAlert("Annotation file not found, download it first!");
            }
        }
        
    }
    
    /**
     * If the file not present, this method will be called instead of buildListOfItems
     * @param event
     * @throws IOException
     */
    @FXML
    private void downloadAndBuildListofItems(ActionEvent event) throws IOException{
    	String selectedgz = organism.getSelectionModel().getSelectedItem().toString();       
    	if (selectedgz.equals("E.Coli(local)")){return;}
    	String selectedtxt = selectedgz;
    	selectedtxt.substring(0, selectedtxt.length() - 3);
        selectedtxt = selectedtxt+".txt";
        String location = dir+File.separator+selectedgz;
        if(Files.isRegularFile(Paths.get(location))) {
            createAlert("Overwriting existing file.");    
        } else {
            createAlert("Downloading file to user directory.");   
        }
        
        FileDownloader(selectedgz);
        gunzipIt(selectedgz,location);
        annotReader.updateReader(selectedtxt, getSelectedECodes());
        initAndUpdateAllCombobox();
    }
    
    /**
     * A file downloader that saves the file in the user dir
     * @param selected
     * @throws IOException
     */
    public void FileDownloader(String selected)throws IOException{
    	String urls = "http://geneontology.org/gene-associations/"+selected;
    	URL url = verify(urls);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = null;
        String filename = url.getFile();
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        String path = new File(".").getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        //System.out.println(path);
        //Paths.get(System.getProperty("user.dir")).resolve(filename);
        FileOutputStream out = new FileOutputStream(path + File.separator + filename);
        in = connection.getInputStream();
        int read = -1;
        byte[] buffer = new byte[4096];
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
            
            LOGGER.info("[SYSTEM/INFO]: Downloading file...");
        }
        in.close();
        out.close();
        LOGGER.info("[SYSTEM/INFO]: File Downloaded!");
    }
        
    /**
     * part of the file downloader to check if the given url exists
     * @param url
     * @return
     */
    private URL verify(String url){
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
    
    /*
     * This method will unzip a Gz file to a .txt file
     */
    private void gunzipIt(String fileName, String location) throws IOException{

        byte[] buffer = new byte[1024];
        GZIPInputStream gzin = null;
        FileOutputStream txtout = null;
        gzin = new GZIPInputStream(new FileInputStream(location));
        txtout =  new FileOutputStream(dir+File.separator+fileName+".txt");
        //fileName = fileName + ".txt";
        //txtout =  new FileOutputStream(Paths.get(System.getProperty("user.dir")).resolve(fileName));
        int len;
        while ((len = gzin.read(buffer)) > 0) {
               txtout.write(buffer, 0, len);
        }
        LOGGER.info("File converted!");      
        gzin.close();
        txtout.close();
    }
    
    /*
     * creating custom Alert windows with OK button
     */
    private void createAlert(String message){
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show();
    }
    
    /*
     * To get the list of selected ECodes
     */
    private HashSet<String> getSelectedECodes(){
        HashSet<String> selectedECodes= new HashSet<>();
        if (EXP.isSelected()){selectedECodes.add("EXP");}
        if (IDA.isSelected()){selectedECodes.add("IDA");}
        if (IPI.isSelected()){selectedECodes.add("IPI");}
        if (IMP.isSelected()){selectedECodes.add("IMP");}
        if (IEP.isSelected()){selectedECodes.add("IEP");}
        if (IGI.isSelected()){selectedECodes.add("IGI");}
        if (TAS.isSelected()){selectedECodes.add("TAS");}
        if (NAS.isSelected()){selectedECodes.add("NAS");}
        if (IEA.isSelected()){selectedECodes.add("IEA");}
        if (IC.isSelected()){selectedECodes.add("IC");}
        if (ND.isSelected()){selectedECodes.add("ND");}
        if (ISS.isSelected()){selectedECodes.add("ISS");}
        if (ISO.isSelected()){selectedECodes.add("ISO");}
        if (ISA.isSelected()){selectedECodes.add("ISA");}
        if (ISM.isSelected()){selectedECodes.add("ISM");}
        if (IGC.isSelected()){selectedECodes.add("IGC");}
        if (RCA.isSelected()){selectedECodes.add("RCA");}
        if (IBA.isSelected()){selectedECodes.add("IBA");}
        if (IBD.isSelected()){selectedECodes.add("IBD");}
        if (IKR.isSelected()){selectedECodes.add("IKR");}
        if (IRD.isSelected()){selectedECodes.add("IRD");}
        
        return selectedECodes;
    }
    
    /**
     * init method
     */
    @FXML
    private void initialize(){
        simMethod.setItems(methodList);
        simMethod.setValue("Resnik");
        organism.setItems(organismList);
        organism.setEditable(true);
        TextFields.bindAutoCompletion(organism.getEditor(), organism.getItems());
        organism.setValue("E.Coli(local)");
        initAndUpdateAllCombobox();
        bpTerms1.setEditable(true);
        bpTerms2.setEditable(true);
        ccTerms1.setEditable(true);
        ccTerms2.setEditable(true);
        mfTerms1.setEditable(true);
        mfTerms2.setEditable(true);
        genes1.setEditable(true);
        genes2.setEditable(true);
        genes1.setVisible(false);
    	genes2.setVisible(false);
    	average.setVisible(false);
    	bestMatch.setVisible(false);
    	resultLabel.setText("Start comparing Terms or Genes.\nIf you want Improved Similarity (ISM) calculation for all methods, check DiShIn.");
    }
    
    @FXML
    private CheckBox EXP;
    @FXML
    private CheckBox IDA;
    @FXML
    private CheckBox IPI;
    @FXML
    private CheckBox IMP;
    @FXML
    private CheckBox IEP;
    @FXML
    private CheckBox IGI;
    @FXML
    private CheckBox TAS;
    @FXML
    private CheckBox NAS;
    @FXML
    private CheckBox IEA;
    @FXML
    private CheckBox IC;
    @FXML
    private CheckBox ND;
    @FXML
    private CheckBox ISS;
    @FXML
    private CheckBox ISO;
    @FXML
    private CheckBox ISA;
    @FXML
    private CheckBox ISM;
    @FXML
    private CheckBox RCA;
    @FXML
    private CheckBox IGC;
    @FXML
    private CheckBox IBA;
    @FXML
    private CheckBox IBD;
    @FXML
    private CheckBox IKR;
    @FXML
    private CheckBox IRD;
    @FXML
    private CheckBox DiShIn;

}
