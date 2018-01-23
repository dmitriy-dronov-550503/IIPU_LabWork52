package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import sample.model.Device;

public class Controller {

    BorderPane root = new BorderPane();
    private Button unbindButton = new Button("Unbind");
    private Button bindButton = new Button("Bind");
    sample.manager.DeviceManager dm = new sample.manager.DeviceManager();
    private TreeView<Device> tree = new TreeView<>(dm.getRoot().getChildren().get(0));
    private ListView<String> capabilitiesListView = new ListView<>();
    private ListView<String> infoListView = new ListView<>();
    private ObservableList<String> capabilitiesList;
    private ObservableList<String> infoList;

    void init(){
        tree.setOnKeyReleased(e -> treeItemSelectedAction());
        tree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                        treeItemSelectedAction();
                }
            }
        });
        initUnbindButton();
        initBindButton();
    }

    void stop(){

    }

    Parent getRoot(){
        Label capLabel = new Label("Capabilities:");
        capabilitiesListView.setItems(capabilitiesList);
        Label infoLabel = new Label("Information:");
        infoListView.setItems(infoList);
        infoListView.setMinWidth(300);

        VBox additionalPanel = new VBox();
        additionalPanel.getChildren().addAll(infoLabel, infoListView,
                capLabel,capabilitiesListView);

        HBox bottomPanel = new HBox();
        bottomPanel.setSpacing(4.0);
        bottomPanel.getChildren().addAll(unbindButton, bindButton);

        BorderPane.setAlignment(tree, Pos.CENTER);
        BorderPane.setMargin(tree, new Insets(5,5,5,5));
        root.setCenter(tree);
        BorderPane.setAlignment(additionalPanel, Pos.CENTER_RIGHT);
        BorderPane.setMargin(additionalPanel, new Insets(5,5,5,5));
        root.setRight(additionalPanel);
        root.setBottom(bottomPanel);

        return root;
    }

    private void initUnbindButton(){
        unbindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                bindOrUnbindButtonAction("unbind");
            }
        });
    }

    private void initBindButton(){
        bindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                bindOrUnbindButtonAction("bind");
            }
        });
    }

    private void treeItemSelectedAction(){
        try {
            treeItemSelected();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void treeItemSelected() throws Exception{
        // Capabilities
        Device device = tree.getSelectionModel().getSelectedItem().getValue();
        capabilitiesList = FXCollections.observableArrayList();
        if(device.getCapabilities()!=null){
            for (String key:
                    device.getCapabilities().keySet()) {
                capabilitiesList.add(key+": "+device.getCapabilities().get(key)+'\n');
            }
        }
        capabilitiesListView.setItems(capabilitiesList);
        // Information
        infoList = FXCollections.observableArrayList();
        infoList.add("HardwareID: " + device.getHardwareID());
        infoList.add("Manufacturer: " + device.getVendor());
        infoList.add("Product: " + device.getProduct());
        infoList.add("Driver path: " + device.getDriverPath());
        infoList.add("GUID: " + device.getGUID());
        infoList.add("Description: " + device.getDescription());
        infoListView.setItems(infoList);
    }

    private void bindOrUnbindButtonAction(String action){
        for (TreeItem<Device> item:
                tree.getSelectionModel().getSelectedItems()) {
            String slot = item.getValue().getSlot();
            String path = item.getValue().getDriverPath();
            String command = "echo "+slot+" | tee -a "+path+"/"+action;
            System.out.println(command);
            try {
                SudoExecutor.exec(action,command);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }


}
