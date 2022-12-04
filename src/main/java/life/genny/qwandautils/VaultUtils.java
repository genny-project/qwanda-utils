package life.genny.qwandautils;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class VaultUtils {

    protected static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    private VaultUtils() {
    }

    private static final HashMap<String, String> values = new HashMap<>();

    // check Vault server if matching
    private static final String secretPathPrefix = System.getenv("VAULT_SECRET_PREFIX_PATH");
    private static final String[] secrets = {
            "email",
            "google",
            "infinispan",
            "keycloak",
            "minio",
            "mysql"};

    private static Vault getDefaultVault(String vaultUrl) throws VaultException {
        final VaultConfig config = new VaultConfig().address(vaultUrl).build();
        return new Vault(config);
    }

    public static Vault getVaultWithToken(final String vaultUrl, final String token) throws VaultException {
        final VaultConfig config = new VaultConfig().address(vaultUrl).token(token).build();
        return new Vault(config);
    }


    // Login with GitHub personal access token
    public static Vault getVaultFromGithubToken(final String vaultUrl, final String githubToken) throws VaultException {
        final AuthResponse response = getDefaultVault(vaultUrl).auth().loginByGithub(githubToken);
        return getVaultWithToken(vaultUrl, response.getAuthClientToken());
    }

    // Login with AppRole
    public static Vault getVaultFromAppRole(final String vaultUrl, final String roleId, final String secretId) throws VaultException {
        final AuthResponse response = getDefaultVault(vaultUrl).auth().loginByAppRole(roleId, secretId);
        return getVaultWithToken(vaultUrl, response.getAuthClientToken());
    }


    // fetch all from Vault
    /*
    @profile, name of environment, value example: dev, prod
     */
    public static void fetchAll(final String profile,
                                final String vaultUrl,
                                final VaultAuthType authType,
                                final VaultAuthParams params) throws VaultException {
        String path;
        Vault vault = null;
        Map<String, String> tmpValues;
        switch (authType) {
            case RootToken:
                vault = VaultUtils.getVaultWithToken(vaultUrl, params.getRootToken());
                break;
            case GitHubAuth:
                vault = VaultUtils.getVaultFromGithubToken(vaultUrl, params.getGithubToken());
                break;
            case AppRole:
                vault = VaultUtils.getVaultFromAppRole(vaultUrl, params.getAppRoleId(), params.getAppRoleSecretId());
                break;
        }

        if (vault == null) {
            log.error("Check your auth type, it needs to be root token, github, or appRole !!!");
            return;
        }

        for (String secret : secrets) {
            path = String.join("/", secretPathPrefix, profile, secret);
            tmpValues = vault.logical().read(path).getData();

            if (tmpValues != null)
                values.putAll(tmpValues);
        }
    }

    public static String readValue(String envName) {
        return values.get(envName);
    }
}
