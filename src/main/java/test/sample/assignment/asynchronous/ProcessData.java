package test.sample.assignment.asynchronous;

import java.util.concurrent.BlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import test.sample.assignment.components.Details;
import test.sample.assignment.db.RequestDB;

public class ProcessData implements Runnable {
	private final BlockingQueue<String> queue;
	private String requestId;
	private RequestDB requestDB;

	public ProcessData(String requestId, RequestDB requestDb, BlockingQueue<String> queue) {
		this.queue = queue;
		this.requestDB = requestDb;
		this.requestId = requestId;
	}

	@Override
	public void run() {
		try {
			System.out.println("Start " + Thread.currentThread().getName());
			System.out.println("Queue size " + queue.size());
			String value = queue.take();
			while (!value.equals("*") && !value.equals("**")) {
				Document internalLinksDoc = Jsoup.connect(value).get();
				Details d = new Details();
				d.setPageTitle(internalLinksDoc.title());
				d.setPageLink(value);
				d.setImage_count(internalLinksDoc.getElementsByTag("img").size());
				requestDB.putLinkDetail(requestId, d);
				value = queue.take();
			}
			if (value.equals("**")) {
				System.out.println("Set processing to false. Data Processed");
				requestDB.request.get(requestId).setIsProcessing(false);
			}
			System.out.println("Completed " + Thread.currentThread().getName());
		} catch (Exception e) {
			System.out.println(Thread.currentThread().getName() + " " + e.getMessage());
		}

	}

}
