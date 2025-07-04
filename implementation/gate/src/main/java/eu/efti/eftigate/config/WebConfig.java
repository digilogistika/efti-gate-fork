package eu.efti.eftigate.config;

import eu.efti.eftigate.utils.StringAsObjectHttpMessageConverter;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // We want to handle parameters of type Object and content type application/xml as plain strings so that we may
        // do xml parsing explicitly in controller method. Let's add a pass-through converter for this combination as
        // the first converter so that MappingJackson2XmlHttpMessageConverter is not used for conversion.
        var stringAsObjectHttpMessageConverter = new StringAsObjectHttpMessageConverter();
        stringAsObjectHttpMessageConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_XML));
        converters.add(0, stringAsObjectHttpMessageConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NotNull String resourcePath, @NotNull Resource location) throws IOException {
                        // Exclude API paths
                        if (resourcePath.startsWith("api/") ||
                                resourcePath.startsWith("v1/") ||
                                resourcePath.startsWith("v3/") ||
                                resourcePath.startsWith("actuator/") ||
                                resourcePath.startsWith("swagger-ui/") ||
                                resourcePath.startsWith("swagger/") ||
                                resourcePath.startsWith("ws/")
                        ) {
                            return null;
                        }

                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() ? requestedResource :
                                new ClassPathResource("/static/index.html");
                    }
                })
        ;
    }
}
