package com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.convertors;

import com.cloudbees.jenkins.plugins.kubernetes_credentials_provider.CredentialsConvertionException;
import com.cloudbees.plugins.credentials.CredentialsScope;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests {@link BitbucketTokenCredentialsConverter}
 */
public class BitbucketTokenCredentialsConverterTest {

    @Test
    public void canConvert() throws Exception {
        StringCredentialConvertor convertor = new StringCredentialConvertor();
        assertThat("correct registration of valid type", convertor.canConvert("secretText"), is(true));
        assertThat("incorrect type is rejected", convertor.canConvert("something"), is(false));
    }

    @Test
    public void canConvertAValidSecret() throws Exception {
        StringCredentialConvertor convertor = new StringCredentialConvertor();

        try (InputStream is = get("valid.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());
            StringCredentialsImpl credential = convertor.convert(secret);
            assertThat(credential, notNullValue());
            assertThat("credential id is mapped correctly", credential.getId(), is("a-test-secret"));
            assertThat("credential description is mapped correctly", credential.getDescription(), is("secret text credential from Kubernetes"));
            assertThat("credential scope is mapped correctly", credential.getScope(), is(CredentialsScope.GLOBAL));
            assertThat("credential text is mapped correctly", credential.getSecret().getPlainText(), is("mySecret!"));
        }
    }

    @Test
    public void canConvertAValidMappedSecret() throws Exception {
        StringCredentialConvertor convertor = new StringCredentialConvertor();

        try (InputStream is = get("valid.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());
            StringCredentialsImpl credential = convertor.convert(secret);
            assertThat(credential, notNullValue());
            assertThat("credential id is mapped correctly", credential.getId(), is("a-test-secret"));
            assertThat("credential description is mapped correctly", credential.getDescription(), is("secret text credential from Kubernetes"));
            assertThat("credential scope is mapped correctly", credential.getScope(), is(CredentialsScope.GLOBAL));
            assertThat("credential text is mapped correctly", credential.getSecret().getPlainText(), is("mySecret!"));
        }
    }

    @Issue("JENKINS-53105")
    @Test
    public void canConvertAValidScopedSecret() throws Exception {
        StringCredentialConvertor convertor = new StringCredentialConvertor();

        try (InputStream is = get("validScoped.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            assertThat("The Secret was loaded correctly from disk", notNullValue());
            StringCredentialsImpl credential = convertor.convert(secret);
            assertThat(credential, notNullValue());
            assertThat("credential id is mapped correctly", credential.getId(), is("a-test-secret"));
            assertThat("credential description is mapped correctly", credential.getDescription(), is("secret text credential from Kubernetes"));
            assertThat("credential scope is mapped correctly", credential.getScope(), is(CredentialsScope.SYSTEM));
            assertThat("credential text is mapped correctly", credential.getSecret().getPlainText(), is("mySecret!"));
        }
    }

    @Test
    public void failsToConvertWhenTextMissing() throws Exception {
        StringCredentialConvertor convertor = new StringCredentialConvertor();

        try (InputStream is = get("missingText.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("missing the text"));
        }
    }

    @Test
    public void failsToConvertWhenUsernameCorrupt() throws Exception {
        StringCredentialConvertor convertor = new StringCredentialConvertor();

        try (InputStream is = get("corruptText.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("invalid text"));
        }
    }

    @Test
    public void failsToConvertWhenDataEmpty() throws Exception {
        StringCredentialConvertor convertor = new StringCredentialConvertor();

        try (InputStream is = get("void.yaml")) {
            Secret secret = Serialization.unmarshal(is, Secret.class);
            convertor.convert(secret);
            fail("Exception should have been thrown");
        } catch (CredentialsConvertionException cex) {
            assertThat(cex.getMessage(), containsString("contains no data"));
        }
    }

    private static final InputStream get(String resource) {
        InputStream is = BitbucketTokenCredentialsConverterTest.class.getResourceAsStream("BitbucketTokenCredentialsConverterTest/" + resource);
        if (is == null) {
            fail("failed to load resource " + resource);
        }
        return is;
    }
}
