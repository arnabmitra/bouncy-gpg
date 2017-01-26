package name.neuhalfen.projects.crypto.bouncycastle.examples.openpgp.encrypting;

import name.neuhalfen.projects.crypto.bouncycastle.examples.openpgp.testtooling.Configs;
import name.neuhalfen.projects.crypto.bouncycastle.examples.openpgp.testtooling.DevNullOutputStream;
import name.neuhalfen.projects.crypto.bouncycastle.examples.openpgp.testtooling.RandomDataInputStream;
import org.bouncycastle.crypto.tls.HashAlgorithm;
import org.hamcrest.text.IsEmptyString;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


public class EncryptionConfigTest {
    @Test
    public void toString_returns_nonEmptyString() throws Exception {
        final EncryptionConfig encryptionConfig = Configs.buildConfigForEncryptionFromResources();
        assertThat(encryptionConfig.toString(), is(not(IsEmptyString.isEmptyString())));
    }

    @Test
    public void loadFromFiles_works() throws IOException {
        final EncryptionConfig encryptionConfig = Configs.buildConfigForEncryptionFromFiles();

        assertThat(encryptionConfig.getEncryptionPublicKeyId(), is(not(isEmptyOrNullString())));
        assertThat(encryptionConfig.getPgpHashAlgorithmCode(), is(not(equalTo((int) HashAlgorithm.none))));

        assertThat(encryptionConfig.getPublicKeyRing(), is(notNullValue()));
        assertThat(encryptionConfig.getSecretKeyRing(), is(notNullValue()));
    }

    public static class EncryptWithOpenPGPTest {


        @Test
        public void encryptionAndSigning_anyData_doesNotCloseInputStream() throws IOException, SignatureException, NoSuchAlgorithmException {

            StreamEncryption sut = new EncryptWithOpenPGP(Configs.buildConfigForEncryptionFromResources());


            InputStream in = mock(InputStream.class);
            when(in.read()).thenReturn(-1);
            when(in.available()).thenReturn(0);
            when(in.read(ArgumentMatchers.any(byte[].class))).thenReturn(-1);
            when(in.read(ArgumentMatchers.any(byte[].class), ArgumentMatchers.any(int.class), ArgumentMatchers.any(int.class))).thenReturn(-1);
            when(in.read()).thenReturn(-1);

            sut.encryptAndSign(in, mock(OutputStream.class));

            verify(in, never()).close();
        }


        @Test
        public void encryptionAndSigning_anyData_doesNotCloseOutputStream() throws IOException, SignatureException, NoSuchAlgorithmException {

            StreamEncryption sut = new EncryptWithOpenPGP(Configs.buildConfigForEncryptionFromResources());

            InputStream in = mock(InputStream.class);
            when(in.read()).thenReturn(-1);
            when(in.available()).thenReturn(0);
            when(in.read(ArgumentMatchers.any(byte[].class))).thenReturn(-1);
            when(in.read(ArgumentMatchers.any(byte[].class), ArgumentMatchers.any(int.class), ArgumentMatchers.any(int.class))).thenReturn(-1);
            when(in.read()).thenReturn(-1);

            OutputStream os = mock(OutputStream.class);

            sut.encryptAndSign(in, os);

            verify(os, never()).close();
        }

        @Test
        public void encryptionAndSigning_smallAmountsOfData_doesNotCrash() throws IOException, SignatureException, NoSuchAlgorithmException {

            StreamEncryption sut = new EncryptWithOpenPGP(Configs.buildConfigForEncryptionFromResources());

            DevNullOutputStream out = new DevNullOutputStream();

            final int sampleSize = 1 * Configs.KB;
            sut.encryptAndSign(someRandomInputData(sampleSize), out);

            assertThat("A compression>50% is fishy!", out.getBytesWritten(), greaterThan(sampleSize / 2));
        }

        /**
         * This is really a "does not crash for moderate amounts of data" test.
         */
        @Test
        @Ignore("this test is  slow (~2sec)")
        public void encryptionAndSigning_10MB_isFast() throws IOException, SignatureException, NoSuchAlgorithmException {
            StreamEncryption sut = new EncryptWithOpenPGP(Configs.buildConfigForEncryptionFromResources());

            DevNullOutputStream out = new DevNullOutputStream();

            final int sampleSize = 10 * Configs.MB;
            sut.encryptAndSign(someRandomInputData(sampleSize), out);

            assertThat("A compression>50% is fishy!", out.getBytesWritten(), greaterThan(sampleSize / 2));
        }


        @Test
        @Ignore("this test is very slow (~2min)")
        public void encryptionAndSigning_1GB_doesNotCrash() throws IOException, SignatureException, NoSuchAlgorithmException {
            StreamEncryption sut = new EncryptWithOpenPGP(Configs.buildConfigForEncryptionFromResources());

            DevNullOutputStream out = new DevNullOutputStream();

            final int sampleSize = 1 * Configs.GB;
            sut.encryptAndSign(someRandomInputData(sampleSize), out);


            assertThat("A compression>50% is fishy!", out.getBytesWritten(), greaterThan(sampleSize / 2));
        }


        private InputStream someRandomInputData(int len) {
            return new RandomDataInputStream(len);
        }

    }
}