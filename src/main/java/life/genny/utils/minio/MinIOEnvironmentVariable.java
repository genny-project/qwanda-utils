package life.genny.utils.minio;

import java.util.Optional;

public class MinIOEnvironmentVariable {

  public final static String MINIO_SERVER_URL;
  public final static String MINIO_ACCESS_KEY;
  public final static String MINIO_PRIVATE_KEY;
  public final static String BUCKET_NAME;

  static {
    /* Values from Environment */
    Optional<String> minioServerURL =
        Optional.ofNullable(System.getenv("MINIO_SERVER_URL"));
    Optional<String> minioAccessKey =
        Optional.ofNullable(System.getenv("MINIO_ACCESS_KEY"));
    Optional<String> minioPrivateKey =
        Optional.ofNullable(System.getenv("MINIO_SECRET_KEY"));
    Optional<String> bucketName =
        Optional.ofNullable(System.getenv("BUCKET_NAME"));

    /* Or Else Default Values */
    MINIO_SERVER_URL = minioServerURL.orElse("http://localhost:9000");
    MINIO_ACCESS_KEY = minioAccessKey.orElse("minioadmin");
    MINIO_PRIVATE_KEY = minioPrivateKey
        .orElse("minioadmin");
    BUCKET_NAME = bucketName.orElse("internmatch");
  }

}
