package life.genny.utils.minio;


import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import io.minio.errors.NoResponseException;
import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

public class Minio {

    private static MinioClient minioClient;
    private static String REALM = Optional.ofNullable(System.getenv("REALM")).orElse("internmatch");

    static {
        try {
            minioClient =
                    new MinioClient(MinIOEnvironmentVariable.MINIO_SERVER_URL,
                            MinIOEnvironmentVariable.MINIO_ACCESS_KEY,
                            MinIOEnvironmentVariable.MINIO_PRIVATE_KEY);
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        } catch (InvalidPortException e) {
            e.printStackTrace();
        }
    }

    public static String saveOnStore(FileUpload file) {
        if (uploadFile(REALM.concat("/") + "public", file.uploadedFileName(), file.fileName())) {
            return file.fileName();
        } else {
            return null;
        }
    }

    public static UUID saveOnStore(FileUpload file, UUID userUUID) {
        UUID randomUUID = UUID.randomUUID();
        if (uploadFile(userUUID.toString(), file.uploadedFileName(), randomUUID.toString())) {
            return randomUUID;
        } else {
            return null;
        }
    }

    public static byte[] fetchFromStoreUserDirectory(UUID fileUUID, UUID userUUID) {
        try {
            InputStream object = minioClient.getObject(MinIOEnvironmentVariable.BUCKET_NAME,
                    userUUID.toString() + "/media/" + fileUUID.toString());
            byte[] byteArray = IOUtils.toByteArray(object);
            return byteArray;
        } catch (InvalidKeyException | InvalidBucketNameException
                 | NoSuchAlgorithmException | InsufficientDataException
                 | NoResponseException | ErrorResponseException
                 | InternalException | InvalidArgumentException | IOException
                 | XmlPullParserException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    public static ObjectStat fetchStatFromStorePublicDirectory(UUID fileUUID) {
        try {
            ObjectStat object = minioClient.statObject(MinIOEnvironmentVariable.BUCKET_NAME,
                    REALM + "/" +
                            "public" + "/" +
                            "media" + "/" +
                            fileUUID.toString());
            return object;
        } catch (InvalidKeyException | InvalidBucketNameException
                 | NoSuchAlgorithmException | InsufficientDataException
                 | NoResponseException | ErrorResponseException
                 | InternalException | IOException
                 | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fetchInfoFromStorePublicDirectory(UUID fileUUID) {
        try {
            InputStream object = minioClient.getObject(MinIOEnvironmentVariable.BUCKET_NAME,
                    REALM + "/" +
                            "public" + "/" +
                            "media" + "/" +
                            fileUUID.toString().concat("-info"));
            byte[] byteArray = IOUtils.toByteArray(object);
            return new String(byteArray);
        } catch (InvalidKeyException | InvalidBucketNameException
                 | NoSuchAlgorithmException | InsufficientDataException
                 | NoResponseException | ErrorResponseException
                 | InternalException | InvalidArgumentException | IOException
                 | XmlPullParserException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static byte[] streamFromStorePublicDirectory(UUID fileUUID, Long start, Long end) {
        try {
            InputStream object = minioClient.getObject(MinIOEnvironmentVariable.BUCKET_NAME,
                    REALM + "/" +
                            "public" + "/" +
                            "media" + "/" +
                            fileUUID.toString(), start, end);
            byte[] byteArray = IOUtils.toByteArray(object);
            return byteArray;
        } catch (InvalidKeyException | InvalidBucketNameException
                 | NoSuchAlgorithmException | InsufficientDataException
                 | NoResponseException | ErrorResponseException
                 | InternalException | InvalidArgumentException | IOException
                 | XmlPullParserException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    public static byte[] fetchFromStorePublicDirectory(UUID fileUUID) {
        try {
            InputStream object = minioClient.getObject(MinIOEnvironmentVariable.BUCKET_NAME,
                    REALM + "/" +
                            "public" + "/" +
                            "media" + "/" +
                            fileUUID.toString());
            byte[] byteArray = IOUtils.toByteArray(object);
            return byteArray;
        } catch (InvalidKeyException | InvalidBucketNameException
                 | NoSuchAlgorithmException | InsufficientDataException
                 | NoResponseException | ErrorResponseException
                 | InternalException | InvalidArgumentException | IOException
                 | XmlPullParserException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    public static byte[] fetchFromStorePublicDirectory(String fileName) {
        try {
            InputStream object = minioClient.getObject(MinIOEnvironmentVariable.BUCKET_NAME,
                    REALM + "/" +
                            "public" + "/" +
                            "media" + "/" +
                            fileName);
            byte[] byteArray = IOUtils.toByteArray(object);
            return byteArray;
        } catch (InvalidKeyException | InvalidBucketNameException
                 | NoSuchAlgorithmException | InsufficientDataException
                 | NoResponseException | ErrorResponseException
                 | InternalException | InvalidArgumentException | IOException
                 | XmlPullParserException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    public static void deleteFromStorePublicDirectory(UUID fileUUID) {
        try {
            minioClient.removeObject(MinIOEnvironmentVariable.BUCKET_NAME,
                    REALM + "/" +
                            "public" + "/" +
                            "media" + "/" +
                            fileUUID.toString());
        } catch (InvalidKeyException | InvalidBucketNameException
                 | NoSuchAlgorithmException | InsufficientDataException
                 | NoResponseException | ErrorResponseException
                 | InternalException | InvalidArgumentException | IOException
                 | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public static boolean uploadFile(String sub, String inpt, String uuid) {
        boolean isSuccess = false;

        String path = sub + "/" + "media" + "/" + uuid;
        try {
            boolean isExist = minioClient.bucketExists(MinIOEnvironmentVariable.BUCKET_NAME);
            if (isExist) {
                System.out.println("Bucket " + MinIOEnvironmentVariable.BUCKET_NAME + "already exists.");
            } else {
                System.out.println("Start creat Bucket:" + MinIOEnvironmentVariable.BUCKET_NAME);
                minioClient.makeBucket(MinIOEnvironmentVariable.BUCKET_NAME);
                System.out.println("Finish create Bucket:" + MinIOEnvironmentVariable.BUCKET_NAME);
            }

            minioClient.putObject(MinIOEnvironmentVariable.BUCKET_NAME, path, inpt);
            isSuccess = true;
            System.out.println("Success, File" + inpt + " uploaded to bucket with path:" + path);
        } catch (MinioException | InvalidKeyException
                 | NoSuchAlgorithmException | IOException
                 | XmlPullParserException e) {
            System.out.println("Error occurred when upload file to bucket: " + e.getMessage());
            e.printStackTrace();
        }
        return isSuccess;
    }
}
