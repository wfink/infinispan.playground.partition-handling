package org.infinispan.wfink.standalone.partition.handling;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.infinispan.conflict.EntryMergePolicy;
import org.infinispan.container.entries.CacheEntry;

/**
 * A custom merge policy which check the creation date to decide which entry to choose.
 * <p><b>The related cache must be configured for expiration with lifespan</b> if entries are found without a creation date, which is the cause if the lifespan is not set
 * a IllegalStateException is thrown</p>
 * <p>Consider that the creation timestamp is the time when created on that node, so if differ for a few millis it will be a replication issue</p>
 * <p>Limitation is that a remove can not be detected as there is no hint if an entry is removed and when</p>
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class LastCreatedMergePolicy implements EntryMergePolicy<String, String> {
	private final static Logger log = Logger.getLogger(LastCreatedMergePolicy.class.getName());

	@Override
	public CacheEntry<String, String> merge(CacheEntry<String, String> preferredEntry, List<CacheEntry<String, String>> otherEntries) {
		CacheEntry<String, String> solved = preferredEntry;

		if(preferredEntry == null) {
			log.fine("preferedEntry is NULL");
		}else{
			log.fine("preferedEntry key=" + preferredEntry.getKey() + ", value="+preferredEntry.getValue() + ", created=" + (preferredEntry.getCreated()>0 ? new Date(preferredEntry.getCreated()):"--"));
			if(preferredEntry.getMetadata().lifespan() == 0) {
				throw new IllegalStateException("Cache/Entry is not configured for expiration, Metadata not available to use LastCreateMergePolicy. Please enable lifespan expiration!");
			}
		}
		log.fine("otherEntries size="+otherEntries.size());

		for (CacheEntry<String, String> entry : otherEntries) {
			log.fine("otherEntry key=" + entry.getKey() + ", value="+entry.getValue() + ", created=" + (entry.getCreated()>0 ? new Date(entry.getCreated()):"--"));
			if(entry.getCreated()==0) {
				throw new IllegalStateException("Cache/Entry key=" + entry.getKey() + ",is not configured for expiration, Metadata not available to use LastCreateMergePolicy. Please enable lifespan expiration!");
			}
			if(solved == null) {
				// can happen for the first iteration if there is no preferred
				log.fine("Conflict merge: prefered was NULL use other  Key=" + entry.getKey() + ", other value=" + entry.getValue());
				solved = entry;
			}else if(entry.getCreated() < solved.getCreated()) {
				// other entry is older, keep the current
				log.fine("Conflict merge: other entry is older  Key=" + entry.getKey() + ",current value=" + solved.getValue() + ",other value=" + entry.getValue());
			}else if(entry.getCreated() > solved.getCreated()) {
				log.fine("Conflict merge: other entry is newer  Key=" + entry.getKey() + ",current value=" + solved.getValue() + ",other value=" + entry.getValue());
				solved = entry;
			}else{
				//creation time is the same, check value and warn if not the same!
				if(entry.getValue().equals(solved.getValue())) {
					log.fine("Conflict merge: current and other are the same");
				}else{
					log.warning("Conflict merge: same creation date but diffenrent  Key=" + entry.getKey() + ",current value=" + solved.getValue() + ",other value=" + entry.getValue());
				}
			}
		}
		if(solved == null) {
			log.warning("Conflict merge: does not return an entry");
		}else{
			log.info("Conflict merge: return  key=" + solved.getKey() + ", value=" + solved.getValue() + ", created=" + (solved.getCreated()>0 ? new Date(solved.getCreated()):"--"));
		}
		return solved;
	}
}