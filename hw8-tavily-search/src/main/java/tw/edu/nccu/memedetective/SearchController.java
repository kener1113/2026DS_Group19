package tw.edu.nccu.memedetective;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class SearchController {
 private final TavilySearchService service;
 public SearchController(TavilySearchService service) { this.service = service; }
 @GetMapping("/")
 public String index(@RequestParam(defaultValue="") String q, Model model) {
  String query = q.strip(); model.addAttribute("q", query);
  if (!q.isEmpty() && query.isEmpty()) model.addAttribute("error", "請輸入搜尋關鍵字。");
  else if (query.length() > 300) model.addAttribute("error", "關鍵字請勿超過 300 個字。");
  else if (!query.isEmpty()) {
   try { model.addAttribute("results", service.search(query)); }
   catch (TavilySearchService.SearchException e) { model.addAttribute("error", e.getMessage()); }
  }
  return "index";
 }
}
