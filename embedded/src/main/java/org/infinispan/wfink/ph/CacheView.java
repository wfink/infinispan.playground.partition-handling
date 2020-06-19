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
package org.infinispan.wfink.ph;

import org.infinispan.partitionhandling.AvailabilityMode;

/**
 * A container for the result of an invocation. It will be used by the JsfController.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf Dieter Fink</a>
 */
public class CacheView {

  private String key;
  private String value;
  private String size;
  private int genSize = 10;
  private int genStartKey = 1;
  private int unavailableSize;
  private String entries;
  private AvailabilityMode mode;

  public String getEntries() {
    return entries;
  }

  public void setEntries(String entries) {
    this.entries = entries;
  }

  public int getGenSize() {
    return genSize;
  }

  public void setGenSize(int genSize) {
    this.genSize = genSize;
  }

  public int getGenStartKey() {
    return genStartKey;
  }

  public void setGenStartKey(int genStartKey) {
    this.genStartKey = genStartKey;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String value) {
    this.size = value;
  }

  public int getUnavailableSize() {
    return unavailableSize;
  }

  public void setUnavailableSize(int unavail) {
    this.unavailableSize = unavail;
  }

  public void setAvailability(AvailabilityMode availability) {
    this.mode = availability;
  }

  public AvailabilityMode getAvailability() {
    return this.mode;
  }

}
