package domain.service;

public interface PasswordHasher 
{
    String hash(String password);
    boolean check(String password, String hashed);
}