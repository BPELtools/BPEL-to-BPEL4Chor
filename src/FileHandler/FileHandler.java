package FileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

//Dateien einlesen
public class FileHandler {

	private String path;

	public FileHandler(String path) {
		this.path = path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public List<File> getFiles(String regExpression) {

		List<File> files = this.find(path, regExpression);

		return files;

	}

	/**
	 * @param start
	 * @param extensionPattern
	 * @return
	 */
	private List<File> find(String start, String extensionPattern)

	{
		final List<File> files = new ArrayList<File>(1024);
		final Stack<File> dirs = new Stack<File>();
		final File startdir = new File(start);
		final Pattern p = Pattern.compile(extensionPattern,
				Pattern.CASE_INSENSITIVE);

		if (startdir.isDirectory())
			dirs.push(startdir);

		while (dirs.size() > 0) {
			for (File file : dirs.pop().listFiles()) {
				if (file.isDirectory())
					dirs.push(file);
				else if (p.matcher(file.getName()).matches())
					files.add(file);
			}
		}

		return files;
	}
	

}
