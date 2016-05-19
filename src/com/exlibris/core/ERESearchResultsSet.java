package com.exlibris.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ERESearchResultsSet {
	static Log log = LogFactory.getLog(ERESearchResultsSet.class);

	private List<SRWRecord> resultsObjects = new ArrayList<SRWRecord>();
	int numberOfRecords = 0;
	boolean hasMore = false;
	private ParseSRWResponse srwParser = null;

	public ERESearchResultsSet(String  input) throws EREException {
		srwParser = new ParseSRWResponse(input);
		setResults();
	}

	private void setResults ( ){
		if (srwParser != null ){
			resultsObjects = srwParser.getResultsObjects();
			numberOfRecords = srwParser.getNumberOfRecords();
			hasMore = (srwParser.getNumberOfRecords() - srwParser.getRecordsRetrived()) > 0;
		}
	}

	public List<SRWRecord> getResultsObjects() {
		return resultsObjects;
	}

	public void setResultsObjects(List<SRWRecord> resultsObjects) {
		this.resultsObjects = resultsObjects;
	}

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public int getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(int numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}
}
