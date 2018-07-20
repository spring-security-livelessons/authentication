package livelessons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@SpringBootApplication
public class LoginApplication {

		public static void main(String[] args) {
				SpringApplication.run(LoginApplication.class, args);
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

		@GetMapping("/login")
		String login() {
				return "login";
		}
}

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
//				super.configure(http);
				http
					.authorizeRequests()
					.anyRequest().authenticated()
					.and()
					.formLogin()
					.loginPage("/login").permitAll();
		}
}
