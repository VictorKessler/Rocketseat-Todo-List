package br.com.victorkessler.todolist.task.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.victorkessler.todolist.user.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
            // Get authentication
            final var authorization = request.getHeader("Authorization");
            final var authEncoded = authorization.substring("Basic".length()).trim();

            final var authDecoded = Base64.getDecoder().decode(authEncoded);
            final var authString = new String(authDecoded);

            final var credentials = authString.split(":");
            final var username = credentials[0];
            final var password = credentials[1];

            // Validates password
            final var user = userRepository.findByUserName(username);

            if (user == null) {
                response.sendError(401, "User doesn't have permission");
            } else {
                // Validate password
                final var verifiedPassword = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (verifiedPassword.verified) {
                    request.setAttribute("userId", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }

            }
        } else {
            filterChain.doFilter(request, response);
        }

    }
}
