package ru.practicum;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final RestTemplate restTemplate;

    @Value("${stats-server.url}")
    private String statsServerUrl; //  будет в properties основно модуля

    @Value("${app.name}")
    private String app; //  будет в properties основно модуля

    public void postHit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        HitRequestDTO hitRequest = new HitRequestDTO();
        hitRequest.setApp(app);
        hitRequest.setIp(ip);
        hitRequest.setUri(uri);

        restTemplate.postForObject(statsServerUrl + "/hit", hitRequest, Void.class);
    }

    public List<StatsResponse> getStats(LocalDateTime start,
                                        LocalDateTime end,
                                        List<String> uris,
                                        boolean unique) {
        try {
            StringBuilder urlBuilder = new StringBuilder(statsServerUrl + "/stats?");
            urlBuilder.append("start=").append(URLEncoder.encode(FORMATTER.format(start), StandardCharsets.UTF_8));
            urlBuilder.append("&end=").append(URLEncoder.encode(FORMATTER.format(end), StandardCharsets.UTF_8));

            if (uris != null && !uris.isEmpty()) {
                urlBuilder.append("&uris=").append(URLEncoder.encode(String.join(",", uris), StandardCharsets.UTF_8));
            }
            urlBuilder.append("&unique=").append(unique);

            URI uri = new URI(urlBuilder.toString());

            ResponseEntity<StatsResponse[]> response = restTemplate.getForEntity(uri, StatsResponse[].class);
            return Arrays.asList(Objects.requireNonNull(response.getBody()));

        } catch (Exception e) {
            throw new RuntimeException("Error while requesting stats from stats-server", e);
        }
    }
}
