package test.sample.assignment.asynchronous;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;

import test.sample.assignment.components.ResponseBean;
import test.sample.assignment.db.RequestDB;

public class InsertData implements Runnable {

	private final BlockingQueue<String> queue;
	private Integer depth;
	private final Set<String> linksSet;
	private Integer imagesCount;
	private String URL;
	private String requestId;
	private RequestDB requestDB;

	public InsertData(Set<String> linksSet, String url, Integer depth, String requestId, RequestDB requestDb) {
		queue = new LinkedBlockingQueue<String>();
		this.depth = depth;
		this.imagesCount = 0;
		this.URL = url;
		this.linksSet = linksSet;
		this.requestDB = requestDb;
		this.requestId = requestId;
	}

	@Override
	public void run() {
		try {
			ResponseBean responseBean = requestDB.request.get(requestId);
			getPageLinks(URL, depth);
			responseBean.setTotalLinks(linksSet.size() - 1);
			responseBean.setTotalImages(imagesCount);
			queue.add("**");
			Thread child3 = new Thread(new ProcessData(requestId, requestDB, queue));
			child3.start();
			System.out.println("Thread child3 started to complete process");

		} catch (IOException e) {
			System.err.println("For '" + URL + "': " + e.getMessage());
		}

	}

	public void getPageLinks(String URL, Integer depth) throws JsonProcessingException {

		if (!URL.isEmpty())
			if (!linksSet.contains(URL)) {
				linksSet.add(URL);
				while (depth-- > 0) {
					System.out.println("Queue size " + queue.size());
					System.out.println(">> Depth: " + depth + " [" + URL + "]");
					try {
						Document internalLinksDoc = Jsoup.connect(URL).get();
						Elements linksOnPage = internalLinksDoc.select("a[href]");
						Elements images = internalLinksDoc.getElementsByTag("img");
						Integer count = (int) images.stream().map(img -> img.attr("abs:src")).filter(Objects::nonNull)
								.count();
						imagesCount += count;
						for (Element page : linksOnPage) {
							if (!linksSet.contains(page.attr("abs:href"))) {
								System.out.println("adding url " + page.attr("abs:href"));
								linksSet.add(page.attr("abs:href"));
								queue.add(page.attr("abs:href"));
							}
						}
						queue.add("*");
						queue.add("*");
						System.out.println("Queue size " + queue.size());
						// start multiple threads here thread and pass the queue
						// to process
						Thread child1 = new Thread(new ProcessData(requestId, requestDB, queue));
						child1.start();
						System.out.println("Thread child1 started");

						Thread child2 = new Thread(new ProcessData(requestId, requestDB, queue));
						child2.start();
						System.out.println("Thread child2 started");
					} catch (IOException e) {
						System.err.println("For '" + URL + "': " + e.getMessage());
					}

				}
			}
	}

}
