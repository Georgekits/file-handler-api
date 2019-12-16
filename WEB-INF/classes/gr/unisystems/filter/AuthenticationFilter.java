package gr.unisystems.filter;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Arrays;
import javax.annotation.security.RolesAllowed;
import java.util.StringTokenizer;
import org.glassfish.jersey.internal.util.Base64;
import javax.ws.rs.core.Response;
import java.util.List;
import javax.ws.rs.GET;
import java.lang.annotation.Annotation;
import javax.ws.rs.POST;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ContainerRequestFilter;

@Provider
public class AuthenticationFilter implements ContainerRequestFilter
{
    @Context
    private ResourceInfo resourceInfo;
    
    public void filter(final ContainerRequestContext requestContext) {
        final Method method = this.resourceInfo.getResourceMethod();
        final Boolean isPost = method.isAnnotationPresent((Class<? extends Annotation>)POST.class);
        final Boolean isGet = method.isAnnotationPresent((Class<? extends Annotation>)GET.class);
        System.out.println("Is a POST call: " + isPost);
        System.out.println("Is a GET call: " + isGet);
        if (isPost || isGet) {
            final MultivaluedMap<String, String> headers = (MultivaluedMap<String, String>)requestContext.getHeaders();
            final List<String> authorization = (List<String>)headers.get((Object)"Authorization");
            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity((Object)"No authorization granted.").build());
                return;
            }
            final String encodedUserPassword = authorization.get(0).replaceFirst("Basic ", "");
            final String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
            try {
                final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                final String username = tokenizer.nextToken();
                final String password = tokenizer.nextToken();
                System.out.println("Username: " + username);
                System.out.println("Password: " + password);
                if (method.isAnnotationPresent((Class<? extends Annotation>)RolesAllowed.class)) {
                    final RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                    final Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));
                    if (!this.isUserAllowed(username, password, rolesSet)) {
                        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity((Object)"Wrong credentials are provided.").build());
                        System.out.println("isUserAllowed");
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity((Object)"Please provide credentials.").build());
            }
        }
    }
    
    private boolean isUserAllowed(final String username, final String password, final Set<String> rolesSet) {
        boolean isAllowed = false;
        try {
            final Properties prop = new Properties();
            final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(inputStream);
            if (username.equals(prop.getProperty("username")) && password.equals(prop.getProperty("password"))) {
                final String userRole = prop.getProperty("role");
                if (rolesSet.contains(userRole)) {
                    isAllowed = true;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return isAllowed;
    }
}
