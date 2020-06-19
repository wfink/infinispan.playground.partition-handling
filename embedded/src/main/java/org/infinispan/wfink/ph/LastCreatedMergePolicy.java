package org.infinispan.wfink.ph;

import java.util.List;
import java.util.logging.Logger;

import org.infinispan.conflict.EntryMergePolicy;
import org.infinispan.container.entries.CacheEntry;

public class LastCreatedMergePolicy implements EntryMergePolicy<String, String> {
	private final static Logger log = Logger.getLogger(LastCreatedMergePolicy.class.getName());

	@Override
	public CacheEntry<String, String> merge(CacheEntry<String, String> preferredEntry, List<CacheEntry<String, String>> otherEntries) {
		CacheEntry<String, String> solved = preferredEntry;

		if(preferredEntry.getMetadata().lifespan() == 0) {
			throw new IllegalStateException("Cache is not configured for expiration, Metadata not available to use LastCreateMergePolicy. Please enable expiration!");
		}

		for (CacheEntry<String, String> entry : otherEntries) {
			if(entry.getCreated() < solved.getCreated()) {
				log.info("Conflict merge: other entry is older  Key:" + entry.getKey() + "Value current:" + solved.getValue() + " other:" + entry.getValue());
			}else if(entry.getCreated() > solved.getCreated()) {
				log.info("Conflict merge: other entry is newer  Key:" + entry.getKey() + "Value current:" + solved.getValue() + " other:" + entry.getValue());
				solved = entry;
			}else{
				if(entry.getValue().equals(solved.getValue())) {
					log.info("Conflict merge: current and other are the same");
				}else{
					log.warning("Conflict merge: same creation date but diffenrent  Key:" + entry.getKey() + "Value current:" + solved.getValue() + " other:" + entry.getValue());
				}
			}
		}
		return solved;
	}
}