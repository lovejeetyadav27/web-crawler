package test.sample.assignment.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import test.sample.assignment.components.ResponseBean;
import test.sample.assignment.service.CrawlService;

@RestController
public class CrawlController {

	@Autowired
	CrawlService crawlService;

	private static Logger LOGGER = LoggerFactory.getLogger(CrawlController.class.getName());

	@PostMapping(value = "/crawl", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> crawl(@RequestParam(required = true) String url,
			@RequestParam(required = true) Integer depth) {
		String requestId = crawlService.getCrawlData(url, depth);
		Map<String, String> map = new HashMap<String, String>();
		map.put("requestId", requestId);
		return map;
	}

	@GetMapping(value = "/crawl", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getCrawlData(@RequestParam(required = true) String requestId)
			throws JsonProcessingException {
		ResponseBean responseBean = null;
		responseBean = crawlService.getCrawlDataByRequestId(requestId);
		Map<String, Object> map = new HashMap<>();
		if (responseBean == null) {
			map.put("message", "invalid request Id");
			return map;
		}
		if (responseBean.getIsProcessing())
			map.put("message", "url is under processing");
		else
			map.put("message", "processing completed");
		map.put("data", responseBean);
		return map;
	}

}
