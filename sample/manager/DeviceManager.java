package sample.manager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TreeItem;
import sample.SudoExecutor;
import sample.model.Device;
import sample.model.LSHWDevice;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceManager {

    private TreeItem<LSHWDevice> root = new TreeItem<>();

    public TreeItem<LSHWDevice> getRoot(){
        return root;
    }

    public static void main(String[] args) throws Exception {
        List<Device> devices = new DeviceManager().findDevices();
        devices.forEach((d) -> {
            System.out.println("----");
            System.out.println(d);
        });
    }

    public List<Device> findDevices() {
        List<Device> devices = new ArrayList<>();
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
                LSHWDevice lshwDevice = mapper.readValue(builder.toString(), LSHWDevice.class);
                //
                parseTree(lshwDevice, root);
                outTree(root, "  ");
                //
                lineraizeDevice(lshwDevice, devices);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices;
    }

    private void parseTree(LSHWDevice lshwDevice, TreeItem<LSHWDevice> root){
        TreeItem<LSHWDevice> item = new TreeItem<>(lshwDevice);
        if(lshwDevice.getChildren()!=null){
            for (LSHWDevice device:
                    lshwDevice.getChildren()) {
                parseTree(device, item);
            }
        }
        root.getChildren().add(item);
    }

    /*private void outList(LSHWDevice lshwDevice, String spaces){
        System.out.println(spaces+lshwDevice.getDescription());
        if(lshwDevice.getChildren()!=null){
            for (LSHWDevice device:
                    lshwDevice.getChildren()) {
                outList(device, spaces+"  ");
            }
        }
    }*/

    private void outTree(TreeItem<LSHWDevice> root, String spaces){
        System.out.println(spaces+root.getValue());
        if(root.getChildren()!=null){
            for (TreeItem<LSHWDevice> item:
                    root.getChildren()) {
                outTree(item, spaces+"  ");
            }
        }
    }

    private List<Device> lineraizeDevice(LSHWDevice lshwDevice, List<Device> devices) {
        Device convertedDevice = convertDevice(lshwDevice);
        if (convertedDevice != null) {
            devices.add(convertedDevice);
        }
        if (lshwDevice.getChildren() != null) {
            for (LSHWDevice device : lshwDevice.getChildren()) {
                devices = lineraizeDevice(device, devices);
            }
        }
        return devices;
    }


    private Device convertDevice(LSHWDevice lshwDevice) {
        String busInfo = lshwDevice.getBusinfo();
        Device device = new Device();
        String deviceFolder = "";
        String busName = "";
        String deviceName = "";
        if (busInfo != null) {
            Pattern pattern = Pattern.compile("([^@]+)@([^@,]+)");
            Matcher matcher = pattern.matcher(busInfo);
            matcher.find();
            busName = matcher.group(1);
            String busPath = matcher.group(2);
            deviceFolder = "/sys/bus";
            deviceName = null;
            if ("pci".equals(busName)) {
                deviceFolder += "/pci/devices/" + busPath;
                device.setDeviceDriverName(busPath);
            } else if ("usb".equals(busName)) {
                deviceFolder += "/usb/devices/";
                Pattern usbPathPattern = Pattern.compile("(\\d+)(:(\\d+))?");
                Matcher usbPathMatcher = usbPathPattern.matcher(busPath);
                usbPathMatcher.find();
                if (usbPathMatcher.group(3) != null) {
                    deviceName = usbPathMatcher.group(1) + "-" + usbPathMatcher.group(3);
                    device.setDeviceDriverName(deviceName);
                    deviceFolder += deviceName;
                } else {
                    deviceName = "usb" + usbPathMatcher.group(1);
                    device.setDeviceDriverName(deviceName);
                    deviceFolder += deviceName;
                }
            } else if ("scsi".equals(busName)) {
                if (busInfo.contains(",")) {
                    return null;
                }
                device.setDeviceDriverName(busPath.replace('.', ':'));
                deviceFolder += "/scsi/devices/" + busPath.replace('.', ':');
            } else {
                if ("cpu".equals(busName)) {
                    deviceFolder += "cpu/devices/" + busPath;
                }
            }
            device.setDevicePath(deviceFolder);
        }else{
            device.setDeviceName("NONE");
        }

        try {
            device.setDriverFolder(FileSystems
                    .getDefault().getPath(deviceFolder + "/driver").toRealPath().toString()
            );
        } catch (IOException e) {
            device.setDriverFolder("");
        }
        if (lshwDevice.getConfiguration() != null) {
            String modalias = lshwDevice.getConfiguration().getModalias();
            if ((modalias == null || modalias.isEmpty()) && "usb".equals(busName)) {
                modalias = getUSBModalias(deviceFolder, deviceName);
            }
            if (modalias != null) {
                device.setDriverPath(getDriverLocation(modalias));
                device.setDriverAuthor(getDriverAuthor(modalias));
                device.setDriverInfo(getDriverInfo(modalias));
                device.setHardwareId(modalias);
            }
        }
        device.setDeviceName(lshwDevice.getProduct());
        if(lshwDevice.getProduct()==null) device.setDeviceName(lshwDevice.getDescription());
        device.setDescription(lshwDevice.getDescription());
        device.setManufacturer(lshwDevice.getVendor());
        return device;
    }

    private String getUSBModalias(String devicePath, String deviceNumber) {
        String modaliasFile = devicePath + "/" + deviceNumber + ":1.0/modalias";
        if (deviceNumber.contains("usb")) {
            modaliasFile = devicePath + "/" + deviceNumber.replace("usb", "") + "-0:1.0/modalias";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(modaliasFile))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private String getDriverLocation(String modalias) {
        return getOutputLine("/sbin/modinfo", "-n", modalias);
    }

    private String getDriverInfo(String modalias) {
        return getOutputLine("/sbin/modinfo", "-d", modalias);
    }

    private String getDriverAuthor(String modalias) {
        return getOutputLine("/sbin/modinfo", "-a", modalias);
    }

    private String getOutputLine(String... params) {
        try {
            Process findProcess = Runtime.getRuntime().exec(params);
            BufferedReader br = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
            return br.readLine();
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }

}
