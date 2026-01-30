package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsClient {

    private final RestClient restClient;

    public void saveHit(EndpointHitDto hitDto) {
        log.info("Отправка запроса saveHit: path=/hit, body={}", hitDto);

        restClient.post()
                .uri("/hit")
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();

        log.info("Hit был сохранен");
    }

    public List<ViewStatsDto> getStats(String start, String end, String[] uris, boolean unique) {
        String pathAndQuery = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", (Object[]) uris)
                .queryParam("unique", unique)
                .build(false)
                .toUriString();

        log.info("Отправка запроса getStats: {}", pathAndQuery);

        ViewStatsDto[] body = restClient.get()
                .uri(pathAndQuery)
                .retrieve()
                .body(ViewStatsDto[].class);

        List<ViewStatsDto> stats = Arrays.asList(body != null ? body : new ViewStatsDto[0]);

        log.info("getStats вернул {} записей", stats.size());
        return stats;
    }
}
