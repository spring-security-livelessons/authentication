package com.example.jdbc;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.security.Principal;

@SpringBootApplication
public class JdbcApplication {

		@Bean
		UserDetailsManager memory(DataSource ds) {
				JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
				jdbcUserDetailsManager.setDataSource(ds);
				return jdbcUserDetailsManager;
		}

		@Bean
		InitializingBean initializer(UserDetailsManager manager) {
				return () -> {

						UserDetails josh = User.withDefaultPasswordEncoder().username("jlong").password("password").roles("USER").build();
						manager.createUser(josh);

						UserDetails rob = User.withUserDetails(josh).username("rwinch").build();
						manager.createUser(rob);
				};
		}


		public static void main(String[] args) {
				SpringApplication.run(JdbcApplication.class, args);
		}
}


@RestController
class GreetingsRestController {

		@GetMapping("/greeting")
		String greeting(Principal principal) {
				return "hello, " + principal.getName() + "!";
		}
}

@Configuration
@EnableWebSecurity
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {

				http
					.httpBasic();

				http
					.authorizeRequests().anyRequest().authenticated();
		}
}