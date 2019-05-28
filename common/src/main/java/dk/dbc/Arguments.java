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
package dk.dbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class Arguments {

    private static final Logger log = LoggerFactory.getLogger(Arguments.class);

    private final Options options;
    private final String footer;
    private CommandLine commandLine;

    @FunctionalInterface
    public interface ConfigReader<T> {

        T of(Arguments args, Iterator<String> positionalArguments) throws ParseException;
    }

    /**
     * Parse arguments
     *
     * @param <T>       Type of configuration
     * @param options   options to parse
     * @param footer    footer text for usage
     * @param converter configuration constructor - (typically Config::new)
     * @param args      arguments to parse (usually from main(String... args)
     * @return result of converter
     */
    public static <T> T parse(Options options, String footer, ConfigReader<T> converter, String... args) {
        return new Arguments(options, footer)
                .parse(args)
                .convertTo(converter);
    }

    private Arguments(Options options, String footer) {
        this.options = options;
        this.footer = footer;
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("This help")
                .build());

        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("Enable debug level logging")
                .build());

        options.addOption(Option.builder()
                .longOpt("version")
                .desc("print version")
                .build());

    }

    private Arguments parse(String... args) {
        try {
            commandLine = new DefaultParser().parse(options, args);
            if (commandLine.hasOption('v'))
                verbose();
            if (commandLine.hasOption('h'))
                throw new ParseException("");
            if (commandLine.hasOption("version")) {
                System.out.println("1.0-SNAPSHOT#${env.BUILD_NUMBER}");
                throw new ExitException(0);
            }
        } catch (ParseException ex) {
            throw usage(ex.getMessage());
        }
        return this;
    }

    private <T> T convertTo(ConfigReader<T> converter) {
        try {
            return converter.of(this, commandLine.getArgList().iterator());
        } catch (ParseException ex) {
            throw usage(ex.getMessage());
        }
    }

    /**
     * Take an argument from the parsed command line
     *
     * @param <T>          type of argument
     * @param arg          name of command line argument
     * @param defaultValue In variable is unset, use this (if null, that is
     *                     returned)
     * @param converter    function to convert to wanted type (for string use: s
     *                     -&gt; s)
     * @return converted value
     * @throws ParseException If a conversion fails
     */
    public <T> T take(String arg, String defaultValue, Function<String, T> converter) throws ParseException {
        try {
            String value = commandLine.getOptionValue(arg, defaultValue);
            if (value == null)
                return null;
            return converter.apply(value);
        } catch (RuntimeException ex) {
            StringBuilder sb = new StringBuilder("Invalid value for ");
            Option option = options.getOption(arg);
            if (option.getOpt() != null) {
                sb.append("-").append(option.getOpt());
                if (option.getLongOpt() != null)
                    sb.append(",--").append(option.getLongOpt());
            } else {
                sb.append("--").append(option.getLongOpt());
            }
            if (ex.getMessage() != null)
                sb.append(" - ").append(ex.getMessage());
            throw new ParseException(sb.toString());
        }
    }

    /**
     * Test if a flag is set
     *
     * @param arg name of flag
     * @return if it was set
     */
    public boolean isSet(String arg) {
        return commandLine.hasOption(arg);
    }

    private ExitException usage(String error) {
        try (OutputStream os = error.isEmpty() ? System.out : System.err ;
                Writer osWriter = new OutputStreamWriter(os, UTF_8) ;
             PrintWriter writer = new PrintWriter(osWriter)) {
            HelpFormatter formatter = new HelpFormatter();
            if (!error.isEmpty()) {
                formatter.printWrapped(writer, 76, error);
                formatter.printWrapped(writer, 76, "");
            }
            String ex = executable();
            formatter.printUsage(writer, 76, ex, options);
            formatter.printWrapped(writer, 76, "");
            formatter.printOptions(writer, 76, options, 4, 4);
            formatter.printWrapped(writer, 76, "");
            if (footer != null) {
                Pattern pattern = Pattern.compile("^", Pattern.MULTILINE);
                String indented = pattern.matcher(footer).replaceAll("    ");
                formatter.printWrapped(writer, 76, indented);
                formatter.printWrapped(writer, 76, "");
            }
        } catch (IOException ex) {
            log.error("Error print usage: {}", ex.getMessage());
            log.debug("Error print usage: ", ex);
        }
        return new ExitException(error.isEmpty() ? 0 : 1);
    }

    private static void verbose() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger("dk.dbc").setLevel(Level.DEBUG);
    }

    private static String executable() {
        try {
            return "java -jar " +
                   new java.io.File(Arguments.class.getProtectionDomain()
                           .getCodeSource()
                           .getLocation()
                           .toURI()
                           .getPath())
                           .getName() +
                   " (1.0-SNAPSHOT)";
        } catch (RuntimeException | URISyntaxException ex) {
            return "executable";
        }
    }
}
