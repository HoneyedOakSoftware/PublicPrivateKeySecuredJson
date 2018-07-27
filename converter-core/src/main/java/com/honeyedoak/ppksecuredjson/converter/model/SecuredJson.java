package com.honeyedoak.ppksecuredjson.converter.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SecuredJson {

	private String base64EncryptedJson;

	private String base64EncryptedOneTimePassword;

	private String base64encodedResponsePublicKey;

}
