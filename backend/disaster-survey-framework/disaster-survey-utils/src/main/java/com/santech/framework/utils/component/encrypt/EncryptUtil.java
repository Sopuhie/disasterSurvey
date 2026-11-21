package com.santech.framework.utils.component.encrypt;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.bouncycastle.crypto.engines.SM2Engine;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
/**
 * @Title: EncryptUtil.java
 * @Description：密码加密方法
 * @Author： Liu
 * @Version： V1.0
 * @Modify：   
 */
public class EncryptUtil {

    public static String encrypt(String password) {
        //加密
        String salt = "xy";
        String md5 = new Md5Hash(password, salt, 2).toHex();
        return md5;
    }
    //政务中台同步过来的数据的解密
    public  static  String  decryptSm2(String  data)   {
    	String  privateKey  =  "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwI BAQQgvcMW3vYIUdE8kD29nDzqyXkkgDvzw5U8rokoBRjtjuOgCgYIKoEcz1UBgi2hRANCAA SCDxuqrcwN6m0CpCzqB38ttaOMyG3b4D9pPI+DbmOH/Dw45wnWJg4+XuqCP8f5jdSnwecaIp2NLFvGW37TALBK";
    	SM2  sm2  =  new  SM2(privateKey,  null);
    	sm2.setMode(SM2Engine.Mode.C1C3C2);
    	return  StrUtil.utf8Str(sm2.decryptFromBcd(data,  KeyType.PrivateKey));
   }
	
	/*
	 * public static void main(String[] args) { String p =
	 * "NTocean_disasterH#d3g@eH"; System.out.println(encrypt(p)); }
	 */
}
