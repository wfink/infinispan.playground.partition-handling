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
package org.infinispan.wfink.ph.repl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.infinispan.manager.DefaultCacheManager;

/**
 * Creates a DefaultCacheManager which is configured with a configuration file.
 * <b>Infinispan's libraries need to be provided as module with dependency, also bundling with the application is possible.</b>
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
// AppScope necessary to provide the correct lifecycle for the cache!
@ApplicationScoped
public class PHReplCacheManagerProvider {
   private static final String CONFIG = "replPHcache-config.xml";
   private static final Logger log = Logger.getLogger(PHReplCacheManagerProvider.class.getName());
   private DefaultCacheManager manager;

   /**
    * Always return an interface, not an implementation.
    * Otherwise CDI might handle the object incorrect and create an additional one which can confuse the cluster-view!
    * 
    * @return
    */
   @Produces @ReplPH
   public DefaultCacheManager getCacheManager() {
      if (manager == null) {
         log.info("construct a CacheManager");

         try {
            manager = new DefaultCacheManager(CONFIG, true);
         } catch (IOException e) {
            log.log(Level.SEVERE, "Could not read " + CONFIG + " to create the CacheManager", e);
         }
      }
      return manager;
   }

   /**
    * Destroy the manager to correct remove this from the cluster-view if the application is stopped 
    */
   @PreDestroy
   public void cleanUp() {
      log.info("PreDestroy");
      manager.stop();
      log.info("cacheManager.stop()");
      manager = null;
   }

}
