package com.mycompany.kavac.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // JAX-RS tự scan toàn bộ @Path trong project
}
