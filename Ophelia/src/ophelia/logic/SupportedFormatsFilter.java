package ophelia.logic;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class SupportedFormatsFilter implements FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = f.getAbsolutePath();
        if (extension != null) {
            if (extension.endsWith(".mp3") ||
                    extension.endsWith(".flac")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
