package life.genny.dto;

import java.util.Map;

public class FileUploadRequest {

    private String name;
    private String type;
    private Map<Object, Object> data;

    public FileUploadRequest() {
    }

    public FileUploadRequest(String name, String type, Map<Object, Object> data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<Object, Object> getData() {
        return data;
    }

    public void setData(Map<Object, Object> data) {
        this.data = data;
    }
}
