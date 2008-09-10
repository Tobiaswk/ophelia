package ophelia.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 *
 * @author Tobias W. Kjeldsen
 */
public class MediaFilter extends FileFilter {

    @Override
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


    //The description of this filter
    public String getDescription() {
        return "Supported formats (*.mp3 and *.flac)";
    }

}
