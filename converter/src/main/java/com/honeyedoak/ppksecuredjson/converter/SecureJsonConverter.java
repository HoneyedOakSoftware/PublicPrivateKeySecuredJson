package com.honeyedoak.ppksecuredjson.converter;

import com.honeyedoak.ppksecuredjson.converter.model.SecuredJson;
import com.honeyedoak.ppksecuredjson.converter.model.UnsecuredJson;
import com.honeyedoak.ppksecuredjson.crypto.exception.CryptoException;

import java.security.Key;

public interface SecureJsonConverter {

	SecuredJson secureJson(String plainJson, Key key) throws CryptoException;

	UnsecuredJson<String> unsecureJson(SecuredJson securedJson) throws CryptoException;
}
