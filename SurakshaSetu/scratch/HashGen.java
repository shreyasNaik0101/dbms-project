import com.suraksha.setu.util.PasswordHasher;

public class HashGen {
    public static void main(String[] args) {
        System.out.println("password123 -> " + PasswordHasher.hash("password123"));
        System.out.println("admin123 -> " + PasswordHasher.hash("admin123"));
    }
}
