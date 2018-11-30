package life.genny.qwandautils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import life.genny.qwanda.FileUploadDetails;
import life.genny.qwanda.entity.BaseEntity;

public class StorageUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	private static AmazonS3 getAmazonClientObj(BaseEntity projectBe) {
		
		String key = projectBe.getValue("PRI_AWS_S3_STORAGE_KEY", null);
		String secret = projectBe.getValue("PRI_AWS_S3_STORAGE_SECRET", null);
		String clientRegion = projectBe.getValue("PRI_AWS_S3_STORAGE_CLIENT_REGION", null);
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(key, secret);
		
		return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(clientRegion)
                .build();
		
	}
	

	public static FileUploadDetails uploadToAWSS3(String downloadableUrl, FileUploadDetails fileUploadDetails, BaseEntity projectBe, String mimeType) {	
		
		AmazonS3 s3Client = getAmazonClientObj(projectBe);
		FileUploadDetails uploadDetails = new FileUploadDetails();
	
		String bucketName = projectBe.getValue("PRI_AWS_S3_STORAGE_BUCKET_NAME", null);
		
		if(bucketName != null && s3Client != null) {
			
			/* unique ID for file */
			String fileUUID = UUID.randomUUID().toString();
			log.info("fileUUID ::"+fileUUID);
			
			/* create a new file */
			File file = new File("/tmp/"+fileUUID);
			
			log.info("file ::"+file.getName());
			log.info("file path ::"+file.getAbsolutePath());
			
			/* get content from URL and convert to inputstream */
			URL url;
			try {
				url = new URL(downloadableUrl);

				uploadDetails = uploadAndGetDetails(s3Client, bucketName, url, file, fileUUID, fileUploadDetails, mimeType);
				
				
			} catch (MalformedURLException e1) {
				 log.error(e1.getMessage());
			}
			 
		}

		log.info("fileUploadDetails ::"+uploadDetails);
		return uploadDetails;

	}
	
	/**
	 * 
	 * @param s3Client
	 * @param bucketName
	 * @param url of file
	 * @param file
	 * @param fileUUID UUID that we generate
	 * @param fileUploadDetails Object to store details of file upload
	 * @param mimeType Mimetype of the file being uploaded
	 * @return
	 */
	private static FileUploadDetails uploadAndGetDetails(AmazonS3 s3Client, String bucketName, URL url, File file, String fileUUID,
			FileUploadDetails fileUploadDetails, String mimeType) {
		
		PutObjectRequest request = null;
		ObjectMetadata metadata = new ObjectMetadata();

		/* copying contents to file outputstream */
		/* using try with resources */
		try (InputStream is = url.openStream(); OutputStream outputStream = new FileOutputStream(file)) {

            // Upload a text string as a new object.
			s3Client.putObject(bucketName, fileUploadDetails.getUploadObjKey(), fileUploadDetails.getUploadObjValue());

			IOUtils.copy(is, outputStream);

			// Upload a file as a new object with ContentType and title specified.
			
			if(fileUploadDetails.getFileName() != null) {
				request = new PutObjectRequest(bucketName, fileUploadDetails.getFileName(), file);
				fileUploadDetails.setFileName(fileUploadDetails.getFileName());
			} else {
				request = new PutObjectRequest(bucketName, fileUUID, file);
				fileUploadDetails.setFileName(fileUUID);
			}		
			
			/* Finally uploads to S3 bucket */
            metadata.setContentType(mimeType);
			metadata.addUserMetadata(fileUploadDetails.getUserMetaDataObjKey(), fileUploadDetails.getUserMetaDataObjValue());
            request.setMetadata(metadata);
            request.withCannedAcl(CannedAccessControlList.PublicRead);
            
            String uploadedUrl = String.valueOf(s3Client.getUrl(bucketName, file.getName()));
                        
            log.info("uploaded url ::"+uploadedUrl);
            fileUploadDetails.setUploadedFilePath(uploadedUrl);	
           
            return fileUploadDetails;
            
        }
        catch(SdkClientException | IOException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            log.error(e.getMessage());
        }
		return null;
	}


	public static String getUploadedS3Link(String fileName, BaseEntity projectBe) {
		
		String uploadedUrl = null;
		AmazonS3 s3Client = getAmazonClientObj(projectBe);
		String bucketName = projectBe.getValue("PRI_AWS_S3_STORAGE_BUCKET_NAME", null);
		
		if(s3Client != null && bucketName != null) {
			uploadedUrl = String.valueOf(s3Client.getUrl(
	                bucketName, 
	                fileName));
	                     
			log.info("uploaded url ::"+uploadedUrl);
		}
		
		return uploadedUrl;
		
	}
	
	public static InputStream getObject(BaseEntity projectBe, String key) {
		
		AmazonS3 s3Client = getAmazonClientObj(projectBe);
		String bucketName = projectBe.getValue("PRI_AWS_S3_STORAGE_BUCKET_NAME", null);
		
		if(s3Client != null && bucketName != null) {
			S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));
			return object.getObjectContent();
		}
		return null;
	}

}
