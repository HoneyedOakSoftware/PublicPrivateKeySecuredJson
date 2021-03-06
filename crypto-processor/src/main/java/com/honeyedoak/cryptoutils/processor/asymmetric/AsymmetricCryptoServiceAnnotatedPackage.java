package com.honeyedoak.cryptoutils.processor.asymmetric;

import com.honeyedoak.ppksecuredjson.crypto.annotation.AsymmetricCryptoService;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AsymmetricCryptoServiceAnnotatedPackage {

	private PackageElement annotatedPackageElement;
	private String serviceName;
	private String algorithm;
	private int keySize;

	public AsymmetricCryptoServiceAnnotatedPackage(PackageElement packageElement) throws IllegalArgumentException {
		this.annotatedPackageElement = packageElement;
		AsymmetricCryptoService annotation = packageElement.getAnnotation(AsymmetricCryptoService.class);
		serviceName = annotation.serviceName();
		algorithm = annotation.algorithm();
		keySize = annotation.keySize();

		if (StringUtils.isEmpty(serviceName)) {
			throw new IllegalArgumentException(
					String.format("serviceName() in @%s for class %s is null or empty! that's not allowed",
							AsymmetricCryptoService.class.getSimpleName(), packageElement.getQualifiedName().toString()));
		}

		if (StringUtils.isEmpty(algorithm)) {
			throw new IllegalArgumentException(
					String.format("algorithm() in @%s for class %s is null or empty! that's not allowed",
							AsymmetricCryptoService.class.getSimpleName(), packageElement.getQualifiedName().toString()));
		}

		if (keySize < 1) {
			throw new IllegalArgumentException(
					String.format("keySize() in @%s for class %s is zero or negative! that's not allowed",
							AsymmetricCryptoService.class.getSimpleName(), packageElement.getQualifiedName().toString()));
		}
	}

	public PackageElement getAnnotatedPackageElement() {
		return annotatedPackageElement;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public int getKeySize() {
		return keySize;
	}

	public void generateCode(Filer filer) throws IOException {
		FieldSpec asymmetricCryptoUtils = FieldSpec.builder(com.honeyedoak.ppksecuredjson.crypto.AsymmetricCryptoService.class, "asymmetricCryptoUtils", Modifier.PRIVATE, Modifier.FINAL)
				.build();

		MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addCode("this.asymmetricCryptoUtils = new $T($S, $L);", com.honeyedoak.ppksecuredjson.crypto.AsymmetricCryptoServiceImpl.class, algorithm, keySize)
				.build();

		MethodSpec generateKeyPair = MethodSpec.methodBuilder("generateKeyPair")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addException(com.honeyedoak.ppksecuredjson.crypto.exception.CryptoException.class)
				.returns(KeyPair.class)
				.addCode("return this.asymmetricCryptoUtils.generateKeyPair();")
				.build();

		MethodSpec decodePrivateKey = MethodSpec.methodBuilder("decodePrivateKey")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addException(com.honeyedoak.ppksecuredjson.crypto.exception.CryptoException.class)
				.returns(PrivateKey.class)
				.addParameter(byte[].class, "encodedKey")
				.addCode("return asymmetricCryptoUtils.decodePrivateKey(encodedKey);")
				.build();

		MethodSpec decodePublicKey = MethodSpec.methodBuilder("decodePublicKey")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addException(com.honeyedoak.ppksecuredjson.crypto.exception.CryptoException.class)
				.returns(PublicKey.class)
				.addParameter(byte[].class, "encodedKey")
				.addCode("return asymmetricCryptoUtils.decodePublicKey(encodedKey);")
				.build();

		MethodSpec decrypt = MethodSpec.methodBuilder("decrypt")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addException(com.honeyedoak.ppksecuredjson.crypto.exception.CryptoException.class)
				.returns(byte[].class)
				.addParameter(byte[].class, "payload")
				.addParameter(Key.class, "key")
				.addCode("return asymmetricCryptoUtils.decrypt(payload, key);")
				.build();

		MethodSpec encrypt = MethodSpec.methodBuilder("encrypt")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addException(com.honeyedoak.ppksecuredjson.crypto.exception.CryptoException.class)
				.returns(byte[].class)
				.addParameter(byte[].class, "payload")
				.addParameter(Key.class, "key")
				.addCode("return asymmetricCryptoUtils.encrypt(payload, key);")
				.build();

		AnnotationSpec generated = AnnotationSpec.builder(javax.annotation.Generated.class)
				.addMember("value", "$S", CryptoutilsAsymmetricCryptoServiceProcessor.class.getName())
				.build();

		TypeSpec asymmetricCryptoService = TypeSpec.classBuilder(serviceName)
				.addAnnotation(generated)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(com.honeyedoak.ppksecuredjson.crypto.AsymmetricCryptoService.class)
				.addField(asymmetricCryptoUtils)
				.addAnnotation(org.springframework.stereotype.Service.class)
				.addMethod(constructor)
				.addMethod(generateKeyPair)
				.addMethod(decodePrivateKey)
				.addMethod(decodePublicKey)
				.addMethod(decrypt)
				.addMethod(encrypt)
				.build();

		JavaFile.builder(annotatedPackageElement.getQualifiedName().toString(), asymmetricCryptoService)
				.build()
				.writeTo(filer);
	}
}
