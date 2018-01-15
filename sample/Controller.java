package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import sample.model.Device;
import sample.model.LSHWDevice;

import javax.swing.*;

public class Controller {

    AnchorPane root = new AnchorPane();
    private TableView<Device> table = new TableView<Device>();
    private Button unbindButton = new Button("Unbind");
    private Button bindButton = new Button("Bind");
    sample.manager.DeviceManager dm = new sample.manager.DeviceManager();
    final ObservableList<Device> data = FXCollections.observableArrayList(dm.findDevices());

    private TreeView<LSHWDevice> tree = new TreeView<>(dm.getRoot());

    void init(){

        /*try {
            parseCommand();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        TableColumn slotCol = new TableColumn("Slot");
        slotCol.setMinWidth(50);
        slotCol.prefWidthProperty().bind(table.widthProperty().divide(11));
        slotCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("deviceDriverName"));

        TableColumn nameCol = new TableColumn("Name");
        nameCol.prefWidthProperty().bind(table.widthProperty().divide(3.6));
        nameCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("deviceName"));

        TableColumn classCol = new TableColumn("GUID");
        classCol.prefWidthProperty().bind(table.widthProperty().divide(4.75));
        classCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("GUID"));

        TableColumn vendorCol = new TableColumn("Vendor");
        vendorCol.prefWidthProperty().bind(table.widthProperty().divide(4.75));
        vendorCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("manufacturer"));

        TableColumn pathCol = new TableColumn("Path");
        pathCol.prefWidthProperty().bind(table.widthProperty().divide(4.75));
        pathCol.setCellValueFactory(
                new PropertyValueFactory<Device, String>("driverFolder"));


        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setItems(data);
        table.getColumns().addAll(slotCol, nameCol, classCol, vendorCol, pathCol);

        initUnbindButton();
        initBindButton();

    }

    void stop(){

    }

    Parent getRoot(){
        HBox bottomPanel = new HBox();
        AnchorPane.setBottomAnchor(bottomPanel, 0.0);
        bottomPanel.setSpacing(4.0);
        bottomPanel.getChildren().addAll(unbindButton, bindButton);

        /*AnchorPane.setBottomAnchor(table, 26.0);
        AnchorPane.setLeftAnchor(table,0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        AnchorPane.setTopAnchor(table,0.0);*/
        AnchorPane.setBottomAnchor(tree, 26.0);
        AnchorPane.setLeftAnchor(tree,0.0);
        AnchorPane.setRightAnchor(tree, 0.0);
        AnchorPane.setTopAnchor(tree,0.0);

        //root.getChildren().addAll(table, bottomPanel);
        root.getChildren().addAll(tree, bottomPanel);
        return root;
    }

    void initUnbindButton(){
        unbindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TablePosition pos:
                        table.getSelectionModel().getSelectedCells()) {
                    String slot = table.getItems().get(pos.getRow()).getDeviceDriverName();
                    String path = table.getItems().get(pos.getRow()).getDriverFolder();
                    String appDirectory = System.getProperty("user.dir")+"/src/sample";
                    try {
                        BindManager bindManager = new BindManager(slot, path);
                        bindManager.unbind();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    void initBindButton(){
        bindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TablePosition pos:
                        table.getSelectionModel().getSelectedCells()) {
                    String slot = table.getItems().get(pos.getRow()).getDeviceDriverName();
                    String path = table.getItems().get(pos.getRow()).getDriverFolder();
                    String appDirectory = System.getProperty("user.dir")+"/src/sample";
                    try {
                        BindManager bindManager = new BindManager(slot, path);
                        bindManager.bind();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /*
    void parseCommand() throws Exception{
        Process p = Runtime.getRuntime().exec("lspci -vmm -k");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        int returnCode = p.waitFor();
        if (returnCode >= 2) {
            System.out.println("OS Error: Unable to Find File or other OS error.");
        }

        Device device = new Device();
        while (br.ready()) {
            String line = br.readLine();
            int matchIndex = line.indexOf("Slot:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Slot:".length()).trim();
                device.setSlot(line);
                continue;
            }
            matchIndex = line.indexOf("Class:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Class:".length()).trim();
                device.setClassName(line);
                continue;
            }
            matchIndex = line.indexOf("Vendor:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Vendor:".length()).trim();
                device.setVendor(line);
                continue;
            }
            matchIndex = line.indexOf("Device:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Device:".length()).trim();
                device.setName(line);
                continue;
            }
            matchIndex = line.indexOf("Driver:");
            if (matchIndex != -1) {
                line = line.substring(matchIndex + "Driver:".length()).trim();
                device.setPath("/sys/bus/pci/drivers/"+line);
                continue;
            }
            matchIndex = line.indexOf("Module:");
            if (matchIndex != -1 && device.getDevicePath().isEmpty()) {
                line = line.substring(matchIndex + "Module:".length()).trim();
                device.setPath("/sys/bus/pci/drivers/"+line);
                continue;
            }

            if(line.equals("")){
                data.add(device);
                device = new Device();
            }
        }
    }
*/


}
