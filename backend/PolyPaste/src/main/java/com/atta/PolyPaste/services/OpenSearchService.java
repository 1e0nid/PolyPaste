package com.atta.PolyPaste.services;

import com.atta.PolyPaste.dto.ModerationPasteMessageDto;
import com.atta.PolyPaste.dto.PasteIndexDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final OpenSearchClient openSearchClient;

    @Async("searchIndexExecutor")
    public void createIndex(ModerationPasteMessageDto message) throws IOException {
        PasteIndexDto indexDto = new PasteIndexDto(
                message.shortUrl(),
                message.shortUrl(),
                message.firstName() + " " + message.lastName(),
                message.content(),
                Instant.now().toString()
        );

        openSearchClient.index(request -> request
                .index("pastes")
                .id(indexDto.id())
                .document(indexDto)
        );
    }

    @Async("searchIndexExecutor")
    public void createIndexDirect(PasteIndexDto indexDto) throws IOException {
        openSearchClient.index(request -> request
                .index("pastes")
                .id(indexDto.id())
                .document(indexDto)
        );
    }

    @Async("searchIndexExecutor")
    public List<Map<String, Object>> search(String query) throws IOException {
        SearchResponse<Map> response = openSearchClient.search(s -> s
                        .index("pastes")
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields("title", "content")
                                        .query(query)
                                )
                        ),
                Map.class
        );

        List<Map<String, Object>> results = new ArrayList<>();
        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source != null) {
                results.add(Map.of(
                        "id", hit.id(),
                        "title", source.getOrDefault("title", "Без названия"),
                        "author", source.getOrDefault("author", "Гость"),
                        "created_at", source.getOrDefault("createdAt", "")
                ));
            }
        }
        log.info(results.toString());
        return results;
    }

    @Async("searchIndexExecutor")
    public void deleteById(String id) throws IOException {

        DeleteResponse response = openSearchClient.delete(request -> request
                .index("pastes")
                .id(id)
        );

        log.info("Результат удаления из OpenSearch: {}", response.result().jsonValue());
    }
}