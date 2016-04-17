package monitoring.utils;

import java.util.List;

import monitoring.model.HealthData;

import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HealthDataRepository extends CouchDbRepositorySupport<HealthData> {

	@Autowired
	public HealthDataRepository(CouchDbConnector db) {
		super(HealthData.class, db);
		initStandardDesignDocument();
	}

	@GenerateView
	@Override
	public List<HealthData> getAll() {
		//ViewQuery query = createQuery("all").descending(true);
		ViewQuery query1 = new ViewQuery().allDocs().includeDocs(true);
		db.queryView(query1);
		
		return db.queryView(query1, HealthData.class);
	}
	
	public String getTest() {
		
		List<String> test = db.getAllDocIds();
		return test.get(0);
	}

	@GenerateView
	public List<HealthData> findByType(String type) {
		
		//ViewQuery query1 = new ViewQuery().limit(20).viewName("by_type").designDocId("_design/HealthData");
		//db.queryView(query1);
		//return db.queryView(query1, HealthData.class);
			
		return queryView("by_type", type);
	}
}
