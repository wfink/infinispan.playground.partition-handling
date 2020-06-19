/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infinispan.wfink.ph.dist;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.partitionhandling.AvailabilityException;
import org.infinispan.wfink.ph.CacheView;

/**
 * A simple JSF controller to show how the EJB invocation on different servers.
 *
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Model
public class DistCacheController {
  private CacheView cacheView;

  @Inject @DistPH
  private DefaultCacheManager manager;

  /**
   * Initialize the controller.
   */
  @PostConstruct
  public void init() {
    initForm();
    size();
  }

  private Cache<String, String> getEmbeddedCache() {
    return manager.getCache("DistPartitionHandlingCache");
  }

  //@PreDestroy
  public void cleanUp() {
    manager.stop();
    manager = null;
  }

  public void initForm() {
    this.cacheView = new CacheView();
  }

  @Produces
  @Named
  public CacheView getDistView() {
    return this.cacheView;
  }

  public void add() {
    getEmbeddedCache().put(cacheView.getKey(), cacheView.getValue());
    size();
  }

  public void get() {
    String value = getEmbeddedCache().get(cacheView.getKey());
    if(value==null) {
      cacheView.setValue("NOT AVAILABLE");
    }else{
      cacheView.setValue(value);
    }
  }

  public void delete() {
    getEmbeddedCache().remove(cacheView.getKey());
    size();
  }

  public void list(boolean avail) {
    StringBuilder available = new StringBuilder();
    StringBuilder notAvailable = new StringBuilder();
    int availableS = 0, notAvailableS = 0;
    // read cache
    Cache<String, String> cache = getEmbeddedCache();
    for (String key : cache.keySet()) {
      try {
        String value =  cache.get(key);
        available.append(key + "  :  ");
        available.append(value);
        available.append(" || ");
        availableS++;
      } catch (AvailabilityException e) {
        notAvailable.append(key + "  ||  ");
        notAvailableS++;
      }
    }
    cacheView.setSize("" + availableS);
    cacheView.setUnavailableSize(notAvailableS);
    cacheView.setEntries(avail ? available.toString() : notAvailable.toString());
  }

  public void size() {
    Cache<String, String> cache = getEmbeddedCache();
    cacheView.setSize("" + cache.size());
    cacheView.setAvailability(cache.getAdvancedCache().getAvailability());
  }

  public void generateEntries() {
    Cache<String, String> cache = getEmbeddedCache();
    int start = cacheView.getGenStartKey();
    int numOfEntries = cacheView.getGenSize();

    for (int i = start; i < start + numOfEntries; i++) {
      cache.put(String.valueOf(i),"generated #" + i);
    }
    initForm();
  }
}
