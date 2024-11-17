package org.example.github2.Security;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class SHA256 {
    public static String hash(String input) {
        byte[] hashBytes = DigestUtils.sha256(input);
        return Hex.encodeHexString(hashBytes);
    }
}
