package io.github.legacy_mcp.samples.simple;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SimpleSampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimpleSampleApplicationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void listsConfiguredReadOnlyTools() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-MCP-API-Key", "dev-key");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/mcp",
                new HttpEntity<String>("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}", headers),
                String.class
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("demo.echo");
        assertThat(response.getBody()).contains("demo.user.search");
        assertThat(response.getBody()).doesNotContain("demo.notice.publish");
    }
}
