package life.genny.qwandautils;

import java.io.IOException;

import life.genny.qwandautils.PaymentUtils;

public class PaymentEndpoint {

	public static String createAssemblyUser(final String entityString, final String authToken) {

		String paymentServiceUrl = System.getenv("PAYMENT_SERVICE_API_URL");
		String paymentProvider = System.getenv("PAYMENT_PROVIDER");
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

		String paymentServiceUrl = System.getenv("PAYMENT_SERVICE_API_URL");
		String paymentProvider = System.getenv("PAYMENT_PROVIDER");
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

		String paymentServiceUrl = System.getenv("PAYMENT_SERVICE_API_URL");
		String paymentProvider = System.getenv("PAYMENT_PROVIDER");
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

}
