package com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.convertors;

import com.atlassian.bitbucket.jenkins.internal.config.BitbucketTokenCredentialsImpl;
import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.CredentialsConvertionException;
import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.SecretToCredentialConverter;
import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.SecretUtils;
import io.fabric8.kubernetes.api.model.Secret;
import org.jenkinsci.plugins.variant.OptionalExtension;

/**
 * SecretToCredentialConvertor that converts {@link BitbucketTokenCredentialsImpl}.
 */
@OptionalExtension(requirePlugins={"atlassian-bitbucket-server-integration"})
public class BitbucketTokenCredentialsConverter extends SecretToCredentialConverter {
    @Override
    public boolean canConvert(String type) {
        return "bitbucketToken".equals(type);
    }

    @Override
    public BitbucketTokenCredentialsImpl convert(Secret secret) throws CredentialsConvertionException {
        SecretUtils.requireNonNull(secret.getData(), "bitbucketToken definition contains no data");

        String tokenBase64 = SecretUtils.getNonNullSecretData(secret, "token", "bitbucketToken credential is missing the token");

        String token = SecretUtils.requireNonNull(SecretUtils.base64DecodeToString(tokenBase64), "bitbucketToken credential has an invalid token (must be base64 encoded UTF-8)");

        hudson.util.Secret tokenSecret = hudson.util.Secret.fromString(token);

        // Bitbucket token always has "SYSTEM" scope (only configurable by administrators).
        return new BitbucketTokenCredentialsImpl(SecretUtils.getCredentialId(secret), SecretUtils.getCredentialDescription(secret), tokenSecret);
    }
}
