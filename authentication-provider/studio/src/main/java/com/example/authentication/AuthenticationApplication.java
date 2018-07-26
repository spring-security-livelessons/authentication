package com.example.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
public class AuthenticationApplication {

		/*

		@Bean
		UserDetailsManager memory() {
				return new InMemoryUserDetailsManager();
		}

		@Bean
		UserDetailsManager jdbc(DataSource ds) {
				JdbcUserDetailsManager jdbc = new JdbcUserDetailsManager();
				jdbc.setDataSource(ds);
				return jdbc;
		}

		@Bean
		ApplicationRunner runner(UserDetailsManager userDetailsManager) {
				return args -> {
						UserDetails rob = User.withDefaultPasswordEncoder().username("rob").password("password").roles("USER", "ADMIN").build();
						UserDetails josh = User.withUserDetails(rob).username("josh").roles("USER").build();
						userDetailsManager.createUser(rob);
						userDetailsManager.createUser(josh);
				};
		}
*/

		public static void main(String[] args) {
				SpringApplication.run(AuthenticationApplication.class, args);
		}
}


@RestController
class GreetingsRestController {

		@GetMapping("/greetings")
		String greeting(Principal principal) {
				return "hello, " + principal.getName() + "!";
		}
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
				http
					.csrf().disable()
					.httpBasic().and().formLogin()
					.and()
					.authorizeRequests()
					.anyRequest().authenticated();
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
				auth
					.ldapAuthentication()
					.userDnPatterns("uid={0},ou=people")
					.groupSearchBase("ou=groups")
					.contextSource()
					.url("ldap://localhost:8389/dc=springframework,dc=org")
					.and()
					.passwordCompare()
					.passwordEncoder(new LdapShaPasswordEncoder())
					.passwordAttribute("userPassword");
		}

}