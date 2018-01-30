package life.genny.qwandautils;

import java.io.IOException;

import life.genny.qwandautils.PaymentUtils;

public class PaymentEndpoint {

	public static final String paymentServiceUrl = System.getenv("PAYMENT_SERVICE_API_URL");
	public static final String paymentProvider = System.getenv("PAYMENT_PROVIDER");

	public static String createAssemblyUser(final String entityString, final String authToken) {

		String newpaymentsUserResponse = null;
		try {
			newpaymentsUserResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/users", entityString, authToken);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return newpaymentsUserResponse;
	}

	public static String getAssemblyUserById(final String assemblyUserId, final String authToken) {

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
			final String authToken) {

		String editPaymentResponse = null;
		try {
			editPaymentResponse = PaymentUtils.apiPutPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/users/" + assemblyUserId, entityString, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return editPaymentResponse;
	}

	public static String createCompany(String companyEntityString, String authToken) {

		String createCompanyResponse = null;

		try {
			createCompanyResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/companies", companyEntityString, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return createCompanyResponse;
	}

	public static String updateCompany(String companyId, String companyEntityString, String authToken) {

		String updateCompanyResponse = null;

		try {
			updateCompanyResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/companies/" + companyId, companyEntityString, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return updateCompanyResponse;

	}

	public static String createItem(String itemEntity, String authToken) {
	
		String createCompanyResponse = null;
		
		try {
			createCompanyResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/items",  itemEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return createCompanyResponse;
	}
	
	
	public static String authenticatePaymentProvider(String paymentProviderEntity, String authToken) {
		String authenticateResponse = null;
		
		try {
			authenticateResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/tokens",  paymentProviderEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return authenticateResponse;
		
	}
	
	public static String createFees(String feeEntity, String authToken) {
		String feeResponse = null;
		
		try {
			feeResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/fees",  feeEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return feeResponse;
	}
	
	
	public static String makePayment(String paymentItemId, String paymentEntity, String authToken) {
		String makePaymentResponse = null;
		
		try {
			makePaymentResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/items/" + paymentItemId + "/payment",  paymentEntity, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return makePaymentResponse;
	}
	
	
	public static String releasePayment(String paymentItemId, String authToken) {
		String releasePaymentResponse = null;
		
		try {
			releasePaymentResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/items/" + paymentItemId + "/release-payment", null, authToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return releasePaymentResponse;
	}

}
