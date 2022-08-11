package life.genny.utils.minio;

public class FileUpload {

    private String fileName;
    private String uploadedFileName;

    public FileUpload(String fileName, String uploadedFileName) {
        this.fileName = fileName;
        this.uploadedFileName = uploadedFileName;
    }

    public String fileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String uploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

}
