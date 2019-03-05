/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.web.security;

import com.expertise.common.codec.Hex;
import com.expertise.common.util.StringUtils;
import com.tnsoft.web.util.Utils;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import java.util.Arrays;

import org.springframework.security.authentication.encoding.PasswordEncoder;


public class LoginPasswordEncoder implements PasswordEncoder {
    
    @Override
    public String encodePassword(String rawPass, Object object) {
                
        try{
            return Hex.toHexString(hash(object.toString(), Utils.md5(rawPass)));
        } catch(GeneralSecurityException e){
            return "";
        } catch(NullPointerException e) {
            return "";
        }
    }

    @Override
    public boolean isPasswordValid(String pass, String rawPass, Object object) {
        
        if(null == pass || null == rawPass || object == null){
            return false;
        }
                
        String tmp = encodePassword(rawPass, object);
                
        return Arrays.equals(Hex.toByteArray(pass), Hex.toByteArray(tmp));
    }
    
    private static byte[] hash(String userName, byte[] passwd) throws GeneralSecurityException {
        MessageDigest md5 = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
        md5.update(StringUtils.toBytesQuietly(userName));
        md5.update(passwd);
        return md5.digest();
    }
    
    public static void main(String ...args){
        try {
            System.out.println(Hex.toHexString(hash("admin@tnsoft.com", Utils.md5("111111"))));
        } catch (GeneralSecurityException e) {
        }
    }
    
}
