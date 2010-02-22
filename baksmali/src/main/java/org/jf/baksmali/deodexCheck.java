package org.jf.baksmali;

import org.apache.commons.cli.*;
import org.jf.dexlib.Code.Analysis.ClassPath;

public class deodexCheck {
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine;

        Options options = buildOptions();

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            usage(options);
            return;
        }

        String bootClassPath = "core.jar:ext.jar:framework.jar:android.policy.jar:services.jar";
        String bootClassPathDir = ".";
        String deodexerantHost = null;
        int deodexerantPort = 0;
        int classStartIndex = 0;


        String[] remainingArgs = commandLine.getArgs();


        if (commandLine.hasOption("v")) {
            main.version();
            return;
        }

        if (commandLine.hasOption("?")) {
            usage(options);
            return;
        }

        if (remainingArgs.length > 0) {
            usage(options);
            return;
        }


        if (commandLine.hasOption("c")) {
            String bcp = commandLine.getOptionValue("c");
            if (bcp.charAt(0) == ':') {
                bootClassPath = bootClassPath + bcp;
            } else {
                bootClassPath = bcp;
            }
        }

        if (commandLine.hasOption("i")) {
            try {
                classStartIndex = Integer.parseInt(commandLine.getOptionValue("i"));
            } catch (Exception ex) {
            }
        }

        if (commandLine.hasOption("C")) {
            bootClassPathDir = commandLine.getOptionValue("C");
        }

        if (commandLine.hasOption("x")) {
            String deodexerantAddress = commandLine.getOptionValue("x");
            String[] parts = deodexerantAddress.split(":");
            if (parts.length != 2) {
               System.err.println("Invalid deodexerant address. Expecting :<port> or <host>:<port>");
               System.exit(1);
            }

            deodexerantHost = parts[0];
            if (deodexerantHost.length() == 0) {
               deodexerantHost = "localhost";
            }
            try {
               deodexerantPort = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
               System.err.println("Invalid port \"" + deodexerantPort + "\" for deodexerant address");
               System.exit(1);
            }
        }

        ClassPath.InitializeClassPath(bootClassPathDir, bootClassPath==null?null:bootClassPath.split(":"), null);

        ClassPath.validateAgainstDeodexerant(deodexerantHost, deodexerantPort, classStartIndex);
    }

    /**
     * Prints the usage message.
     */
    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.printHelp("java -classpath baksmali.jar deodexCheck -x HOST:PORT [options]",
                "disassembles and/or dumps a dex file", options, "");
    }

    private static Options buildOptions() {
        Options options = new Options();

        Option versionOption = OptionBuilder.withLongOpt("version")
                .withDescription("prints the version then exits")
                .create("v");

        Option helpOption = OptionBuilder.withLongOpt("help")
                .withDescription("prints the help message then exits")
                .create("?");

        Option classPathOption = OptionBuilder.withLongOpt("bootclasspath")
                .withDescription("the bootclasspath jars to use, for analysis. Defaults to " +
                        "core.jar:ext.jar:framework.jar:android.policy.jar:services.jar. If you specify a value that " +
                        "begins with a :, it will be appended to the default bootclasspath")
                .hasOptionalArg()
                .withArgName("BOOTCLASSPATH")
                .create("c");

        Option classPathDirOption = OptionBuilder.withLongOpt("bootclasspath-dir")
                .withDescription("the base folder to look for the bootclasspath files in. Defaults to the current " +
                        "directory")
                .hasArg()
                .withArgName("DIR")
                .create("C");

        Option deodexerantOption = OptionBuilder.withLongOpt("deodexerant")
                .isRequired()
                .withDescription("connect to deodexerant on the specified HOST:PORT, and validate the virtual method " +
                        "indexes, field offsets and inline methods against what dexlib calculates")
                .hasArg()
                .withArgName("HOST:PORT")
                .create("x");

        Option classStartOption = OptionBuilder.withLongOpt("class-start-index")
                .withDescription("Start checking classes at the given class index")
                .hasArg()
                .withArgName("CLASSINDEX")
                .create("i");

        options.addOption(versionOption);
        options.addOption(helpOption);
        options.addOption(deodexerantOption);
        options.addOption(classPathOption);
        options.addOption(classPathDirOption);
        options.addOption(classStartOption);

        return options;
    }
}