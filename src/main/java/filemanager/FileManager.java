package filemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class FileManager {

    static final Logger logger = Logger.getLogger("Main");

	static Properties loadConfiguration() {
		Path confPath = Paths.get("conf.properties");
		Properties configuration = new Properties();
		try {
			configuration.load(Files.newInputStream(confPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return configuration;
	}
	
	static int count = 0;
	
	public static void main(String[] args) throws Exception {
		Properties configuration = loadConfiguration();
		logger.info("Application is started");
		String dirPath = configuration.getProperty("dirPath");
		String xroadName = configuration.getProperty("xroadName");
		String jettyName = configuration.getProperty("jettyName");
		
		Path path = Paths.get(dirPath+jettyName);
		if(!Files.isDirectory(path)) {
			logger.log(Level.SEVERE,"'" +path.toString() + "' is not a directory");	
			throw new Exception(path.toString() + " is not a directory");
		}
		Stream<Path> files = Files.list(path);
		if(files.count() <= 3) {
			logger.warning("For this process the files have to be more than 3 version");
			System.exit(0);
		}
		files.sorted((o1, o2) -> (o1.toFile().lastModified() > o2.toFile().lastModified() ? 1 : -1));
		
		Iterator<Path> iterator = files.iterator();
		int i = 0;
		while(iterator.hasNext()) {
			iterator.remove();
			i++;
			if(i == 3) {
				break;
			}
		}
		
		
		files.forEach( p -> {
			try {
				String uniqueName = p.getFileName().toString().split("git")[1];
				Path pathX = Paths.get(dirPath+xroadName);
				if(!Files.isDirectory(pathX)) {
					logger.log(Level.SEVERE,"'" +pathX.toString() + "' is not a directory");	
					throw new Exception(pathX.toString() + " is not a directory");
				}
				Stream<Path> filesX = Files.list(pathX);
				filesX.forEach(p2 -> {
					if(p2.getFileName().toString().endsWith(uniqueName)) {
						try {
							count++;
							logger.warning(p2.toString() + " is removed");
							Files.deleteIfExists(p2);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				logger.warning(p.toString() + " is removed");
				count++;
				Files.deleteIfExists(p);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		logger.info(count+" elements is removed");
		logger.info("Process is finished");
	}

}
