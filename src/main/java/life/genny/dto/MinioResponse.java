package life.genny.dto;

import java.io.Serializable;
import java.util.List;

public class MinioResponse implements Serializable {

    private List<File> files;

    public MinioResponse() {
    }

    public MinioResponse(List<File> files) {
        this.files = files;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public static class File implements Serializable {
        private String name;
        private String uuid;

        public File() {
        }

        public File(String name, String uuid) {
            this.name = name;
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }
}
