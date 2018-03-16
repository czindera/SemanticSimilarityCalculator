/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import javafx.scene.layout.Region;
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
    ObservableList<String> methodList = FXCollections.observableArrayList("Resnik","Lin","Jiang","SimgraSM","simUI","simGIC");
    LinkedHashSet<String> geneAssocSet;
    final String dir;
    private HashSet<String> selectedECodes;
    
    ObservableList<String> organismList;
    
    public Controller(){
        this.geneAssocSet = new LinkedHashSet<>();
        geneAssocSet.add("E.Coli(local)");
        organismList = FXCollections.observableArrayList(geneAssocSet);
        dir = System.getProperty("user.dir");
        System.out.println(dir);
        selectedECodes = new HashSet<>();   
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
    private ComboBox TermOrGene1;
    @FXML
    private ComboBox TermOrGene2;
    @FXML
    private ComboBox organism;
    @FXML
    private ChoiceBox simMethod;
    
    @FXML
    private void updateList(ActionEvent event){
        updateOrganismList();
        organism.setItems(organismList);
        organism.setValue("gene_association.ecocyc.gz");
    }
    
    @FXML
    private void buildListOfItems(ActionEvent event) throws IOException{
        String selected = organism.getSelectionModel().getSelectedItem().toString();       
        String location = dir+"\\"+selected;
        if(Files.isRegularFile(Paths.get(location))) {
            createAlert("File exists on system, using the old file to build the DAG.");
            //TODO
            
        } else {
            createAlert("Annotation file not found, download it first!");
        }
    }
    
    private void createAlert(String message){
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show();
    }
    
    public HashSet<String> getSelectedECodes(){
        HashSet<String> result= new HashSet<>();
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
        
        return result;
    }
    
    @FXML
    private void initialize(){
        simMethod.setItems(methodList);
        simMethod.setValue("Resnik");
        organism.setItems(organismList);
        organism.setEditable(true);
        TextFields.bindAutoCompletion(organism.getEditor(), organism.getItems());
        organism.setValue("E.Coli(local)");
        TermOrGene1.setItems(methodList);
        TermOrGene1.setEditable(true);
        TextFields.bindAutoCompletion(TermOrGene1.getEditor(), TermOrGene1.getItems());
        
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

}
