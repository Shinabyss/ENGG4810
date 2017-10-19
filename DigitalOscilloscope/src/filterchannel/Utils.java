package filterchannel;

import java.io.File;

/**Utils Class
 * 
 * for setup FileFilter extensions
 * 
 * @author Michael
 *
 */
public class Utils {
	
	public final static String csv = "csv";
	
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
 
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
 
}
