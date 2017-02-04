package name.neuhalfen.projects.crypto.bouncycastle.openpgp;

import name.neuhalfen.projects.crypto.bouncycastle.openpgp.decrypting.DecryptionConfig;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.testtooling.Configs;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.testtooling.ExampleMessages;
import org.bouncycastle.util.io.Streams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.Mockito.mock;

public class BuildDecryptionInputStreamAPITest {

    @Test(expected = IllegalArgumentException.class)
    public void decryptConfigure_NoConfigPassed_throws() throws Exception {
        BouncyGPG.decrypt().withConfig(null);
    }

    @Test
    public void decryptConfigure_ConfigPassed_notNull() throws Exception {
        assertNotNull(BouncyGPG.decrypt().withConfig(mock(DecryptionConfig.class)));
    }

    @Test
    public void decryptConfigureValidate_notNull() throws Exception {
        final BuildDecryptionInputStreamAPI.Validation withConfig = BouncyGPG.decrypt().withConfig(mock(DecryptionConfig.class));
        assumeNotNull(withConfig);

        assertNotNull(withConfig.andValidateSomeoneSigned());
        assertNotNull(withConfig.andIgnoreSignatures());
        assertNotNull(withConfig.andRequireSignatureFromAllKeys(1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decryptConfigureValidate_pasNullCiphertext_throws() throws Exception {
        final BuildDecryptionInputStreamAPI.Build build = BouncyGPG.decrypt().withConfig(mock(DecryptionConfig.class)).andIgnoreSignatures();
        build.fromEncryptedInputStream(null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void decryptValidateSpecificKeysLong_passNoKeys_throws() throws Exception {
        final BuildDecryptionInputStreamAPI.Validation validation = BouncyGPG.decrypt().withConfig(mock(DecryptionConfig.class));
        assumeNotNull(validation);

        validation.andRequireSignatureFromAllKeys(new Long[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void decryptValidateSpecificKeysUserId_passNoKeys2_throws() throws Exception {
        final BuildDecryptionInputStreamAPI.Validation validation = BouncyGPG.decrypt().withConfig(mock(DecryptionConfig.class));
        assumeNotNull(validation);

        validation.andRequireSignatureFromAllKeys(new String[]{});
    }

    @Test()
    public void decryptAndValidateSignature_withGoodSettings_works() throws Exception {

        try (InputStream ciphertext = new ByteArrayInputStream(ExampleMessages.IMPORTANT_QUOTE_SIGNED_COMPRESSED.getBytes("US-ASCII"))) {
            final InputStream plaintextStream = BouncyGPG.decrypt()
                    .withConfig(Configs.buildConfigForDecryptionFromResources())
                    .andRequireSignatureFromAllKeys("sender@example.com")
                    .fromEncryptedInputStream(ciphertext);

            final String plainText = inputStreamToText(plaintextStream);

            assertThat(plainText, equalTo(ExampleMessages.IMPORTANT_QUOTE_TEXT));
            plaintextStream.close();
        }
    }


    @Test()
    public void decryptNoSignatureValidation_withUnsignedData_works() throws Exception {

        try (InputStream ciphertext = new ByteArrayInputStream(ExampleMessages.IMPORTANT_QUOTE_NOT_SIGNED_NOT_COMPRESSED.getBytes("US-ASCII"))) {
            final InputStream plaintextStream = BouncyGPG.decrypt()
                    .withConfig(Configs.buildConfigForDecryptionFromResources())
                    .andIgnoreSignatures()
                    .fromEncryptedInputStream(ciphertext);

            final String plainText = inputStreamToText(plaintextStream);

            assertThat(plainText, equalTo(ExampleMessages.IMPORTANT_QUOTE_TEXT));
            plaintextStream.close();
        }
    }

    @Test(expected = IOException.class)
    public void decryptAndValidateSignature_withUnsignedData_throws() throws Exception {

        try (InputStream ciphertext = new ByteArrayInputStream(ExampleMessages.IMPORTANT_QUOTE_NOT_SIGNED_NOT_COMPRESSED.getBytes("US-ASCII"))) {
            final InputStream plaintextStream = BouncyGPG.decrypt()
                    .withConfig(Configs.buildConfigForDecryptionFromResources())
                    .andRequireSignatureFromAllKeys("sender@example.com")
                    .fromEncryptedInputStream(ciphertext);

            Streams.drain(plaintextStream);
        }
    }

    private String inputStreamToText(InputStream in) throws IOException {
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        Streams.pipeAll(in, res);
        res.close();
        return res.toString();
    }
}