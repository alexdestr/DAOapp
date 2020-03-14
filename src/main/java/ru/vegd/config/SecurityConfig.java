package ru.vegd.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccessDeniedHandler accessDeniedHandler;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user").password(passwordEncoder().encode("1234")).roles("USER")
                .and()
                .withUser("admin").password(passwordEncoder().encode("1234")).roles("USER", "ADMIN")
                .and()
                .withUser("sa").password(passwordEncoder().encode("1")).roles("SUPER_ADMIN");
    }

    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/news/**").hasAuthority("ROLE_ADMIN")
                .antMatchers("/news/**").hasAuthority("ROLE_SUPER_ADMIN")
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler);

        http.formLogin()
                /*.loginPage("/login")*/
                .permitAll();

        http.logout()
                .permitAll()
                .invalidateHttpSession(true);

    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
