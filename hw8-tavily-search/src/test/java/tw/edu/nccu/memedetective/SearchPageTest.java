package tw.edu.nccu.memedetective;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SearchPageTest {
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TavilySearchService service;

    @Test
    void rendersClickableResultAndEscapesSummary() throws Exception {
        when(service.search("國立政治大學")).thenReturn(List.of(
                new TavilySearchService.Result("Test title", "https://example.com/", "<b>Test summary</b>")));

        MockMvcBuilders.webAppContextSetup(context).build()
                .perform(get("/").param("q", "國立政治大學"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"https://example.com/\"")))
                .andExpect(content().string(containsString("Test title")))
                .andExpect(content().string(containsString("&lt;b&gt;Test summary&lt;/b&gt;")));
    }

    @Test
    void rendersFriendlyMessageWhenSearchReturnsNoResults() throws Exception {
        when(service.search("no matches")).thenReturn(List.of());

        MockMvcBuilders.webAppContextSetup(context).build()
                .perform(get("/").param("q", "no matches"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("沒有找到結果")));
    }
}
