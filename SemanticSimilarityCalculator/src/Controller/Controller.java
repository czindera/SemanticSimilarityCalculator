/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Attila
 */
public class Controller {
    
    ObservableList<String> methodList = FXCollections.observableArrayList("Resnik","Lin","Jiang","SimgraSM","simUI","simGIC");
    //ObservableList<String> organismList = FXCollections.observableArrayList();

    @FXML
    private ComboBox organism;
    @FXML
    private ChoiceBox simMethod;
    
    @FXML
    private void initialize(){
        simMethod.setItems(methodList);
        simMethod.setValue("Resnik");
        //organism.setItems();
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
