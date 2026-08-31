package com.boilingpoint.news.collector;

import com.boilingpoint.news.common.enums.HotSource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.IOException; import java.net.URI; import java.net.http.HttpClient; import java.net.http.HttpRequest; import java.net.http.HttpResponse; import java.nio.charset.StandardCharsets; import java.time.Duration; import java.util.ArrayList; import java.util.List;

@Slf4j @Component @ConditionalOnProperty(name="app.collector.kr36.enabled",havingValue="true",matchIfMissing=true)
public class Kr36HotSearchCollector implements HotSearchCollector {
    private static final String UA="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 Chrome/140.0.0.0 Safari/537.36 HoloView/0.1"; private final BaiduCategoryClassifier classifier; private final HttpClient client; private final String url; private final int limit; private final Duration timeout;
    public Kr36HotSearchCollector(BaiduCategoryClassifier c,@Value("${app.collector.kr36.url:https://www.36kr.com/newsflashes}")String u,@Value("${app.collector.kr36.limit:30}")int l,@Value("${app.collector.kr36.timeout:10s}")Duration t){classifier=c;url=u;limit=Math.max(1,Math.min(50,l));timeout=t;client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL).build();}
    @Override public HotSource source(){return HotSource.KR36;}
    @Override public List<CollectedHotItem> collect(){long s=System.nanoTime();try{var r=client.send(HttpRequest.newBuilder(URI.create(url)).timeout(timeout).header("User-Agent",UA).header("Accept","text/html,application/xhtml+xml").GET().build(),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));if(r.statusCode()!=200)throw new IllegalStateException("36Kr hot board returned HTTP "+r.statusCode());var o=parse(r.body());log.info("36Kr hot board collected: itemCount={}, responseBytes={}, durationMs={}",o.size(),r.body().getBytes(StandardCharsets.UTF_8).length,elapsed(s));return o;}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("36Kr hot board request interrupted",e);}catch(IOException e){log.warn("36Kr hot board request failed: durationMs={}, error={}",elapsed(s),e.toString());throw new IllegalStateException("36Kr hot board request failed",e);}}
    List<CollectedHotItem> parse(String html){List<CollectedHotItem> o=new ArrayList<>();int rank=1;for(Element box:Jsoup.parse(html).select(".newsflash-item")){Element a=box.selectFirst("a.item-title"),d=box.selectFirst(".item-desc");if(a==null)continue;String href=a.attr("href"),title=a.text().trim();if(title.isBlank()||!href.startsWith("/"))continue;o.add(new CollectedHotItem("kr36-"+href,title,d==null?null:d.text().trim(),source(),classifier.classify(title,d==null?null:d.text()),0L,"榜单第 "+rank+" 位",rank++,"https://www.36kr.com"+href,null,null));if(o.size()>=limit)break;}if(o.isEmpty())throw new IllegalStateException("36Kr hot board contains no usable items");return List.copyOf(o);}
    private long elapsed(long s){return(System.nanoTime()-s)/1_000_000;}
}
