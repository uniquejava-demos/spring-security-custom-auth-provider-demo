package demo.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class MyUserDetailsService implements UserDetailsService {
    final PasswordEncoder passwordEncoder;

    public MyUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // This is where you should fetch the user from database.
        // We keep it simple to focus on authentication flow.
        Map<String, String> users = new HashMap<>();
        users.put("cyper", passwordEncoder.encode("123"));
        if (users.containsKey(username))
            return new User(username, users.get(username), new ArrayList<>());
        throw new UsernameNotFoundException(username);
    }
}
