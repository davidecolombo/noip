package io.github.davidecolombo.noip;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import io.github.davidecolombo.noip.exception.ConfigurationException;
import io.github.davidecolombo.noip.exception.NoIpException;
import io.github.davidecolombo.noip.noip.NoIpUpdater;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.io.IOException;

@Slf4j
public class App {

	@Option(name = "-settings", aliases = {"-s"}, required = true)
	private String fileName;

	private static class SingletonHolder {
		public static final App instance = new App();
	}

	public static App getInstance() {
		return SingletonHolder.instance;
	}

	private App() {}

	public Integer update(String[] args) {
		int status = NoIpUpdater.ERROR_RETURN_CODE;
		try {
			logger.debug("Parsing command line arguments: {}", String.join(" ", args));
			new CmdLineParser(this).parseArgument(args);
			
			logger.info("Starting No-IP update process with settings file: {}", fileName);
			status = NoIpUpdater.updateFromIpify(fileName);
			
			if (status == 0) {
				logger.info("No-IP update completed successfully");
			} else {
				logger.warn("No-IP update completed with status code: {}", status);
			}
			
		} catch (CmdLineException e) {
			logger.error("Command line error: {}", e.getMessage());
			logger.error("Usage: -settings <path_to_settings.json>");
		} catch (ConfigurationException e) {
			logger.error("Configuration error: {}. Please check your settings file.", e.getMessage());
		} catch (NoIpException e) {
			logger.error("No-IP service error: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("I/O error accessing settings or external services: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error: {}", e.getMessage(), e);
		}
		
		logger.info("Application exiting with status code: {}", status);
		return status;
	}

	/*
	 * Usage: -settings src/test/resources/settings.json
	 */
	public static void main(String[] args) {
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
		System.exit(App.getInstance().update(args));
	}
}