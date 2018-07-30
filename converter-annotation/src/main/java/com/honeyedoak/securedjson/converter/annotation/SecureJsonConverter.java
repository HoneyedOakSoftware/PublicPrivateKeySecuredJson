package com.honeyedoak.securedjson.converter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SecureJsonConverter {

	String targetPackage();

	String charsetPropertyName() default "ppksecuredws.charset";

	String charsetDefaultValue() default "UTF-8";

	String oneTimePasswordLengthPropertyName() default "ppksecuredws.oneTimePasswordLength";

	int oneTimePasswordLengthDefaultValue() default  200;

	String keystoreLocationPropertyName() default "ppksecuredws.keystorelocation";

	String keystorePasswordPropertyName() default "ppksecuredws.keystorepassword";

}
