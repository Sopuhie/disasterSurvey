package com.santech.framework.utils.component.encrypt.rsa;

import lombok.Data;

@Data
public class RSAKeyPair {
	
	/**公钥*/
	private String publicKey;
	/**私钥*/
    private String privateKey;

    public RSAKeyPair(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

}
