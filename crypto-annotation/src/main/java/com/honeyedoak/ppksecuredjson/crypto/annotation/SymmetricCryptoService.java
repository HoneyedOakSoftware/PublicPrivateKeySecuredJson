package com.honeyedoak.ppksecuredjson.crypto.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface SymmetricCryptoService {

	String serviceName() default "SymmetricCryptoService";

	String algorithm() default "PBEWITHSHA256AND128BITAES-CBC-BC";
}
