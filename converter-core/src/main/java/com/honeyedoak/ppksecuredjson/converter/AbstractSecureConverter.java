package com.honeyedoak.ppksecuredjson.converter;


import com.honeyedoak.ppksecuredjson.converter.model.SecuredJson;
import com.honeyedoak.ppksecuredjson.converter.model.UnsecuredJson;
import com.honeyedoak.ppksecuredjson.crypto.AsymmetricCryptoService;
import com.honeyedoak.ppksecuredjson.crypto.SymmetricCryptoService;
import com.honeyedoak.ppksecuredjson.crypto.exception.CryptoException;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

public abstract class AbstractSecureConverter implements SecureJsonConverter {

	private final Charset CHARSET;
	private final int oneTimePasswordLength;

	private final PrivateKey secureWsPrivateKey;

	@Getter
	private final AsymmetricCryptoService asymmetricCryptoService;
	@Getter
	private final SymmetricCryptoService symmetricCryptoService;
	private final String base64encodedSecureWsPublicKey;

	public AbstractSecureConverter(String charset, int oneTimePasswordLength, String keystoreLocation, String keystorePassword, SymmetricCryptoService symmetricCryptoService, AsymmetricCryptoService asymmetricCryptoService) {
		this.oneTimePasswordLength = oneTimePasswordLength;
		this.asymmetricCryptoService = asymmetricCryptoService;
		this.symmetricCryptoService = symmetricCryptoService;

		try (InputStream is = new FileInputStream(keystoreLocation)) {

			this.CHARSET = Charset.forName(charset);

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, keystorePassword.toCharArray());

			String alias = "secureJson";

			Key key = keystore.getKey(alias, keystorePassword.toCharArray());
			if (key instanceof PrivateKey) {
				this.secureWsPrivateKey = (PrivateKey) key;
				PublicKey secureWsPublicKey = keystore.getCertificate(alias).getPublicKey();
				this.base64encodedSecureWsPublicKey = Base64.getEncoder().encodeToString(secureWsPublicKey.getEncoded());
			} else {
				throw new CryptoException("secureJson key could not be read from KeyStore");
			}
		} catch (NoSuchAlgorithmException | IOException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
			throw new CryptoException(e);
		}
	}

	@Override
	public final SecuredJson secureJson(String plainJson, Key key) throws CryptoException {
		String oneTimePassword = RandomStringUtils.randomAscii(oneTimePasswordLength);
		byte[] encryptedJson = symmetricCryptoService.encrypt(plainJson.getBytes(CHARSET), oneTimePassword);
		byte[] encryptedOneTimePassword = asymmetricCryptoService.encrypt(oneTimePassword.getBytes(CHARSET), key);

		String base64EncryptedJson = Base64.getEncoder().encodeToString(encryptedJson);
		String base64EncryptedOneTimePassword = Base64.getEncoder().encodeToString(encryptedOneTimePassword);
		return new SecuredJson(base64EncryptedJson, base64EncryptedOneTimePassword, base64encodedSecureWsPublicKey);
	}

	@Override
	public final UnsecuredJson<String> unsecureJson(SecuredJson securedJson) throws CryptoException {
		String decodedOneTimePassword = new String(asymmetricCryptoService.decrypt(Base64.getDecoder().decode(securedJson.getBase64EncryptedOneTimePassword()), secureWsPrivateKey), CHARSET);
		PublicKey clientKey = asymmetricCryptoService.decodePublicKey(Base64.getDecoder().decode(securedJson.getBase64encodedResponsePublicKey()));
		String json = new String(symmetricCryptoService.decrypt(Base64.getDecoder().decode(securedJson.getBase64EncryptedJson()), decodedOneTimePassword), CHARSET);
		return new UnsecuredJson<>(json, clientKey);

	}
}
