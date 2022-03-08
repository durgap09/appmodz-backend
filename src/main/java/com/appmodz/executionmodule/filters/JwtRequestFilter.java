package com.appmodz.executionmodule.filters;

import com.appmodz.executionmodule.dao.RolePermissionDAO;
import com.appmodz.executionmodule.dao.UserOrganizationDAO;
import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.model.Permission;
import com.appmodz.executionmodule.model.Role;
import com.appmodz.executionmodule.model.RolePermissions;
import com.appmodz.executionmodule.service.JWTUtilService;
import com.appmodz.executionmodule.service.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JWTUtilService jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
            }


            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {

                    Integer userOrganizationId = jwtUtil.extractUserOrganizationId(jwt);
                    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                    if(userOrganizationId!=null) {
                        grantedAuthorities.add(new SimpleGrantedAuthority(""+userOrganizationId));
                    }
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, grantedAuthorities);
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseDTO responseDTO = new ResponseDTO("failure","Unauthorized",null);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            ObjectMapper mapper = new ObjectMapper();
            PrintWriter out = response.getWriter();
            out.print(mapper.writeValueAsString(responseDTO ));
            out.flush();
            return;
        }


        chain.doFilter(request, response);
        this.resetAuthenticationAfterRequest();
    }

    private void resetAuthenticationAfterRequest() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }


}
