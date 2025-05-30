package services;

import models.Admin;
import models.Doctor;
import models.Patient;
import models.Pharmacist;
import models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class AuthenticationService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    
    // Maps to track login attempts and lockouts
    private Map<String, Integer> loginAttempts = new HashMap<>();
    private Map<String, Long> lockoutTimes = new HashMap<>();
    
    private List<Admin> admins;
    private List<Patient> patients;
    private List<Doctor> doctors;
    private List<Pharmacist> pharmacists;
    
    /**
     * Constructor to initialize AuthenticationService
     * 
     * @param admins List of admins
     * @param patients List of patients
     * @param doctors List of doctors
     * @param pharmacists List of pharmacists
     */
    public AuthenticationService(List<Admin> admins, List<Patient> patients, 
                               List<Doctor> doctors, List<Pharmacist> pharmacists) {
        this.admins = admins;
        this.patients = patients;
        this.doctors = doctors;
        this.pharmacists = pharmacists;
    }
    
    /**
     * Generic method to authenticate a user
     * 
     * @param <T> Type of user
     * @param username Username to authenticate
     * @param password Password to authenticate
     * @param userFinder Function to find a user by username
     * @return User object if authentication successful, null otherwise
     */
    private <T extends User> T authenticate(String username, String password, Function<String, T> userFinder) {
        // Debug log removed
        
        if (!validateLoginInput(username, password)) {
            // Debug log removed
            return null;
        }
        
        if (isAccountLocked(username)) {
            System.out.println("Account is temporarily locked due to multiple failed login attempts.");
            System.out.println("Please try again after " + LOCKOUT_DURATION_MINUTES + " minutes.");
            // Debug log removed
            return null;
        }
        
        T user = userFinder.apply(username);
        
        if (user == null) {
            // Debug log removed
            recordFailedLoginAttempt(username);
            return null;
        }
        
        // Debug log removed
        
        boolean authenticated = false;
        
        // Check if the password is already hashed
        if (user.getPassword().startsWith("$SHA$")) {
            // Verify hashed password
            // Debug log removed
            authenticated = verifyPassword(password, user.getPassword());
        } else {
            // Legacy authentication (plaintext password)
            // Debug logs removed
            // Debug logs removed
            authenticated = user.getPassword().equals(password);
            
            // Upgrade to hashed password if authentication successful
            if (authenticated) {
                // Debug log removed
                user.setPassword(hashPassword(password));
            }
        }
        
        if (authenticated) {
            // Debug log removed
            resetLoginAttempts(username);
            return user;
        } else {
            // Debug log removed
            recordFailedLoginAttempt(username);
            return null;
        }
    }
    
    /**
     * Authenticate admin credentials with security measures
     * 
     * @param username Admin username
     * @param password Admin password (plaintext)
     * @return Admin object if authentication successful, null otherwise
     */
    public Admin authenticateAdmin(String username, String password) {
        return authenticate(username, password, 
            un -> admins.stream()
                .filter(a -> a.getUsername().equals(un))
                .findFirst()
                .orElse(null));
    }
    
    /**
     * Authenticate patient credentials with security measures
     * 
     * @param username Patient username
     * @param password Patient password (plaintext)
     * @return Patient object if authentication successful, null otherwise
     */
    public Patient authenticatePatient(String username, String password) {
        // Perform case-insensitive username matching for patients
        return authenticate(username, password, 
            un -> patients.stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(un))
                .findFirst()
                .orElse(null));
    }
    
    /**
     * Authenticate doctor credentials with security measures
     * 
     * @param username Doctor username
     * @param password Doctor password (plaintext)
     * @return Doctor object if authentication successful, null otherwise
     */
    public Doctor authenticateDoctor(String username, String password) {
        return authenticate(username, password, 
            un -> doctors.stream()
                .filter(d -> d.getUsername().equals(un))
                .findFirst()
                .orElse(null));
    }
    
    /**
     * Authenticate pharmacist credentials with security measures
     * 
     * @param username Pharmacist username
     * @param password Pharmacist password (plaintext)
     * @return Pharmacist object if authentication successful, null otherwise
     */
    public Pharmacist authenticatePharmacist(String username, String password) {
        return authenticate(username, password, 
            un -> pharmacists.stream()
                .filter(p -> p.getUsername().equals(un))
                .findFirst()
                .orElse(null));
    }
    
    /**
     * hashPassword - Creates a secure SHA-256 password hash with salt
     * 
     * This method implements cryptographic best practices by generating
     * a random salt for each password and applying SHA-256 hashing.
     * The result is formatted as "$SHA$salt$hash" using Base64 encoding
     * for storage compatibility.
     * 
     * @param plainPassword The plain text password to hash
     * @return Securely hashed password with embedded salt
     */
    public String hashPassword(String plainPassword) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            // Create SHA-256 hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            
            // Convert to Base64 for storage
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            // Format: $SHA$salt$hash
            return "$SHA$" + saltBase64 + "$" + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            // Fallback to plaintext if hashing fails
            System.err.println("Warning: Password hashing failed. Using plaintext password.");
            return plainPassword;
        }
    }
    
    /**
     *verifyPassword - Securely validates user password against stored hash
     * 
     * This method demonstrates secure password verification by extracting
     * the salt from the stored hash, applying the same hashing algorithm,
     * and using a timing-safe comparison to prevent timing attacks.
     * The method handles the "$SHA$salt$hash" format created by hashPassword.
     * 
     * @param plainPassword The plain text password to verify
     * @param storedHash The stored password hash with embedded salt
     * @return True if password matches, false otherwise
     */
    private boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            if (!storedHash.startsWith("$SHA$")) {
                // Not a hashed password, can't verify
                return false;
            }
            
            // Split the hash into components
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            
            // Extract salt and hash
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[3]);
            
            // Hash the plaintext password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] computedHash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            
            // Compare hashes
            return MessageDigest.isEqual(storedHashBytes, computedHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * validateLoginInput - Prevents injection attacks and ensures data quality
     * 
     * @param username The username to validate
     * @param password The password to validate
     * @return True if input is valid, false otherwise
     */
    private boolean validateLoginInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty.");
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        
        if (username.length() < 2) {
            System.out.println("Username must be at least 3 characters long.");
            return false;
        }
        
        // Check for potentially dangerous characters
        if (username.contains(";") || username.contains("'") || username.contains("\"") || 
            username.contains("`") || username.contains("--")) {
            System.out.println("Username contains invalid characters.");
            return false;
        }
        
        return true;
    }
    
    /**
     * recordFailedLoginAttempt - Implements brute force attack protection
     * 
     * This method enhances system security by tracking failed login attempts
     * and implementing account lockouts after reaching a threshold. It provides
     * user feedback about remaining attempts and implements a timing-based
     * lockout mechanism to prevent password guessing attacks.
     * 
     * @param username The username that failed login
     */
    private void recordFailedLoginAttempt(String username) {
        int attempts = loginAttempts.getOrDefault(username, 0) + 1;
        loginAttempts.put(username, attempts);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            // Lock the account
            lockoutTimes.put(username, System.currentTimeMillis());
            System.out.println("Too many failed login attempts. Account locked for " + 
                             LOCKOUT_DURATION_MINUTES + " minutes.");
        } else {
            int remainingAttempts = MAX_LOGIN_ATTEMPTS - attempts;
            System.out.println("Login failed. " + remainingAttempts + 
                             " attempt" + (remainingAttempts > 1 ? "s" : "") + " remaining.");
        }
    }
    
    /**
     *resetLoginAttempts - Clears security constraints after successful login
     * 
     * This method works in tandem with the account lockout system to
     * reset the security constraints when a user successfully authenticates.
     * It demonstrates proper security state management by cleaning up
     * tracking data after a legitimate user gains access.
     * 
     * @param username The username to reset attempts for
     */
    private void resetLoginAttempts(String username) {
        loginAttempts.remove(username);
        lockoutTimes.remove(username);
    }
    
    /**
     *isAccountLocked - Enforces temporary account lockout policy
     * 
     * This method implements a time-based account lockout mechanism that
     * automatically expires after the defined lockout duration. It prevents
     * brute force attacks while ensuring legitimate users can regain access
     * after a security timeout without administrator intervention.
     * 
     * @param username The username to check lockout status
     * @return True if account is currently locked, false if accessible
     */
    private boolean isAccountLocked(String username) {
        Long lockTime = lockoutTimes.get(username);
        if (lockTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long lockoutDurationMillis = LOCKOUT_DURATION_MINUTES * 60 * 1000;
        
        if (currentTime - lockTime > lockoutDurationMillis) {
            // Lockout period has passed
            lockoutTimes.remove(username);
            loginAttempts.remove(username);
            return false;
        }
        
        return true;
    }
    
    /**
     *setHashedPassword - Secures user credentials during account creation
     * 
     * This utility method simplifies the process of securely storing
     * passwords when creating new users or updating existing credentials.
     * It encapsulates the secure hashing process to ensure consistent
     * application of security practices throughout the system.
     * 
     * @param user User object whose password needs to be secured
     * @param plainPassword The plain text password to hash and store
     */
    public void setHashedPassword(User user, String plainPassword) {
        // We still use the local hashPassword method since it's a public interface
        // that might be used elsewhere in the codebase
        String hashedPassword = hashPassword(plainPassword);
        user.setPassword(hashedPassword);
    }
}