package sample.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LSHWDevice {
    private String id;
    private String handle;
    private String description;
    private String product;
    private String vendor;
    private String businfo;
    private String serial;
    private LSHWConfiguration configuration;
    private List<LSHWDevice> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getBusinfo() {
        return businfo;
    }

    public void setBusinfo(String businfo) {
        this.businfo = businfo;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public List<LSHWDevice> getChildren() {
        return children;
    }

    public void setChildren(List<LSHWDevice> children) {
        this.children = children;
    }

    public String getDriverName() {
        return (configuration != null) ? configuration.getDriver() : null;
    }

    public LSHWConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(LSHWConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlot(){
        String result = "[none information]";
        if(businfo!=null){
            try {
                Pattern pci = Pattern.compile("(?<=pci@)(.*)");
                Pattern scsi = Pattern.compile("(?<=scsi@)(.*)");
                Matcher matcher = pci.matcher(businfo);
                if (matcher.find()) {
                    result = matcher.group(1);
                } else {
                    matcher = scsi.matcher(businfo);
                    if (matcher.find()) {
                        result = matcher.group(1).replace('.',':');
                    }
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return result;

    }

    public String getDriverPath(){
        if(businfo!=null) {
            if (businfo.contains("pci")) return "/sys/bus/pci/drivers/"+getDriverName();
            if (businfo.contains("scsi")) return "/sys/bus/scsi/drivers/sr";
        }
        return businfo;
    }

    @Override
    public String toString() {
        return (product==null?"":product+" | ") +
                (description==null?"":description+" | ") +
                (vendor==null?"":vendor+" | ") +
                (id==null?"":id+" | ") +
                (businfo==null?"":businfo)
                ;
    }
}
