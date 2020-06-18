package org.infinispan.wfink.standalone.partition.handling;

import java.io.Console;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.health.ClusterHealth;
import org.infinispan.manager.DefaultCacheManager;

/**
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class ClusteredCache {
  private static final Level[] LEVELS = new Level[] { Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL, Level.ALL, Level.ALL };
  private static final String msgEnterKey = "Enter key: ";

  private final Console con;
  private final DefaultCacheManager cacheManager;
  private Cache<String, String> cache;

  public ClusteredCache(Console con, int logLevel, boolean consoleLog, String logFile) throws IOException {
    this.con = con;

    setLogging(logLevel, consoleLog, logFile);
    cacheManager = new DefaultCacheManager("standalone-config.xml", true);
    cache = cacheManager.getCache();
  }

  /**
   * Initialize standard java util logging. For parameters see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax">Formatter</a>
   *
   * @param logLevel
   * @param consoleLog
   * @param logFile
   * @throws IOException
   */
  private static void setLogging(int logLevel, boolean consoleLog, String logFile) throws IOException {
    SimpleFormatter sf = new SimpleFormatter() {
      @Override
      public synchronized String format(LogRecord lr) {
        return String.format("%1$tH:%1$tM:%1$tS.%1$tL %2$-4s [%3$s] (%5$s) %4$s%n", new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getSourceClassName(), lr.getMessage(), lr.getThreadID());
      }
    };

    Logger root = Logger.getLogger("");

    if (consoleLog) {
      root.getHandlers()[0].setFormatter(sf);
      if (logFile == null) // set console logging to show ALL
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
    } else {
      root.removeHandler(root.getHandlers()[0]);
    }
    if (logFile != null) {
      FileHandler fh = new FileHandler(logFile);
      fh.setLevel(Level.ALL);
      fh.setFormatter(sf);
      root.addHandler(fh);
    }

    // xxxx? example classes
    Logger.getLogger("org.infinispan.wfink").setLevel(LEVELS[logLevel % 10]);
    // xxx?x topology and conflict messages
    Logger.getLogger("org.infinispan.topology").setLevel(LEVELS[(logLevel % 100) / 10]);
    Logger.getLogger("org.infinispan.partitionhandling").setLevel(LEVELS[(logLevel % 100) / 10]);
    Logger.getLogger("org.infinispan.conflict").setLevel(LEVELS[(logLevel % 100) / 10]);
    // xx?xx example classes
    Logger.getLogger("org.infinispan").setLevel(LEVELS[(logLevel % 1000) / 100]);
    // x?xxx JGroups
    Logger.getLogger("org.jgroups").setLevel(LEVELS[(logLevel % 10000) / 1000]);
  }

  public void add() {
    String key = con.readLine(msgEnterKey);
    String value = con.readLine("Enter value: ");

    String oldValue = cache.put(key, value);

    if (oldValue != null) {
      con.printf("   Replace old value : %s\n", oldValue);
    }
  }

  public void generateEntries() {
    int start = 0;
    int num = 0;

    try {
      start = Integer.parseInt(con.readLine("Start number : "));
      num = Integer.parseInt(con.readLine("Number of enries : "));
    } catch (NumberFormatException e) {
      con.printf("Is not a number!\n");
    }

    for (int i = start; i < start + num; i++) {
      cache.put(String.valueOf(i), "Value#" + i);
    }
  }

  public void checkGenerateEntries() {
    int start = 0;
    int num = 0;

    try {
      start = Integer.parseInt(con.readLine("Start number : "));
      num = Integer.parseInt(con.readLine("Number of enries : "));
    } catch (NumberFormatException e) {
      con.printf("Is not a number!\n");
    }

    int missing = 0, wrong = 0;
    for (int i = start; i < start + num; i++) {
      String key = String.valueOf(i);
      String value = cache.get(key);
      if (value == null) {
        con.printf("Entry for key=%s not found!\n", key);
        missing++;
      } else if (!("Value#" + i).equals(value)) {
        con.printf("Value for key=%s wrong : %s!\n", key, value);
        wrong++;
      }
    }
    if (missing != 0)
      con.printf(" %d entries not found\n", missing);
    if (missing != 0)
      con.printf(" %d entries with unexpected value\n", wrong);
  }

  public void get() {
    String key = con.readLine(msgEnterKey);

    if (cache.containsKey(key)) {
      con.printf("  value : %s\n", cache.get(key));
    } else {
      con.printf("   No entry for key found!\n");
    }
  }

  public void getWithMeta() {
    String key = con.readLine(msgEnterKey);

    if (cache.containsKey(key)) {
      printEntryWithMeta(key);
    } else {
      con.printf("   No entry for key found!\n");
    }
  }

  private void printEntryWithMeta(String key) {
    CacheEntry<String, String> entry = cache.getAdvancedCache().getCacheEntry(key);
    con.printf("  Entry  %s : %s\n", key, entry.getValue());
    con.printf("  META    lifespan:%d  maxIdle:%d\n", entry.getLifespan(), entry.getMaxIdle());
    con.printf("  META    created:%s lastUsed:%s\n", (entry.getCreated() > 0 ? new Date(entry.getCreated()) : "--"), (entry.getLastUsed() > 0 ? new Date(entry.getLastUsed()) : "--"));
  }

  public void remove() {
    String key = con.readLine(msgEnterKey);

    if (cache.containsKey(key)) {
      cache.remove(key);
    } else {
      con.printf("   No entry for key found!\n");
    }
  }

  public void list() {
    for (String key : cache.keySet()) {
      con.printf("  Entry  %s : %s\n", key, cache.get(key));
    }
  }

  public void size(final boolean local) {
    if (local) {
      con.printf("  Cache size (local) is %d\n", cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).size());
    } else {
      con.printf("  Cache size is %d\n", cache.size());
    }
  }

  public void health() {
    ClusterHealth clusterHealth = cache.getCacheManager().getHealth().getClusterHealth();
    con.printf("  Cluster %s %s  nodes (%d) %s \n\n", clusterHealth.getClusterName(), clusterHealth.getHealthStatus(), clusterHealth.getNumberOfNodes(), clusterHealth.getNodeNames());
  }

  public void stop() {
    cacheManager.stop();
  }

  private void inputLoop() {
    while (true) {
      String action = con.readLine(">");
      if ("put".equals(action)) {
        add();
      } else if ("rm".equals(action)) {
        remove();
      } else if ("getM".equals(action)) {
        getWithMeta();
      } else if ("get".equals(action)) {
        get();
      } else if ("list".equals(action)) {
        list();
      } else if ("sizeL".equals(action)) {
        size(true);
      } else if ("size".equals(action)) {
        size(false);
      } else if ("gen".equals(action)) {
        generateEntries();
      } else if ("chk".equals(action)) {
        checkGenerateEntries();
      } else if ("health".equals(action)) {
        health();
      } else if ("help".equals(action)) {
        printConsoleHelp();
      } else if ("q".equals(action)) {
        break;
      }
    }
  }

  private void printConsoleHelp() {
    con.printf("Choose:\n" + "============= \n" + "put  -  add/update an entry\n" + "rm   -  remove an entry\n" + "get  -  print a value for key\n"
        + "getM -  print entry with Metadata\n"
        + "list -  list all entries which are store local\n"
        + "size -  size of cache\n"
        + "sizeL-  local size of cache\n"
        + "gen  -  generate # of entries start with a given number\n"
        + "chk  -  check the generated # of entries start with a given number\n"
        + "health -  check the generated # of entries start with a given number\n"
        + "h    -  help\n"
        + "q    -  quit\n");
  }

  public static void main(String[] args) throws IOException {
    Console con = System.console();
    int logLevel = 99;
    String logFile = null;
    boolean consoleLog = true;

    int argc = 0;
    while (argc < args.length) {
      if (args[argc].equals("-L")) {
        argc++;
        logLevel = Integer.parseInt(args[argc]);
        argc++;
      } else if (args[argc].equals("-C")) {
        argc++;
        consoleLog = false;
      } else if (args[argc].equals("-F")) {
        argc++;
        logFile = args[argc];
        argc++;
      } else {
        con.printf("option '%s' unknown\n", args[argc]);
        System.exit(1);
      }
    }

    ClusteredCache main = new ClusteredCache(con, logLevel, consoleLog, logFile);
    main.printConsoleHelp();
    main.inputLoop();
    main.stop();
  }

  public static void mainY(String[] args) {
    System.out.println("Start");
    org.infinispan.configuration.cache.Configuration config = new ConfigurationBuilder()
        .clustering().cacheMode(CacheMode.DIST_SYNC).sync().l1()
        .build();
    DefaultCacheManager cacheManager = new DefaultCacheManager(
        new GlobalConfigurationBuilder().transport().defaultTransport()
        .build(), config);
    Cache<String, Long> cache = cacheManager.getCache();
    System.out.println("Cache created : " + cache);
    cacheManager.stop();
    System.out.println("Exit");
  }
}
