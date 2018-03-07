package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import life.genny.qwanda.exception.PaymentException;
import life.genny.qwandautils.PaymentUtils;

public class PaymentEndpoint {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String paymentServiceUrl = System.getenv("PAYMENT_SERVICE_API_URL");
	
	private static String getPaymentProvider(String authToken) {

		JSONObject authObj = PaymentUtils.base64Decoder(authToken);
		String provider = null;

		if (authObj.get("provider") != null) {
			provider = authObj.get("provider").toString();
		} else {
			try {
				throw new PaymentException("Assembly Provider is null, causing Payments API call failure");
			} catch (PaymentException e) {
				log.error("Assembly Provider is null, causing Payments API call failure");
			}
		}
		return provider;
	}

	public static String createAssemblyUser(final String entityString, final String authToken) throws PaymentException {

		String newpaymentsUserResponse = null;
		try {
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request entity ::"+entityString);
				newpaymentsUserResponse = PaymentUtils.apiPostPaymentEntity(
						paymentServiceUrl + "/" + provider + "/users", entityString, authToken);
			} 

		} catch (IOException e) {
			e.printStackTrace();
		}

		return newpaymentsUserResponse;
	}

	public static String getAssemblyUserById(final String assemblyUserId, final String authToken) throws PaymentException {

		String userResponse = null;
		try {
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				userResponse = PaymentUtils.apiGetPaymentResponse(
						paymentServiceUrl + "/" + provider + "/users/" + assemblyUserId, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return userResponse;
	}

	public static String updateAssemblyUser(final String assemblyUserId, final String entityString,
			final String authToken) throws PaymentException {

		String editPaymentResponse = null;
		try {
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+entityString);
				editPaymentResponse = PaymentUtils.apiPutPaymentEntity(
						paymentServiceUrl + "/" + provider + "/users/" + assemblyUserId, entityString, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return editPaymentResponse;
	}

	public static String createCompany(String companyEntityString, String authToken) throws PaymentException {

		String createCompanyResponse = null;

		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+companyEntityString);
				createCompanyResponse = PaymentUtils.apiPostPaymentEntity(
						paymentServiceUrl + "/" + provider + "/companies", companyEntityString, authToken);
			} 
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return createCompanyResponse;
	}

	public static String updateCompany(String companyId, String companyEntityString, String authToken) throws PaymentException {

		String updateCompanyResponse = null;

		try {
			String provider = getPaymentProvider(authToken);
			System.out.println("Request Entity ::"+companyEntityString);
			updateCompanyResponse = PaymentUtils.apiPutPaymentEntity(
					paymentServiceUrl + "/" + provider + "/companies/" + companyId, companyEntityString, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return updateCompanyResponse;

	}


	public static String createItem(String itemEntity, String authToken) throws PaymentException {
	
		String createItemResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+itemEntity);
				createItemResponse = PaymentUtils.apiPostPaymentEntity(
						paymentServiceUrl + "/" + provider + "/items",  itemEntity, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return createItemResponse;
	}
	
	
	public static String authenticatePaymentProvider(String paymentProviderEntity, String authToken) throws PaymentException {
		String authenticateResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+paymentProviderEntity);
				authenticateResponse = PaymentUtils.apiPostPaymentEntity(
						paymentServiceUrl + "/" + provider + "/tokens",  paymentProviderEntity, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return authenticateResponse;
		
	}
	
	public static String createFees(String feeEntity, String authToken) throws PaymentException {
		String feeResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+feeEntity);
				feeResponse = PaymentUtils.apiPostPaymentEntity(
						paymentServiceUrl + "/" + provider + "/fees",  feeEntity, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return feeResponse;
	}
	
	
	public static String makePayment(String paymentItemId, String paymentEntity, String authToken) throws PaymentException {
		String makePaymentResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+paymentEntity);
				makePaymentResponse = PaymentUtils.apiPostPaymentEntity(
						paymentServiceUrl + "/" + provider + "/items/" + paymentItemId + "/payment",  paymentEntity, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return makePaymentResponse;
	}
	
	
	public static String releasePayment(String paymentItemId, String authToken) throws PaymentException {
		String releasePaymentResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				releasePaymentResponse = PaymentUtils.apiPostPaymentEntity(
						paymentServiceUrl + "/" + provider + "/items/" + paymentItemId + "/release-payment", authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
				
		return releasePaymentResponse;
	}
	
	
	public static String disburseAccount(String assemblyUserId, String disburseEntity, String authToken) throws PaymentException {
		
		String disbursementResponse = null;
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+disburseEntity);
				disbursementResponse = PaymentUtils.apiPutPaymentEntity(
						paymentServiceUrl + "/" + provider + "/users/" + assemblyUserId + "/disbursement-account", disburseEntity, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return disbursementResponse;
		
	}
	
	public static String searchUser(String emailId, String authToken) throws PaymentException {
		
		String searchUserResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				searchUserResponse = PaymentUtils.apiGetPaymentResponse(
						paymentServiceUrl + "/" + provider + "/users?search=" + emailId + "&limit=500", authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return searchUserResponse;
		
	}
	
	public static String searchUserWithPhoneNumber(String phoneNumber, String authToken) throws PaymentException {
		
		String searchUserResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				searchUserResponse = PaymentUtils.apiGetPaymentResponse(
						paymentServiceUrl + "/" + provider + "/users?search=" + phoneNumber + "&limit=500", authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return searchUserResponse;
		
	}
	
	public static String getdebitAuthorization(String debitEntity, String authToken) throws PaymentException {
		
		String searchUserResponse = null;
		
		try {
			
			String provider = getPaymentProvider(authToken);
			if(provider != null) {
				System.out.println("Request Entity ::"+debitEntity);
				searchUserResponse = PaymentUtils.apiPostPaymentEntity(paymentServiceUrl + "/" + provider + "/payment-authority", debitEntity, authToken);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return searchUserResponse;
		
	}
	
	public static String deleteBankAccount(String bankAccountId, String authToken) throws PaymentException {
		
		String deleteAccountResponse = null;
		
		try {
			String provider = getPaymentProvider(authToken);
			deleteAccountResponse = PaymentUtils.apiDeletePaymentEntity(paymentServiceUrl + "/" + provider + "/bank-accounts/" + bankAccountId, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return deleteAccountResponse;
		
	}
	
	public static String deleteCardAccount(String cardAccountId, String authToken) throws PaymentException {
		
		String deleteAccountResponse = null;
		
		try {
			String provider = getPaymentProvider(authToken);
			deleteAccountResponse = PaymentUtils.apiDeletePaymentEntity(paymentServiceUrl + "/" + provider + "/card-accounts/" + cardAccountId, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return deleteAccountResponse;
		
	}

}
