package gr.unisystems.filter;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

public class CustomApplication extends ResourceConfig
{
    public CustomApplication() {
        this.packages(new String[] { "gr.unisystems.filter" });
        this.register(LoggingFilter.class);
        this.register(GsonMessageBodyHandler.class);
        this.register(AuthenticationFilter.class);
    }
}
