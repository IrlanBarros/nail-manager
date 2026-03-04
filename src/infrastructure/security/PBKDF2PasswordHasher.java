package infrastructure.security;

import domain.service.PasswordHasher;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2PasswordHasher implements PasswordHasher 
{

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;

    @Override
    public String hash(String password) 
    {
        try 
        {
            byte[] salt = new byte[SALT_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(salt);

            byte[] hash = pbkdf2(password.toCharArray(), salt);

            return Base64.getEncoder().encodeToString(salt) + ":" + 
                   Base64.getEncoder().encodeToString(hash);
        } 
        catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }

    @Override
    public boolean check(String password, String storedHash) 
    {
        try 
        {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            byte[] testHash = pbkdf2(password.toCharArray(), salt);

            return slowEquals(hash, testHash);
        } 
        catch (Exception e) { return false; }
    }

    private byte[] pbkdf2(char[] password, byte[] salt) 
        throws NoSuchAlgorithmException, InvalidKeySpecException 
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    private boolean slowEquals(byte[] a, byte[] b) 
    {
        int diff = a.length ^ b.length;
        
        for (int i = 0; i < a.length && i < b.length; i++) 
        {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}