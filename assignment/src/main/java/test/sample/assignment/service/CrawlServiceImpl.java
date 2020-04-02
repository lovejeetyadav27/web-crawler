package test.sample.assignment.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import test.sample.assignment.asynchronous.InsertData;
import test.sample.assignment.components.ResponseBean;
import test.sample.assignment.db.RequestDB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class CrawlServiceImpl implements CrawlService {

	@Autowired
	private RequestDB requestDB;

	private static final String HTTP = "http";

	public void getPageLinks(String URL, Integer depth, Set<String> links) throws JsonProcessingException {

		if (!URL.isEmpty())
			if ((!links.contains(URL) && (depth-- > 0))) {
				System.out.println(">> Depth: " + depth + " [" + URL + "]");
				try {
					links.add(URL);

					Document document = Jsoup.connect(URL).get();
					Elements linksOnPage = document.select("a[href]");

					depth++;
					for (Element page : linksOnPage) {
						getPageLinks(page.attr("abs:href"), depth, links);
					}
				} catch (IOException e) {
					System.err.println("For '" + URL + "': " + e.getMessage());
				}
			}
	}

	@Override
	public String getCrawlData(String url, Integer depth) {

		String requestId = getRequestId();

		ResponseBean responseBean = new ResponseBean();

		requestDB.request.put(requestId, responseBean);

		HashSet<String> links = new HashSet<>();
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

		System.out.println("Crawling Link:" + url);
		if (!url.contains(HTTP))
			url = HTTP + "://" + url;

		crawlURLinThread(/* queue, */links, url, depth, requestId, requestDB);

		return requestId;

	}

	public void crawlURLinThread(/* BlockingQueue<String> queue, */Set<String> links, String url, Integer depth,
			String requestId, RequestDB requestDb) {

		Thread t1 = new Thread(new InsertData(/* queue, */links, url, depth, requestId, requestDb));
		t1.start();
		System.out.println("Thread started");

	}

	public String getRequestId() {
		UUID gfg = UUID.randomUUID();
		return gfg.toString();
	}

	@Override
	public ResponseBean getCrawlDataByRequestId(String requestId) {
		ResponseBean responseBean = requestDB.request.get(requestId);
		return responseBean;
	}
}
