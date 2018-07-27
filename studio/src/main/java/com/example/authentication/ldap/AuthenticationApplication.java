package com.example.authentication.ldap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.stream.Stream;

@SpringBootApplication
public class AuthenticationApplication {

		public static void main(String[] args) {
				SpringApplication.run(AuthenticationApplication.class, args);
		}
}

@RestController
class GreetingsRestController {

		@GetMapping("/greetings")
		String greet(Principal principal) {
				return "hello, " + principal.getName() + "!";
		}
}

@Configuration
@EnableWebSecurity
@Slf4j
class LdapSecurityConfig extends WebSecurityConfigurerAdapter {

		LdapSecurityConfig() {
				log.info("starting " + getClass().getName() + ".");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
				http.httpBasic();
				http.csrf().disable();
				http.authorizeRequests().anyRequest().authenticated();
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
				auth
					.ldapAuthentication()
					.userDnPatterns("uid={0},ou=people")
					.groupSearchBase("ou=groups")
					.contextSource()
					.url("ldap://127.0.0.1:8389/dc=springframework,dc=org")
					.and()
					.passwordCompare()
					.passwordEncoder(new LdapShaPasswordEncoder())
					.passwordAttribute("userPassword");
		}
}

