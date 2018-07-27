package com.example.authentication.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
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
@Slf4j
@EnableWebSecurity
class CommonSecurityConfig extends WebSecurityConfigurerAdapter {

		CommonSecurityConfig() {
				log.info("starting " + getClass().getName() + ".");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
				http.httpBasic();
				http.csrf().disable();
				http.authorizeRequests().anyRequest().authenticated();
		}
}

@Configuration
@Order(2)
class UserDetailsConfiguration {

		@Bean
		JdbcUserDetailsManager jdbc(DataSource ds) {
				return new JdbcUserDetailsManager(ds);
		}

		@Bean
		InitializingBean initializer(UserDetailsManager udm) {
				return () -> Stream
					.of("jlong", "rwinch")
					.map(name -> User.withDefaultPasswordEncoder().password("password").username(name).roles("USER").build())
					.forEach(udm::createUser);
		}
}
