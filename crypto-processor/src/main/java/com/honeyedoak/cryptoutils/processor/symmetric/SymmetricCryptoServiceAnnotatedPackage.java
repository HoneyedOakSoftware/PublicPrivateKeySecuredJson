package com.honeyedoak.cryptoutils.processor.symmetric;

import com.honeyedoak.ppksecuredjson.crypto.annotation.SymmetricCryptoService;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import java.io.IOException;

public class SymmetricCryptoServiceAnnotatedPackage {

	private PackageElement annotatedPackageElement;
	private String serviceName;
	private String algorithm;

	public SymmetricCryptoServiceAnnotatedPackage(PackageElement packageElement) throws IllegalArgumentException {
		this.annotatedPackageElement = packageElement;
		SymmetricCryptoService annotation = packageElement.getAnnotation(SymmetricCryptoService.class);
		serviceName = annotation.serviceName();
		algorithm = annotation.algorithm();

		if (StringUtils.isEmpty(serviceName)) {
			throw new IllegalArgumentException(
					String.format("serviceName() in @%s for class %s is null or empty! that's not allowed",
							SymmetricCryptoService.class.getSimpleName(), packageElement.getQualifiedName().toString()));
		}

		if (StringUtils.isEmpty(algorithm)) {
			throw new IllegalArgumentException(
					String.format("algorithm() in @%s for class %s is null or empty! that's not allowed",
							SymmetricCryptoService.class.getSimpleName(), packageElement.getQualifiedName().toString()));
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

	public void generateCode(Filer filer) throws IOException {
		String symmetricCryptoServiceFieldName = "symmetricCryptoService";

		FieldSpec symmetricCryptoUtils = FieldSpec.builder(com.honeyedoak.ppksecuredjson.crypto.SymmetricCryptoService.class, symmetricCryptoServiceFieldName, Modifier.PRIVATE, Modifier.FINAL)
				.build();

		MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addCode("this.$L = new $T($S);", symmetricCryptoServiceFieldName, com.honeyedoak.ppksecuredjson.crypto.SymmetricCryptoServiceImpl.class, algorithm)
				.build();

		MethodSpec decrypt = MethodSpec.methodBuilder("decrypt")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(byte[].class)
				.addParameter(byte[].class, "payload")
				.addParameter(String.class, "password")
				.addCode("return this.$L.decrypt(payload, password);", symmetricCryptoServiceFieldName)
				.build();

		MethodSpec decryptChar = MethodSpec.methodBuilder("decrypt")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(byte[].class)
				.addParameter(byte[].class, "payload")
				.addParameter(char[].class, "password")
				.addCode("return this.$L.decrypt(payload, password);", symmetricCryptoServiceFieldName)
				.build();

		MethodSpec encrypt = MethodSpec.methodBuilder("encrypt")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(byte[].class)
				.addParameter(byte[].class, "payload")
				.addParameter(String.class, "password")
				.addCode("return this.$L.encrypt(payload, password);", symmetricCryptoServiceFieldName)
				.build();

		MethodSpec encryptChar = MethodSpec.methodBuilder("encrypt")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(byte[].class)
				.addParameter(byte[].class, "payload")
				.addParameter(char[].class, "password")
				.addCode("return this.$L.encrypt(payload, password);", symmetricCryptoServiceFieldName)
				.build();

		AnnotationSpec generated = AnnotationSpec.builder(javax.annotation.Generated.class)
				.addMember("value", "$S", com.honeyedoak.cryptoutils.processor.asymmetric.CryptoutilsAsymmetricCryptoServiceProcessor.class.getName())
				.build();

		TypeSpec asymmetricCryptoService = TypeSpec.classBuilder(serviceName)
				.addAnnotation(generated)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(com.honeyedoak.ppksecuredjson.crypto.SymmetricCryptoService.class)
				.addAnnotation(org.springframework.stereotype.Service.class)
				.addField(symmetricCryptoUtils)
				.addMethod(constructor)
				.addMethod(decrypt)
				.addMethod(decryptChar)
				.addMethod(encrypt)
				.addMethod(encryptChar)
				.build();

		JavaFile.builder(annotatedPackageElement.getQualifiedName().toString(), asymmetricCryptoService)
				.build()
				.writeTo(filer);
	}
}
