package test.sample.assignment.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import test.sample.assignment.components.Details;
import test.sample.assignment.components.ResponseBean;

@Component
public class RequestDB {

	public static final Map<String, ResponseBean> request = new LinkedHashMap<String, ResponseBean>();

	public void putLinkDetail(String requestId, Details details) {
		synchronized (request.get(requestId)) {
			if (details != null) {
				List<Details> detailsList = request.get(requestId).getDetails();
				detailsList.add(details);
			}
		}

	}

}
