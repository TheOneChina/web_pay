package com.tnsoft.web.util;

import com.expertise.common.codec.Hex;
import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.DbSession;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAUser;

import java.security.GeneralSecurityException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.Arrays;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public final class AuthUtils {
    
    public static final int AUTH_OK = 0;
    public static final int AUTH_DISABLED = -1;
    public static final int AUTH_ATTEMPT_EXCEED = -2;
    public static final int AUTH_FAILED = -3;

    private static final int MAX_ATTEMPT = 10;

    private AuthUtils() {
    }

    public static int authWithPassword(NDAUser user, byte[] password, boolean reset)
            throws GeneralSecurityException {
        if (user.getStatus() != Constants.State.STATE_ACTIVE) {
            return AUTH_DISABLED;
        }

        int attempt = user.getAttempt();
        if (attempt > MAX_ATTEMPT) {
            return AUTH_ATTEMPT_EXCEED;
        }

        byte[] hash = hash(user.getName(), password);
        if (!Arrays.equals(user.getPassword(), hash)) {
            user.setAttempt(user.getAttempt() + 1);
            return AUTH_FAILED;
        }

        user.setAttempt(0);
        String ticket = user.getTicket();
        if (ticket == null || reset) {
            user.setTicket(newTicket());
        }
        return AUTH_OK;
    }

    public static NDAUser authWithTicket(DbSession session, String userName, String ticket)
            throws GeneralSecurityException {
        Criteria criteria = session.createCriteria(NDAUser.class);
        criteria.add(Restrictions.eq("name", userName)); //$NON-NLS-1$
        NDAUser user = (NDAUser)criteria.uniqueResult();
        if (user == null || user.getStatus() != Constants.State.STATE_ACTIVE) {
            return null;
        }

        int attempt = user.getAttempt();
        if (attempt > MAX_ATTEMPT) {
            return null;
        }

        if (!ticket.equals(user.getTicket())) {
            user.setAttempt(user.getAttempt() + 1);
            return null;
        }

        user.setAttempt(0);
        return user;
    }

    public static byte[] hash(String userName, byte[] passwd) throws GeneralSecurityException {
        MessageDigest md5 = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
        md5.update(StringUtils.toBytesQuietly(userName));
        md5.update(passwd);
        return md5.digest();
    }

    public static String newTicket() {
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[16];
        random.nextBytes(buf);
        return Hex.toHexString(buf);
    }
    
    public static byte[] newPassword(String pwd){
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            return md5.digest(StringUtils.toBytesQuietly(pwd));
        } catch (NoSuchAlgorithmException e) {
        }
        return new byte[]{};
    }
    
    public static String newValidateCode(int len){
        int[] array = {0,1,2,3,4,5,6,7,8,9};
        SecureRandom random = new SecureRandom();
        for (int i = 10; i > 1; i--) {
            int index = random.nextInt(i);
            int tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        int result = 0;
        for(int i = 0; i < len; i++) {
            result = result * 10 + array[i];
        }
        
        String str = String.valueOf(result);   
        if(str.length() < len){
            str = "0" + str;
        }
        
        return str;
    }
        
    public static byte[] md5(String pwd){
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            return md5.digest(StringUtils.toBytesQuietly(pwd));
        } catch (NoSuchAlgorithmException e) {
        }
        return new byte[]{};
    }
}
