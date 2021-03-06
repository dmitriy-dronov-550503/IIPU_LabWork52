package sample.manager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeItem;
import sample.SudoExecutor;
import sample.model.Device;

import java.io.*;

public class DeviceManager {

    private TreeItem<Device> root = new TreeItem<>();

    public TreeItem<Device> getRoot(){
        return root;
    }

    public DeviceManager(){
       findDevices();
    }

    public void findDevices() {
        try {
            SudoExecutor.prepareScript("lshw","lshw -json");
            ProcessBuilder processBuilder = new ProcessBuilder();
            String appDirectory = System.getProperty("user.dir")+"/src/sample";
            processBuilder.command("gksudo","bash", appDirectory+"/"+"lshw"+".sh");
            processBuilder.directory(new File(appDirectory));
            Process lshwProcess = processBuilder.start();
            try {
                lshwProcess.waitFor();
                BufferedReader br = new BufferedReader(new InputStreamReader(lshwProcess.getInputStream()));
                String line = br.readLine();
                StringBuilder builder = new StringBuilder(line);
                while (line != null) {
                    line = br.readLine();
                    builder.append(line);
                }
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
                Device device = mapper.readValue(builder.toString(), Device.class);
                root.setExpanded(true);
                parseTree(device, root);
                outTree(root, "  ");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseTree(Device lshwDevice, TreeItem<Device> root){
        TreeItem<Device> item = new TreeItem<>(lshwDevice);
        item.setExpanded(true);
        if(lshwDevice.getChildren()!=null){
            for (Device device:
                    lshwDevice.getChildren()) {
                parseTree(device, item);
            }
        }
        root.getChildren().add(item);
    }

    private void outTree(TreeItem<Device> root, String spaces){
        System.out.println(spaces+root.getValue());
        if(root.getChildren()!=null){
            for (TreeItem<Device> item:
                    root.getChildren()) {
                outTree(item, spaces+"  ");
            }
        }
    }


}
