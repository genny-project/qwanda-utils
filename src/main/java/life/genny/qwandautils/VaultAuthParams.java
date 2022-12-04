package life.genny.qwandautils;

public class VaultAuthParams {
    // root token
    private String rootToken;
    // GitHub personal token
    private String githubToken;

    // app role
    private String appRoleId;
    private String appRoleSecretId;

    private VaultAuthParams(Builder builder) {
        rootToken = builder.rootToken;
        githubToken = builder.githubToken;
        appRoleId = builder.appRoleId;
        appRoleSecretId = builder.appRoleSecretId;
    }

    public String getRootToken() {
        return rootToken;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public String getAppRoleId() {
        return appRoleId;
    }

    public String getAppRoleSecretId() {
        return appRoleSecretId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        // root token
        private String rootToken;
        // GitHub personal token
        private String githubToken;

        // app role
        private String appRoleId;
        private String appRoleSecretId;

        private Builder() {
        }

        public Builder withRootToken(String val) {
            rootToken = val;
            return this;
        }

        public Builder withGithubToken(String val) {
            githubToken = val;
            return this;
        }

        public Builder withAppRole(String roleIdVal, String secretIdVal) {
            appRoleId = roleIdVal;
            appRoleSecretId = secretIdVal;
            return this;
        }

        public VaultAuthParams build() {
            return new VaultAuthParams(this);
        }
    }
}
