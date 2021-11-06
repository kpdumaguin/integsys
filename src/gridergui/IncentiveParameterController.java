/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridergui;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agent.MsgBox;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.fund.manager.base.LMasDetTrans;
import org.rmj.fund.manager.parameters.Incentive;

/**
 * FXML Controller class
 *
 * @author user
 */
public class IncentiveParameterController implements Initializable , ScreenInterface{
    private GRider oApp;
    private Incentive oTrans;
    private LMasDetTrans oListener;
    
    private int pnIndex = -1;
    private int pnEditMode;
    private int pnRow = 0;
    private int pnSubItems = 0;
    private boolean pbLoaded = false;
    private String psOldRec;
    private String psBarcode = "";
    private String psDescript = "";
    ObservableList<String> cType = FXCollections.observableArrayList("Branch", "Main Office", "Both");
    ObservableList<String> cPercent = FXCollections.observableArrayList("No", "Yes", "Both");
    
    @FXML
    private Label lblStatus;
    @FXML
    private TextField txtSeeks05;
    @FXML
    private TextField txtField01;
    @FXML
    private TextField txtField02;
    @FXML
    private ComboBox cmbType;
    @FXML
    private ComboBox cmbPercent;
    
    @FXML
    private CheckBox Check01;
    @FXML
    private CheckBox Check02;
    @FXML
    private CheckBox Check03;
    @FXML
    private CheckBox Check04;
    @FXML
    private CheckBox Check05;
    @FXML
    private CheckBox Check06;
    @FXML
    private Button btnBrowse;
    @FXML
    private Button btnNew;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnActivate;
    @FXML
    private Button btnDeactivate;
    @FXML
    private AnchorPane AnchorMainBankInfo;
    @FXML
    private HBox hbButtons;
    @FXML
    private Label lblHeader;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        oListener = new LMasDetTrans() {
            @Override
            public void MasterRetreive(int i, Object o) {
                switch (i){
                    case 1: //xEmployNm
                        txtField01.setText((String) o);
                        break;
                    case 2: 
                        txtField02.setText((String) o);
                        break;
                    case 3: //xBankName
                      
                        System.out.println((String) o);
                        break;
                }
            }

            @Override
            public void DetailRetreive(int i, int i1, Object o) {
            }
        };
        
        oTrans = new Incentive(oApp, oApp.getBranchCode(), false);
        oTrans.setListener(oListener);
        oTrans.setWithUI(true);
        
        btnBrowse.setOnAction(this::cmdButton_Click);
        btnNew.setOnAction(this::cmdButton_Click);
        btnSave.setOnAction(this::cmdButton_Click);
        btnUpdate.setOnAction(this::cmdButton_Click);
        btnSearch.setOnAction(this::cmdButton_Click);
        btnCancel.setOnAction(this::cmdButton_Click);
        btnClose.setOnAction(this::cmdButton_Click);
        btnActivate.setOnAction(this::cmdButton_Click);
        btnDeactivate.setOnAction(this::cmdButton_Click);
//      text field focus
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
//      text field  key pressed
        txtSeeks05.setOnKeyPressed(this::txtField_KeyPressed);
        txtField01.setOnKeyPressed(this::txtField_KeyPressed);
        txtField02.setOnKeyPressed(this::txtField_KeyPressed);
        cmbType.setItems(cType);
        cmbPercent.setItems(cPercent);
        cmbType.getSelectionModel().select(0);
        cmbType.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                pnEditMode = EditMode.UNKNOWN;
        
                clearFields();
                initButton(pnEditMode);
            }    
        });
        cmbPercent.getSelectionModel().select(0);
        cmbPercent.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                pnEditMode = EditMode.UNKNOWN;
        
                clearFields();
                initButton(pnEditMode);
            }    
        });
     
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
    }    

    @Override
    public void setGRider(GRider foValue) {
         oApp = foValue;
    }
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button)event.getSource()).getId();
        try {
            switch (lsButton){
                 case "btnBrowse":
                        if (oTrans.SearchRecord(txtSeeks05.getText(), false)){
                            loadRecord();
                            pnEditMode = EditMode.READY;
                        } else 
                            MsgBox.showOk(oTrans.getMessage());
                    break;
                case "btnNew": //create new transaction
                        pbLoaded = true;
                        if (oTrans.NewRecord()){
                            loadRecord();
                            
                            pnEditMode = oTrans.getEditMode();
                        } else 
                            MsgBox.showOk(oTrans.getMessage());
                    break;
                 case "btnSave":
                        if(sendIncentives()){
                             if (oTrans.SaveRecord()){
                                clearFields();
                                MsgBox.showOk("Record Save Successfully.");
                                pnEditMode = EditMode.UNKNOWN;
                            } else 
                                MsgBox.showOk(oTrans.getMessage());
                        }
                       
                    break;
                case "btnUpdate":
                        if (oTrans.UpdateRecord()){
                            pnEditMode = oTrans.getEditMode();
                        } else 
                            MsgBox.showOk(oTrans.getMessage());
                    break;
                case "btnSearch":
                       
                    break;
                case "btnCancel":
                    clearFields();
                    oTrans = new Incentive(oApp, oApp.getBranchCode(), false);
                    oTrans.setListener(oListener);
                    oTrans.setWithUI(true);
                    pnEditMode = EditMode.UNKNOWN;
                    //reload detail
                    break;
                case "btnActivate":
//                    if (oTrans.ActivateRecord()){
//                        MsgBox.showOk("Account successfully activated!");
//                        clearFields();
//                    }else
//                        MsgBox.showOk(oTrans.getMessage());
                    break;
                case "btnDeactivate":
//                    if (oTrans.DeactivateRecord()){
//                        MsgBox.showOk("Account successfully deactivated!");
//                        clearFields();
//                    }else
//                        MsgBox.showOk(oTrans.getMessage());
                    break;

                case "btnClose":
                    if(ShowMessageFX.OkayCancel(null, "Employee Bank Info", "Do you want to disregard changes?") == true){
                        unloadForm();
                        break;
                    } else
                        return;
            }
            
            initButton(pnEditMode);
        } catch (SQLException e) {
            e.printStackTrace();
            MsgBox.showOk(e.getMessage());
        }
    } 
    private void initButton(int fnValue){
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        
        btnCancel.setVisible(lbShow);
        btnSearch.setVisible(lbShow);
        btnSave.setVisible(lbShow);
        btnActivate.setVisible(!lbShow);
        btnDeactivate.setVisible(!lbShow);
        
        btnSave.setManaged(lbShow);
        btnCancel.setManaged(lbShow);
        btnSearch.setManaged(lbShow);
        btnUpdate.setVisible(!lbShow);
        btnBrowse.setVisible(!lbShow);
        btnNew.setVisible(!lbShow);
        
        txtSeeks05.setDisable(!lbShow);
        txtField01.setDisable(true);
        txtField02.setDisable(!lbShow);
        
        Check01.setDisable(!lbShow);
        Check02.setDisable(!lbShow);
        Check03.setDisable(!lbShow);
        Check04.setDisable(!lbShow);
        Check05.setDisable(!lbShow);
        Check06.setDisable(!lbShow);
        if (lbShow){
            txtSeeks05.setDisable(lbShow);
            txtSeeks05.clear();
            btnCancel.setVisible(lbShow);
            btnSearch.setVisible(lbShow);
            btnSave.setVisible(lbShow);
            btnUpdate.setVisible(!lbShow);
            btnBrowse.setVisible(!lbShow);
            btnNew.setVisible(!lbShow);
            btnBrowse.setManaged(false);
            btnNew.setManaged(false);
            btnUpdate.setManaged(false);
            btnActivate.setManaged(false);
            btnDeactivate.setManaged(false);
        }
        else{
            txtSeeks05.setDisable(lbShow);
            txtSeeks05.requestFocus();
        }
    }
    public void clearFields(){
        txtField01.clear();
        txtField02.clear();
        lblStatus.setVisible(false);
        
        Check01.selectedProperty().setValue(false);
        Check02.selectedProperty().setValue(false);
        Check03.selectedProperty().setValue(false);
        Check04.selectedProperty().setValue(false);
        Check05.selectedProperty().setValue(false);
        Check06.selectedProperty().setValue(false);
    }
    
    private void unloadForm(){
        StackPane myBox = (StackPane) AnchorMainBankInfo.getParent();
        myBox.getChildren().clear();
        myBox.getChildren().add(getScene("MainScreenBG.fxml"));
      
    }
    private AnchorPane getScene(String fsFormName){
         ScreenInterface fxObj = new MainScreenBGController();
         fxObj.setGRider(oApp);
        
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(fsFormName));
        fxmlLoader.setController(fxObj);      
   
        AnchorPane root;
        try {
            root = (AnchorPane) fxmlLoader.load();
            FadeTransition ft = new FadeTransition(Duration.millis(1500));
            ft.setNode(root);
            ft.setFromValue(1);
            ft.setToValue(1);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft.play();

            return root;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }
    
    private void loadRecord(){
        
        try {
            MsgBox.showOk((String) oTrans.getMaster(1));
            txtSeeks05.setText((String) oTrans.getMaster(1));
            txtField01.setText((String) oTrans.getMaster(1));
            txtField02.setText((String) oTrans.getMaster(2));
            System.out.println((String) oTrans.getMaster(3));
            System.out.println((String) oTrans.getMaster(2));
        } catch (SQLException e) {
            MsgBox.showOk(e.getMessage());
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean sendIncentives(){
        try {
            
            if (cmbType.getSelectionModel().getSelectedIndex() < 0){
                ShowMessageFX.Warning("No `Incentive Type` selected.", "Incentive Parameter", "Please select `Unit Type` value.");
                cmbType.requestFocus();
                return false;
            }else 
                oTrans.setMaster(4, String.valueOf(cmbType.getSelectionModel().getSelectedIndex()));
            if (cmbPercent.getSelectionModel().getSelectedIndex() < 0){
                ShowMessageFX.Warning("No `Incentive By Percent` selected.", "Incentive Parameter", "Please select `Inv. Status` value.");
                cmbPercent.requestFocus();
                return false;
            }else 
                oTrans.setMaster(5, String.valueOf(cmbPercent.getSelectionModel().getSelectedIndex()));
            String div = "";
            if(Check01.isSelected())
                div += "1";
            
            if(Check02.isSelected())
                div += "2";
            
            if(Check03.isSelected())
                div += "3";
            
            if(Check04.isSelected())
                div += "4";
            
            if(Check05.isSelected())
                div += "5";
            
            if(Check06.isSelected())
                div += "6";
            
            oTrans.setMaster("sDivision", div);
            oTrans.setMaster("sInctveDs", txtField02.getText());
            } catch (SQLException ex) {
              MsgBox.showOk(ex.getMessage());
        }

        return true;
    }
    final ChangeListener<? super Boolean> txtField_Focus = (o,ov,nv)->{
        if (!pbLoaded) return;
        
        TextField txtField = (TextField)((ReadOnlyBooleanPropertyBase)o).getBean();
        
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
       
        String lsValue = txtField.getText();
        if (lsValue == null) return;
            
        if(!nv){ //Lost Focus
            try {
                switch (lnIndex){
                    case 3: //sBankAcct
                        oTrans.setMaster(lnIndex, lsValue);
                        break;
                }
            } catch (SQLException e) {
                MsgBox.showOk(e.getMessage());
            }
            
        } else{ //Focus
            pnIndex = lnIndex;
            txtField.selectAll();
        }
    };   
    private void txtField_KeyPressed(KeyEvent event){
        TextField txtField = (TextField)event.getSource();        
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
             
        try{
           switch (event.getCode()){
            case F3:
                switch (lnIndex){
                case 1: /*sBranchCd*/
//                     oTrans.se(txtField.getText(), false);
                     break;
                case 2: /*sBankNme*/
//                     oTrans.searchBank(txtField.getText(), false); 
                     break;
                case 5: /*Search*/
                    if (oTrans.SearchRecord(txtSeeks05.getText(), false)){
                        loadRecord();
                        pnEditMode = EditMode.READY;
                    } else 
                        MsgBox.showOk(oTrans.getMessage());
                     break;
                }   
            case ENTER:
            case DOWN:
                CommonUtils.SetNextFocus(txtField); break;
            case UP:
                CommonUtils.SetPreviousFocus(txtField);
        } 
        }catch(SQLException e){
                MsgBox.showOk(e.getMessage());
        }
        
    }
  
}
