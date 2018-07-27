package com.example.authentication.password;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@SpringBootApplication
class AuthenticationApplication {

		public static void main(String[] args) {
				SpringApplication.run(com.example.authentication.memory.AuthenticationApplication.class, args);
		}

/*
		@Bean
		PasswordEncoder plainTextPasswordEncoder() {
				return NoOpPasswordEncoder.getInstance();
		}
*/

		@Bean
		PasswordEncoder delegatingPasswordEncoder() {
				return PasswordEncoderFactories.createDelegatingPasswordEncoder();
		}

		/*
		 1. this is plain text. use NoOpPasswordEncoder.getInstance() ;
		 2. in order to take advantage of the new password encode in Sing security u need to migrate existing passwords.
		 3. if u are using plaint text that amounts to prefixing it whatever the appropirate prefix.
		 4. now replace NooOPWEncoder w/ DelegatingPWencoder
		 5. now imagine u have a signup page and the new user signs up and uses BCrypt. Use the DelegatingPWEncoder to encode new strings. the default is bcrypt.
		 6. NEW! spring security UDS supports password migration through the UserDetailsPasswordService. Out of the box only InMemoryUDM supports this. the idea is simple. we want them to login and SS
		 			and ideally to change their password.  SS will automatically check if the old password was stored in a insecure way and migrate to brcypt by invoking our UserDetailsPasswordService.
		 			All fo that logic is in the DaoAuthentiationProvider. It scostly to upgrade the encoding so we dont do this unless we need to
		 			But ur auth SHOULD support it. so the business/marketing could be any of a number of things. "weve changed terms of service. login." or "its been a month, password expired." or
		 			"we want to proactively protect u. taking additional measures."
			7. there were two different PasswordEncoders in SS 4.2
			8. if you are using the old password encoders u should see the migration javadocs in org.springframework.security.crypto.password.MessageDigestPasswordEncoder.
		 */

		@Service
		static class MyUDS implements UserDetailsService, UserDetailsPasswordService {

				static class MyUD implements UserDetails {
						@Override
						public Collection<? extends GrantedAuthority> getAuthorities() {
								return null;
						}

						@Override
						public String getPassword() {
								return null;
						}

						@Override
						public String getUsername() {
								return null;
						}

						@Override
						public boolean isAccountNonExpired() {
								return false;
						}

						@Override
						public boolean isAccountNonLocked() {
								return false;
						}

						@Override
						public boolean isCredentialsNonExpired() {
								return false;
						}

						@Override
						public boolean isEnabled() {
								return false;
						}
				}

				private final Map<String, UserDetails> userDetailsMap = new HashMap<>();

				@Override
				public UserDetails updatePassword(UserDetails userDetails, String newPasswordToUpgradeTo) {
						// u can return a custom UserDetails
						// run SQL statement

						return loadUserByUsername(userDetails.getUsername());
				}

				@Override
				public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
						return null;
				}
		}
}