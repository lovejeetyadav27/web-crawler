package test.sample.assignment.service;

import test.sample.assignment.components.ResponseBean;

public interface CrawlService {

    public String getCrawlData(String url,Integer depth);

	public ResponseBean getCrawlDataByRequestId(String requestId);

}
