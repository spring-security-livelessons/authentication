package com.example.authentication.password;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@SpringBootApplication
class AuthenticationApplication {

		@RestController
		public static class GreetingsRestController {

				@GetMapping("/greetings")
				String greetings(Principal principal) {
						return "hiya " + principal.getName() + "!";
				}
		}

		public static void main(String[] args) {
				SpringApplication.run(AuthenticationApplication.class, args);
		}

		@Configuration
		public static class CommonConfiguration extends WebSecurityConfigurerAdapter {

				@Override
				protected void configure(HttpSecurity http) throws Exception {
						http
							.httpBasic();
				}
		}

//		 	@Bean
		/*PasswordEncoder oldPasswordEncoder() {
				return new PasswordEncoder() {

						private final String prefix = "old:";

						@Override
						public String encode(CharSequence charSequence) {
								return prefix + charSequence;
						}

						@Override
						public boolean matches(CharSequence rawPasswordBeingSubmitted, String prefixEncodedPassword) {
								if (StringUtils.isEmpty(rawPasswordBeingSubmitted) && StringUtils.isEmpty(prefixEncodedPassword)) {
										return true;
								}
								String encodedPassword = prefixEncodedPassword.substring(prefix.length());
								return encodedPassword.equals(rawPasswordBeingSubmitted.toString());
						}
				};
		}
*/

		//@Bean
		PasswordEncoder oldPasswordEncoder() {
				String md5 = "MD5";
				return new DelegatingPasswordEncoder(
					md5, Collections.singletonMap(md5, new org.springframework.security.crypto.password.MessageDigestPasswordEncoder(md5)));
		}

		@Bean
		PasswordEncoder delegatingPasswordEncoder() {
				return PasswordEncoderFactories.createDelegatingPasswordEncoder();
		}

		/*
		 1. this is plain text. use NoOpPasswordEncoder.getInstance() ;
		 2. in order to take advantage of the new plainPasswordEncoder encode in Sing security u need to migrate existing passwords.
		 3. if u are using plaint text that amounts to prefixing it whatever the appropirate prefix.
		 4. now replace NooOPWEncoder w/ DelegatingPWencoder
		 5. now imagine u have a signup page and the new user signs up and uses BCrypt. Use the DelegatingPWEncoder to encode new strings. the default is bcrypt.
		 6. NEW! spring security UDS supports plainPasswordEncoder migration through the UserDetailsPasswordService. Out of the box only InMemoryUDM supports this. the idea is simple. we want them to login and SS
		 			and ideally to change their plainPasswordEncoder.  SS will automatically check if the old plainPasswordEncoder was stored in a insecure way and migrate to brcypt by invoking our UserDetailsPasswordService.
		 			All fo that logic is in the DaoAuthentiationProvider. It scostly to upgrade the encoding so we dont do this unless we need to
		 			But ur auth SHOULD support it. so the business/marketing could be any of a number of things. "weve changed terms of service. login." or "its been a month, plainPasswordEncoder expired." or
		 			"we want to proactively protect u. taking additional measures."
			7. there were two different PasswordEncoders in SS 4.2
			8. if you are using the old plainPasswordEncoder encoders u should see the migration javadocs in org.springframework.security.crypto.plainPasswordEncoder.MessageDigestPasswordEncoder.
		 */

		@Bean
		UserDetailsService userDetailsService() {
				String password = oldPasswordEncoder().encode("password");
				UserDetails a = User.builder().password(password).roles("USER").username("jlong").build();
				UserDetails b = User.builder().password(password).roles("USER", "ADMIN").username("rwinch").build();
				return new MyUDS(a, b);
		}

		@Slf4j
		public static class MyUDS implements UserDetailsService, UserDetailsPasswordService {

				private static MyUD from(UserDetails ud) {
						return new MyUD(ud.getUsername(), ud.getPassword(), ud.isEnabled(), ud.getAuthorities());
				}

				MyUDS(UserDetails... uds) {
						Set<MyUD> collect = Stream.of(uds).map(MyUDS::from).collect(Collectors.toSet());
						collect.forEach(myUD -> this.users.put(myUD.getUsername(), myUD));
						log.info(this.users.toString());
				}

				@ToString
				private static class MyUD implements UserDetails {

						private final String username, password;
						private final boolean active;
						private final Set<GrantedAuthority> authorities = new HashSet<>();

						MyUD(String u, String pw, boolean a, Collection<? extends GrantedAuthority> authorities) {
								this.username = u;
								this.password = pw;
								this.active = a;
								this.authorities.addAll(authorities);
						}

						MyUD(String u, String pw, boolean a, String... authorities) {
								this(u, pw, a, Stream.of(authorities).map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));
						}

						MyUD(MyUD ud, String newPassword) {
								this(ud.username, newPassword, ud.active, ud.authorities);
						}

						@Override
						public Collection<? extends GrantedAuthority> getAuthorities() {
								return this.authorities;
						}

						@Override
						public String getPassword() {
								return this.password;
						}

						@Override
						public String getUsername() {
								return this.username;
						}

						@Override
						public boolean isAccountNonExpired() {
								return active;
						}

						@Override
						public boolean isAccountNonLocked() {
								return active;
						}

						@Override
						public boolean isCredentialsNonExpired() {
								return active;
						}

						@Override
						public boolean isEnabled() {
								return active;
						}
				}

				private final Map<String, UserDetails> users = new ConcurrentHashMap<>();

				@Override
				public UserDetails updatePassword(UserDetails userDetails, String newPasswordToUpgradeTo) {
						log.info("being prompted to updated plainPasswordEncoder for " + userDetails.getUsername() + " to " + newPasswordToUpgradeTo + ".");
						String username = userDetails.getUsername();
						this.users.put(username, new MyUD(MyUD.class.cast(userDetails), newPasswordToUpgradeTo));
						return this.loadUserByUsername(username);
				}

				@Override
				public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
						if (this.users.containsKey(s)) {
								return this.users.get(s);
						}
						throw new UsernameNotFoundException("couldn't find " + s + "!");
				}
		}
}