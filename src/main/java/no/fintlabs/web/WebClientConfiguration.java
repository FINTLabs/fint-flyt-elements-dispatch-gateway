package no.fintlabs.web;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfiguration {

    @Bean
    public ClientHttpConnector clientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create(
                        ConnectionProvider
                                .builder("laidback")
                                .maxLifeTime(Duration.ofMinutes(30))
                                .maxIdleTime(Duration.ofMinutes(5))
                                .build())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300000)
                .responseTimeout(Duration.ofMinutes(5)
                ));
    }

}
