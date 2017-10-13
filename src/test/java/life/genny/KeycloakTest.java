package life.genny;

import org.junit.Test;
// import org.junit.Test;
// import org.keycloak.representations.AccessTokenResponse;
import life.genny.qwandautils.KeycloakUtils;

public class KeycloakTest {

  // @Test
  // // run the keycloak docker located in the root of the project runKeycloak.sh
  // public void tokenTest() {
  // try {
  // AccessTokenResponse accessToken = KeycloakUtils.getAccessToken("http://localhost:8180",
  // "wildfly-swarm-keycloak-example", "curl", "056b73c1-7078-411d-80ec-87d41c55c3b4", "user1",
  // "password1");
  // String tokenString = accessToken.getToken();
  // System.out.println(tokenString);
  //
  // } catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  //
  // }

  @Test
  public void decodeTokenTest() {
    final String token =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrcE9kQzY3NGdRRUJJd2hQUVRaemJsbTRXN3N4ZG85NnVaTDNEUkFPczdvIn0.eyJqdGkiOiI0MjVlMjM4ZS0wOGFhLTQzNGUtOWYzNC1kOTI2MDI2NzgzYzUiLCJleHAiOjE1MDc4NzM5MTAsIm5iZiI6MCwiaWF0IjoxNTA3ODczNjEwLCJpc3MiOiJodHRwOi8vMTAuMS4xLjczOjgxODAvYXV0aC9yZWFsbXMvd2lsZGZseS1zd2FybS1rZXljbG9hay1leGFtcGxlIiwiYXVkIjoiY3VybCIsInN1YiI6IjZlYTcwNWEzLWY1MjMtNDVhNC1hY2EzLWRjMjJlNmMyNGY0ZiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImN1cmwiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI5M2JlNWVlNy1kNjZjLTQ5NzEtYTM5ZC04MTdjNWNlMDUwNTIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly8xOTIuMTY4LjY0Ljc6ODA4OC8qIiwiaHR0cDovL2xvY2FsaG9zdDo4MjgwIiwiaHR0cDovL2xvY2FsaG9zdDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdCIsImh0dHA6Ly8xOTIuMTY4Ljk5LjEwMDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdDo4MDg4IiwiaHR0cDovL2xvY2FsaG9zdDozMDAwIiwiaHR0cDovLzEwLjY0LjAuNjo4MjgwIiwiaHR0cDovLzE5Mi4xNjguOTkuMTAwOjMwMDAiLCJodHRwOi8vbG9jYWxob3N0OjU4MDgwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1bWFfYXV0aG9yaXphdGlvbiIsInVzZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50Iiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6IkphbWVzIEJvbmQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMSIsImdpdmVuX25hbWUiOiJKYW1lcyIsImZhbWlseV9uYW1lIjoiQm9uZCIsImVtYWlsIjoiYWRhbWNyb3c2NEBnbWFpbC5jb20ifQ.QKAOG3EHncby8jufaYmo1hxDWjq-s_yHRpHS2U6ZkLVp4d4kNBDDhIyEFhNyPsqBVZ_nt-w67TbeO6bz2V_C4p7diAGk6jgefsYtqzlGCtHymlq7SJp-sEiPtjrQg84C0Lj3GIqJfnZucGTBUGyKADLE35-w8G7p3YjGz4vI4sLOYFWlX47OmHRjMb4z-Q6dSjAdTjeNmG0a1TK3IPB4onA_LPkuJdXcGwTVvG_iWssf-uL4D_Y78luj7u5Jgs_RDjN-fOhYkbuJH9fuZ6eNK38-69Mqv76HBXdeclCWl2_B2_uiY-f1ukwHZy567v4SBcM_QGHuOVPLVoUee2mm3Q";

    KeycloakUtils.getDecodedToken(token);

    // System.out.println(json);

  }

  @Test
  public void mapTokenTest() {
    final String token =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrcE9kQzY3NGdRRUJJd2hQUVRaemJsbTRXN3N4ZG85NnVaTDNEUkFPczdvIn0.eyJqdGkiOiI0MjVlMjM4ZS0wOGFhLTQzNGUtOWYzNC1kOTI2MDI2NzgzYzUiLCJleHAiOjE1MDc4NzM5MTAsIm5iZiI6MCwiaWF0IjoxNTA3ODczNjEwLCJpc3MiOiJodHRwOi8vMTAuMS4xLjczOjgxODAvYXV0aC9yZWFsbXMvd2lsZGZseS1zd2FybS1rZXljbG9hay1leGFtcGxlIiwiYXVkIjoiY3VybCIsInN1YiI6IjZlYTcwNWEzLWY1MjMtNDVhNC1hY2EzLWRjMjJlNmMyNGY0ZiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImN1cmwiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI5M2JlNWVlNy1kNjZjLTQ5NzEtYTM5ZC04MTdjNWNlMDUwNTIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly8xOTIuMTY4LjY0Ljc6ODA4OC8qIiwiaHR0cDovL2xvY2FsaG9zdDo4MjgwIiwiaHR0cDovL2xvY2FsaG9zdDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdCIsImh0dHA6Ly8xOTIuMTY4Ljk5LjEwMDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdDo4MDg4IiwiaHR0cDovL2xvY2FsaG9zdDozMDAwIiwiaHR0cDovLzEwLjY0LjAuNjo4MjgwIiwiaHR0cDovLzE5Mi4xNjguOTkuMTAwOjMwMDAiLCJodHRwOi8vbG9jYWxob3N0OjU4MDgwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1bWFfYXV0aG9yaXphdGlvbiIsInVzZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50Iiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6IkphbWVzIEJvbmQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMSIsImdpdmVuX25hbWUiOiJKYW1lcyIsImZhbWlseV9uYW1lIjoiQm9uZCIsImVtYWlsIjoiYWRhbWNyb3c2NEBnbWFpbC5jb20ifQ.QKAOG3EHncby8jufaYmo1hxDWjq-s_yHRpHS2U6ZkLVp4d4kNBDDhIyEFhNyPsqBVZ_nt-w67TbeO6bz2V_C4p7diAGk6jgefsYtqzlGCtHymlq7SJp-sEiPtjrQg84C0Lj3GIqJfnZucGTBUGyKADLE35-w8G7p3YjGz4vI4sLOYFWlX47OmHRjMb4z-Q6dSjAdTjeNmG0a1TK3IPB4onA_LPkuJdXcGwTVvG_iWssf-uL4D_Y78luj7u5Jgs_RDjN-fOhYkbuJH9fuZ6eNK38-69Mqv76HBXdeclCWl2_B2_uiY-f1ukwHZy567v4SBcM_QGHuOVPLVoUee2mm3Q";

    KeycloakUtils.getJsonMap(token);

    // System.out.println(jsonMap);

  }

}
