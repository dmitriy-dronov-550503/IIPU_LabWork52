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
    private Button unbindButton = new Button("Unbind");
    private Button bindButton = new Button("Bind");
    sample.manager.DeviceManager dm = new sample.manager.DeviceManager();
    final ObservableList<Device> data = FXCollections.observableArrayList(dm.findDevices());
    private TreeView<LSHWDevice> tree = new TreeView<>(dm.getRoot().getChildren().get(0));

    void init(){
        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

        AnchorPane.setBottomAnchor(tree, 26.0);
        AnchorPane.setLeftAnchor(tree,0.0);
        AnchorPane.setRightAnchor(tree, 0.0);
        AnchorPane.setTopAnchor(tree,0.0);

        root.getChildren().addAll(tree, bottomPanel);
        return root;
    }

    void initUnbindButton(){
        unbindButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TreeItem<LSHWDevice> item:
                     tree.getSelectionModel().getSelectedItems()) {
                    String slot = item.getValue().getSlot();
                    String path = item.getValue().getDriverPath();
                    String command = "echo "+slot+" | tee -a "+path+"/unbind";
                    System.out.println(command);
                    try {
                        SudoExecutor.exec("unbind",command);
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
                for (TreeItem<LSHWDevice> item:
                        tree.getSelectionModel().getSelectedItems()) {
                    String slot = item.getValue().getSlot();
                    String path = item.getValue().getDriverPath();
                    String command = "echo "+slot+" | tee -a "+path+"/bind";
                    System.out.println(command);
                    try {
                        SudoExecutor.exec("bind",command);
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
