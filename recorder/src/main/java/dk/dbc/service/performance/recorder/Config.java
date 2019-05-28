/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of performance-test-recorder
 *
 * performance-test-recorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * performance-test-recorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.service.performance.recorder;

import dk.dbc.Arguments;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.Locale;

/**
 * Parameters as supplied on the command line
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public final class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private static Options options() {
        Options options = new Options();

        options.addOption(Option.builder("s")
                .longOpt("sort-buffer")
                .hasArg()
                .argName("NUM")
                .desc("Size of cache to mitigate out-of-order lines in input, at least 1")
                .build());

        options.addOption(Option.builder("d")
                .longOpt("duration")
                .hasArg()
                .argName("DURATION")
                .desc("Run duration ie. 15s or 3h (default: 1h)")
                .build());

        options.addOption(Option.builder("l")
                .longOpt("limit")
                .hasArg()
                .argName("NUM")
                .desc("Max number of lines in output")
                .build());

        options.addOption(Option.builder("k")
                .longOpt("kafka")
                .hasArg()
                .argName("URL")
                .desc("Connect url (host[:port][,host:port]/topic)")
                .build());

        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .argName("FILE")
                .desc("File to read log lines from")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("FILE")
                .desc("File to put parsed queries into")
                .build());

        options.addOption(Option.builder("a")
                .longOpt("application")
                .hasArg()
                .argName("NAME")
                .desc("name of application in log")
                .build());

        options.addOption(Option.builder("A")
                .longOpt("append")
                .desc("if output is given, append or overwrite")
                .build());

        return options;
    }

    private static final String FOOTER =
            String.join("\n",
                        "* -i/-k are mutually exclusive",
                        "* The program only terminates after 1st log-line after DURATION,",
                        "  if log-lines are sparse it could run for a long time",
                        "",
                        "Copyright (C) 2019 DBC A/S (http://dbc.dk/)");

    private final int sortBufferSize;
    private final long duration;
    private final long limit;
    private final String kafka;
    private final String input;
    private final String output;
    private final String application;
    private boolean append;

    /**
     * Construct a configuration from (main) args
     *
     * @param args argument list as supplied from main
     * @return configuration
     */
    public static Config of(String... args) {
        return Arguments.parse(options(), FOOTER, Config::new, args);
    }

    public int getSortBufferSize() {
        return sortBufferSize;
    }

    public long getDuration() {
        return duration;
    }

    public long getLimit() {
        return limit;
    }

    public String getKafka() {
        return kafka;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public String getApplication() {
        return application;
    }

    public boolean isAppend() {
        return append;
    }

    private Config(Arguments args, Iterator<String> positionalArguments) throws ParseException {
        if (positionalArguments.hasNext())
            throw new ParseException("Unexpected positional argument(s) at: " + positionalArguments.next());
        this.sortBufferSize = args.take("s", "500", t -> {
                                    int value = Integer.parseInt(t);
                                    if (value < 0)
                                        throw new RuntimeException("sort buffer needs to be atleast 0");
                                    return value;
                                });
        this.duration = args.take("d", "1h", t -> {
                              String[] parts = t.split("(?=[^0-9])", 2);
                              if (parts.length != 2)
                                  throw new RuntimeException();
                              long number = Long.parseUnsignedLong(parts[0]);
                              if (number < 1)
                                  throw new RuntimeException();
                              switch (parts[1].toLowerCase(Locale.ROOT)) {
                                  case "s":
                                      return Duration.ofSeconds(number).toMillis();
                                  case "m":
                                      return Duration.ofMinutes(number).toMillis();
                                  case "h":
                                      return Duration.ofHours(number).toMillis();
                                  case "d":
                                      return Duration.ofDays(number).toMillis();
                                  default:
                                      throw new RuntimeException();
                              }
                          });
        this.kafka = args.take("k", null, t -> t);
        this.input = args.take("i", null, t -> t);
        switch (countNotNull(this.kafka, this.input)) {
            case 0:
                log.debug("Using input from stdin");
                break;
            case 1:
                log.debug("Using input from commandline");
                break;
            default:
                throw new ParseException("-i/-k are mutually exclusive");
        }
        this.output = args.take("o", null, t -> t);
        this.append = args.isSet("A");
        if (append && output == null)
            throw new ParseException("-A,--append, does not makle sense without -o,--output");

        this.limit = args.take("l", String.valueOf(Long.MAX_VALUE), t -> {
                           long value = Long.parseLong(t);
                           if (value < 1)
                               throw new RuntimeException("number of lines needs to be atleast 1");
                           return value;
                       });
        this.application = args.take("a", null, t -> t);
    }

    private static int countNotNull(Object... objs) {
        int i = 0;
        for (Object obj : objs) {
            if (obj != null)
                i++;
        }
        return i;
    }

    @Override
    public String toString() {
        return "Config{" + "sortBufferSize=" + sortBufferSize + ", duration=" + duration + ", limit=" + limit + ", kafka=" + kafka + ", input=" + input + ", output=" + output + ", application=" + application + ", append=" + append + '}';
    }

}
