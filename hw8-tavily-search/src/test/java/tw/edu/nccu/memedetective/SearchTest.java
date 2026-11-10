package tw.edu.nccu.memedetective;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import java.net.http.*;
import org.springframework.ui.ExtendedModelMap;
class SearchTest {
 @SuppressWarnings("unchecked")
 private HttpClient client(int status, String body) throws Exception {
  HttpClient client = mock(HttpClient.class); HttpResponse<String> response = mock(HttpResponse.class);
  when(response.statusCode()).thenReturn(status); when(response.body()).thenReturn(body);
  when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response); return client;
 }
 @Test void mapsJsonAndUsesBearerPost() throws Exception {
  var c = client(200, "{\"results\":[{\"title\":\"Example\",\"url\":\"https://example.com\",\"content\":\"摘要\"},{\"url\":\"javascript:alert(1)\"}]}");
  var r = new TavilySearchService("test-placeholder", true, c).search("Java");
  assertEquals(1,r.size());assertEquals("摘要",r.get(0).content());
  var request = ArgumentCaptor.forClass(HttpRequest.class);verify(c).send(request.capture(),any(HttpResponse.BodyHandler.class));
  assertEquals("POST",request.getValue().method());assertEquals("https://api.tavily.com/search",request.getValue().uri().toString());
  assertEquals("Bearer test-placeholder",request.getValue().headers().firstValue("Authorization").orElseThrow());
 }
 @Test void emptyResults() throws Exception { assertTrue(new TavilySearchService("test-placeholder",true,client(200,"{\"results\":[]}")).search("Java").isEmpty()); }
 @Test void missingKey() { assertThrows(TavilySearchService.SearchException.class,()->new TavilySearchService("",true).search("Java")); }
 @Test void errorsAreFriendlyAndDoNotLeakBody() throws Exception {
  for(int status : new int[]{401,429,432,500}) {
   var s = new TavilySearchService("test-placeholder",true,client(status,"private upstream content"));
   var e=assertThrows(TavilySearchService.SearchException.class,()->s.search("Java"));
   assertFalse(e.getMessage().contains("private upstream"));
  }
 }
 @Test void malformedJson() throws Exception { assertThrows(TavilySearchService.SearchException.class,()->new TavilySearchService("test-placeholder",true,client(200,"{}")).search("Java")); }
 @Test void validatesQueryWithoutCallingApi() {
  var s=mock(TavilySearchService.class);var c=new SearchController(s);var m=new ExtendedModelMap();
  assertEquals("index",c.index(" ",m));assertTrue(m.containsAttribute("error"));
  c.index("a".repeat(301),new ExtendedModelMap());c.index("",new ExtendedModelMap());verifyNoInteractions(s);
 }
 @Test void urlsAreSafe() { assertFalse(TavilySearchService.safeUrl("//example.com"));assertFalse(TavilySearchService.safeUrl("https://user@example.com"));assertTrue(TavilySearchService.safeUrl("https://example.com")); }
}
