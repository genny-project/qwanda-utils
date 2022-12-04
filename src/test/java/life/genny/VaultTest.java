package life.genny;

import com.bettercloud.vault.VaultException;
import life.genny.qwandautils.VaultAuthParams;
import life.genny.qwandautils.VaultAuthType;
import org.junit.Test;
import life.genny.qwandautils.VaultUtils;

import static junit.framework.TestCase.assertNotNull;

public class VaultTest {
    @Test
    public void testRootTokenAuth() throws VaultException {
        String vaultUrl = System.getenv("VAULT_URL");

        final String profile = System.getenv("VAULT_PROFILE");
        final String rootToken = System.getenv("VAULT_ROOT_TOKEN");

        VaultAuthParams params = VaultAuthParams.newBuilder()
                .withRootToken(rootToken)
                .build();

        VaultUtils.fetchAll(profile, vaultUrl, VaultAuthType.RootToken, params);
        String value = VaultUtils.readValue("ENV_SERVICE_PASSWORD");
        System.out.println("RootToken:" + value);
        assertNotNull(value);
    }

    @Test
    public void testAppRoleAuth() throws VaultException {
        String vaultUrl = System.getenv("VAULT_URL");

        final String profile = System.getenv("VAULT_PROFILE");
        final String appRoleId = System.getenv("VAULT_APP_ROLE_ID");
        final String secretId = System.getenv("VAULT_APP_SECRET_ID");

        VaultAuthParams params = VaultAuthParams.newBuilder()
                .withAppRole(appRoleId, secretId)
                .build();

        VaultUtils.fetchAll(profile, vaultUrl, VaultAuthType.AppRole, params);
        String value = VaultUtils.readValue("ENV_SERVICE_PASSWORD");
        System.out.println("AppRole:" + value);
        assertNotNull(value);
    }

    @Test
    public void testGithubAuth() throws VaultException {
        String vaultUrl = System.getenv("VAULT_URL");

        final String profile = System.getenv("VAULT_PROFILE");
        final String githubToken = System.getenv("VAULT_GITHUB_TOKEN");

        VaultAuthParams params = VaultAuthParams.newBuilder()
                .withGithubToken(githubToken)
                .build();

        VaultUtils.fetchAll(profile, vaultUrl, VaultAuthType.GitHubAuth, params);
        String value = VaultUtils.readValue("ENV_SERVICE_PASSWORD");
        System.out.println("GitHub:" + value);
        assertNotNull(value);
    }

}
