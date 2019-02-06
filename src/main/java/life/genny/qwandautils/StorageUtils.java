package life.genny.qwandautils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

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
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(clientRegion)
                .build();
		
		return s3Client;
		
	}
	

	public static FileUploadDetails uploadToAWSS3(String downloadableUrl, FileUploadDetails fileUploadDetails, BaseEntity projectBe, String mimeType) {	
		
		AmazonS3 s3Client = getAmazonClientObj(projectBe);
	
		String bucketName = projectBe.getValue("PRI_AWS_S3_STORAGE_BUCKET_NAME", null);
		PutObjectRequest request = null;
		ObjectMetadata metadata = new ObjectMetadata();
		
		if(bucketName != null && s3Client != null) {
			try {

	            // Upload a text string as a new object.
				s3Client.putObject(bucketName, fileUploadDetails.getUploadObjKey(), fileUploadDetails.getUploadObjValue());
				
				/* unique ID for file */
				String fileUUID = UUID.randomUUID().toString();
				log.info("fileUUID ::"+fileUUID);
				
				/* get content from URL and convert to inputstream */
				URL url = new URL(downloadableUrl);
				InputStream is = url.openStream();
				
				/* create a new file */
				File file = new File("/tmp/"+fileUUID);
				
				/* copying contents to file outputstream */
				OutputStream outputStream = new FileOutputStream(file);
				IOUtils.copy(is, outputStream);
				
				log.info("file ::"+file.getName());
				log.info("file path ::"+file.getAbsolutePath());
					
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
	            PutObjectResult result = s3Client.putObject(request);
	            
	            String uploadedUrl = String.valueOf(s3Client.getUrl(bucketName, file.getName()));
	                        
	            log.info("uploaded url ::"+uploadedUrl);
	            fileUploadDetails.setUploadedFilePath(uploadedUrl);
	            
				is.close();
				outputStream.close();
	            
	        }
	        catch(AmazonServiceException e) {
	            // The call was transmitted successfully, but Amazon S3 couldn't process 
	            // it, so it returned an error response.
	            e.printStackTrace();
	        }
	        catch(SdkClientException e) {
	            // Amazon S3 couldn't be contacted for a response, or the client
	            // couldn't parse the response from Amazon S3.
	            e.printStackTrace();
	        } catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		log.info("fileUploadDetails ::"+fileUploadDetails);
		return fileUploadDetails;

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

}
