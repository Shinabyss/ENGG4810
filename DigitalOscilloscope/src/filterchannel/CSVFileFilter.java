package filterchannel;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**FileFilter class for CSVFiles
 * 
 * @author Michael connor
 *CSV filter
 */
public class CSVFileFilter extends FileFilter {
	
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
 
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.csv)) {
                    return true;
            } else {
                return false;
            }
        }
 
        return false;
    }
 
    //The description of this filter
    public String getDescription() {
        return "CSV files only";
    }

}

