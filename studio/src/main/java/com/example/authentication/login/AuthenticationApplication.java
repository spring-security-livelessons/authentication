package com.example.authentication.login;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.stream.Stream;

@SpringBootApplication
public class AuthenticationApplication {

		@Bean
 		InMemoryUserDetailsManager memory() {
				return new InMemoryUserDetailsManager();
		}

		@Bean
		InitializingBean initializer(UserDetailsManager udm) {
				return () -> Stream
					.of("jlong", "rwinch")
					.map(name -> User.withDefaultPasswordEncoder().password("password").username(name).roles("USER").build())
					.forEach(udm::createUser);
		}

		public static void main(String[] args) {
				SpringApplication.run(AuthenticationApplication.class, args);
		}
}

@ControllerAdvice
class SecurityControllerAdvice {

		@ModelAttribute("currentUser")
		Principal currentUser(Principal principal) {
				return principal;
		}
}

@Controller
class LoginController {

		@GetMapping("/")
		String index() {
				return "hidden";
		}

		@GetMapping("/logout-success")
		String logout() {
				return "logout";
		}

		@GetMapping("/login")
		String login() {
				return "login";
		}
}


@EnableWebSecurity
@Configuration
@Order(5)
class SecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
				http.authorizeRequests().anyRequest().authenticated();
				http.logout().logoutUrl("/logout").logoutSuccessUrl("/logout-success").permitAll();
				http.formLogin().loginPage("/login").permitAll();
				http.httpBasic();
		}
}


@RestController
class GreetingsRestController {

		@GetMapping("/greetings")
		String greet(Principal principal) {
				return "hello, " + principal.getName() + "!";
		}
}


