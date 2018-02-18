package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;

import life.genny.qwanda.exception.PaymentException;
import life.genny.qwandautils.PaymentUtils;

public class PaymentEndpoint {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String paymentServiceUrl = System.getenv("PAYMENT_SERVICE_API_URL");
	public static final String paymentProvider = System.getenv("PAYMENT_PROVIDER");

	public static String createAssemblyUser(final String entityString, final String authToken) throws PaymentException {

		String newpaymentsUserResponse = null;
		try {
			newpaymentsUserResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/users", entityString, authToken);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return newpaymentsUserResponse;
	}

	public static String getAssemblyUserById(final String assemblyUserId, final String authToken) throws PaymentException {

		String userResponse = null;
		try {
			userResponse = PaymentUtils.apiGetPaymentResponse(
					paymentServiceUrl + "/" + paymentProvider + "/users/" + assemblyUserId, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return userResponse;
	}

	public static String updateAssemblyUser(final String assemblyUserId, final String entityString,
			final String authToken) throws PaymentException {

		String editPaymentResponse = null;
		try {
			editPaymentResponse = PaymentUtils.apiPutPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/users/" + assemblyUserId, entityString, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return editPaymentResponse;
	}

	public static String createCompany(String companyEntityString, String authToken) throws PaymentException {

		String createCompanyResponse = null;

		try {
			createCompanyResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/companies", companyEntityString, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return createCompanyResponse;
	}

	public static String updateCompany(String companyId, String companyEntityString, String authToken) throws PaymentException {

		String updateCompanyResponse = null;

		try {
			updateCompanyResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/companies/" + companyId, companyEntityString, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return updateCompanyResponse;

	}

	public static String createItem(String itemEntity, String authToken) throws PaymentException {
	
		String createItemResponse = null;
		
		try {
			createItemResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/items",  itemEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return createItemResponse;
	}
	
	
	public static String authenticatePaymentProvider(String paymentProviderEntity, String authToken) throws PaymentException {
		String authenticateResponse = null;
		
		try {
			authenticateResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/tokens",  paymentProviderEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return authenticateResponse;
		
	}
	
	public static String createFees(String feeEntity, String authToken) throws PaymentException {
		String feeResponse = null;
		
		try {
			feeResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/fees",  feeEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return feeResponse;
	}
	
	
	public static String makePayment(String paymentItemId, String paymentEntity, String authToken) throws PaymentException {
		String makePaymentResponse = null;
		
		try {
			makePaymentResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/items/" + paymentItemId + "/payment",  paymentEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return makePaymentResponse;
	}
	
	
	public static String releasePayment(String paymentItemId, String authToken) throws PaymentException {
		String releasePaymentResponse = null;
		
		try {
			releasePaymentResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/items/" + paymentItemId + "/release-payment", authToken);
		} catch (IOException e) {
			e.printStackTrace();
		} 
				
		return releasePaymentResponse;
	}
	
	
	public static String disburseAccount(String assemblyUserId, String disburseEntity, String authToken) throws PaymentException {
		
		String disbursementResponse = null;
		try {
			disbursementResponse = PaymentUtils.apiPutPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/users/" + assemblyUserId + "/disbursement-account", disburseEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return disbursementResponse;
		
	}
	
	public static String searchUser(String emailId, String authToken) throws PaymentException {
		
		String searchUserResponse = null;
		
		try {
			searchUserResponse = PaymentUtils.apiGetPaymentResponse(
					paymentServiceUrl + "/" + paymentProvider + "/users?search=" + emailId + "&limit=500", authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return searchUserResponse;
		
	}
	
	public static String getdebitAuthorization(String debitEntity, String authToken) throws PaymentException {
		
		String searchUserResponse = null;
		
		try {
			searchUserResponse = PaymentUtils.apiPostPaymentEntity(paymentServiceUrl + "/" + paymentProvider + "/payment-authority", debitEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return searchUserResponse;
		
	}

}
