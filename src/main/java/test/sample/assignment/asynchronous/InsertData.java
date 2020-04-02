package test.sample.assignment.asynchronous;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;

import test.sample.assignment.components.Details;
import test.sample.assignment.components.ResponseBean;
import test.sample.assignment.db.RequestDB;

public class InsertData implements Runnable {

	// private final BlockingQueue<String> queue;
	private Integer depth;
	private final Set<String> linksSet;
	private String URL;
	private String requestId;
	private RequestDB requestDB;

	public InsertData(/* BlockingQueue<String> q, */ Set<String> linksSet, String url, Integer depth, String requestId,
			RequestDB requestDb) {
		// queue = q;
		this.depth = depth;
		this.URL = url;
		this.linksSet = linksSet;
		this.requestDB = requestDb;
		this.requestId = requestId;
	}

	@Override
	public void run() {
		try {
			ResponseBean responseBean = requestDB.request.get(requestId);
			Document document = Jsoup.connect(URL).get();
			Elements links = document.select("a[href]");
			Elements images = document.getElementsByTag("img");
			responseBean.setTotalLinks(links.size());
			Long count = images.stream().map(img -> img.attr("abs:src")).filter(Objects::nonNull).count();
			responseBean.setTotalImages(count.intValue());
			List<Details> detailsList = responseBean.getDetails();
			getPageLinks(URL, depth, linksSet, detailsList);

			responseBean.setIsProcessing(false);

		} catch (IOException e) {
			System.err.println("For '" + URL + "': " + e.getMessage());
		}

	}

	public void getPageLinks(String URL, Integer depth, Set<String> links, List<Details> detailsList)
			throws JsonProcessingException {

		if (!URL.isEmpty())
			if ((!links.contains(URL) && (depth-- > 0))) {
				System.out.println(">> Depth: " + depth + " [" + URL + "]");
				try {
					links.add(URL);

					Document internalLinksDoc = Jsoup.connect(URL).get();
					Elements linksOnPage = internalLinksDoc.select("a[href]");

					Details d = new Details();
					d.setPageTitle(internalLinksDoc.title());
					d.setPageLink(URL);
					d.setImage_count(internalLinksDoc.getElementsByTag("img").size());
					detailsList.add(d);

					depth++;
					for (Element page : linksOnPage) {
						getPageLinks(page.attr("abs:href"), depth, links, detailsList);
					}
				} catch (IOException e) {
					System.err.println("For '" + URL + "': " + e.getMessage());
				}
			}
	}

}
