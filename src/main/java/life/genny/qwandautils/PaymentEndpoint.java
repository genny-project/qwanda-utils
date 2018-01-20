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
			System.out.println("create users - payments response ::" + newpaymentsUserResponse);

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
			System.out.println("get a particular user detail ::" + userResponse);
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
			System.out.println("Response after editing a user ::" + editPaymentResponse);
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
			System.out.println("Response after creating a new company ::" + createCompanyResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return createCompanyResponse;
	}

	public static String updateCompany(String companyId, String companyEntityString, String authToken) {

		String updateCompanyResponse = null;

		try {
			updateCompanyResponse = PaymentUtils.apiPostPaymentEntity(
					paymentServiceUrl + "/" + paymentProvider + "/users/" + companyId, companyEntityString, authToken);
			System.out.println("Response after editing a user ::" + updateCompanyResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return updateCompanyResponse;

	}

	public static String createItem(String authToken) {
	
		String createCompanyResponse = null;
		return createCompanyResponse;
	}

}
