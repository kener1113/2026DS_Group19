package tw.edu.nccu.memedetective;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class TavilySearchService {
 private final String key;
 private final boolean enabled;
 private final HttpClient client;
 private final ObjectMapper mapper = new ObjectMapper();
 @org.springframework.beans.factory.annotation.Autowired
 public TavilySearchService(@Value("${tavily.apiKey:}") String key, @Value("${tavily.enabled:true}") boolean enabled) {
  this(key, enabled, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
 }
 TavilySearchService(String key, boolean enabled, HttpClient client) {
  this.key = key; this.enabled = enabled; this.client = client;
 }
 public record Result(String title, String url, String content) {}
 public List<Result> search(String query) {
  if (!enabled) throw new SearchException("搜尋服務目前未啟用。");
  if (key.isBlank()) throw new SearchException("尚未設定 API 金鑰。請在啟動前設定 TAVILY_API_KEY 環境變數。");
  try {
   var body = mapper.writeValueAsString(Map.of("query", query, "search_depth", "basic", "max_results", 8,
       "include_answer", false, "include_raw_content", false));
   var request = HttpRequest.newBuilder(URI.create("https://api.tavily.com/search"))
       .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + key)
       .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
   var response = client.send(request, HttpResponse.BodyHandlers.ofString());
   int status = response.statusCode();
   if (status == 401 || status == 403) throw new SearchException("API 金鑰無效或無權限，請檢查 Tavily Dashboard。");
   if (status == 429 || status == 432 || status == 433) throw new SearchException("搜尋次數或額度已達限制，請稍後重試或檢查 Tavily 額度。");
   if (status != 200) throw new SearchException("搜尋服務暫時無法使用，請稍後重試。");
   var root = mapper.readTree(response.body());
   if (!root.path("results").isArray()) throw new SearchException("搜尋服務回傳格式異常，請稍後重試。");
   List<Result> results = new ArrayList<>();
   for (var item : root.path("results")) {
    String url = item.path("url").asText("");
    if (safeUrl(url)) results.add(new Result(item.path("title").asText("未命名網頁"), url, item.path("content").asText("沒有摘要")));
   }
   return results;
  } catch (SearchException e) { throw e;
  } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new SearchException("搜尋已中斷，請重試。");
  } catch (Exception e) { throw new SearchException("搜尋連線逾時或回應異常，請檢查網路後重試。"); }
 }
 static boolean safeUrl(String url) {
  try { var uri = URI.create(url); return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) && uri.getHost() != null && uri.getUserInfo() == null; }
  catch (IllegalArgumentException e) { return false; }
 }
 public static class SearchException extends RuntimeException { public SearchException(String message) { super(message); } }
}
