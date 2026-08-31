package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.collector.ithome.enabled", havingValue = "true", matchIfMissing = true)
public class IthomeHotSearchCollector implements HotSearchCollector {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 Chrome/140.0.0.0 Safari/537.36 HoloView/0.1";
    private final BaiduCategoryClassifier categoryClassifier; private final HttpClient httpClient; private final String url; private final int limit; private final Duration timeout;
    public IthomeHotSearchCollector(BaiduCategoryClassifier classifier, @Value("${app.collector.ithome.url:https://www.ithome.com/list/}") String url, @Value("${app.collector.ithome.limit:30}") int limit, @Value("${app.collector.ithome.timeout:10s}") Duration timeout) { this.categoryClassifier=classifier; this.url=url; this.limit=Math.max(1,Math.min(50,limit)); this.timeout=timeout; this.httpClient=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL).build(); }
    @Override public HotSource source(){return HotSource.ITHOME;}
    @Override public List<CollectedHotItem> collect(){ long started=System.nanoTime(); try { var r=httpClient.send(HttpRequest.newBuilder(URI.create(url)).timeout(timeout).header("User-Agent",USER_AGENT).header("Accept","text/html,application/xhtml+xml").GET().build(),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)); if(r.statusCode()!=200) throw new IllegalStateException("IThome hot board returned HTTP "+r.statusCode()); var items=parse(r.body()); log.info("IThome hot board collected: itemCount={}, responseBytes={}, durationMs={}",items.size(),r.body().getBytes(StandardCharsets.UTF_8).length,elapsed(started)); return items; } catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("IThome hot board request interrupted",e);} catch(IOException e){log.warn("IThome hot board request failed: durationMs={}, error={}",elapsed(started),e.toString());throw new IllegalStateException("IThome hot board request failed",e);} }
    List<CollectedHotItem> parse(String html){List<CollectedHotItem> out=new ArrayList<>(); int rank=1; for(Element li:Jsoup.parse(html).select("#list > div.fl > ul > li")){Element a=li.selectFirst("a.t"), time=li.selectFirst("i"); if(a==null||time==null)continue; String href=a.attr("href"), title=a.text().trim(); if(title.isBlank()||!isHttp(href)||href.contains("lapin.ithome.com")||title.matches(".*(神券|优惠|补贴|京东).*"))continue; LocalDateTime published=parseTime(time.text()); out.add(new CollectedHotItem("ithome-"+href, title, null, source(), categoryClassifier.classify(title,null),0L,"榜单第 "+rank+" 位",rank++,href,null,published)); if(out.size()>=limit)break;} if(out.isEmpty())throw new IllegalStateException("IThome hot board contains no usable items"); return List.copyOf(out);}
    private boolean isHttp(String v){try{URI u=URI.create(v);return ("http".equalsIgnoreCase(u.getScheme())||"https".equalsIgnoreCase(u.getScheme()))&&u.getHost()!=null;}catch(Exception e){return false;}}
    private LocalDateTime parseTime(String v){try{return LocalDateTime.parse(v.trim(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));}catch(Exception e){return null;}}
    private long elapsed(long s){return(System.nanoTime()-s)/1_000_000;}
}
