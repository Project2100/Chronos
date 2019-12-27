package chronos;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;

/*
 Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

 Developed by Pete Sanderson (psanderson@otterbein.edu)
 and Kenneth Vollmar (kenvollmar@missouristate.edu)

 Permission is hereby granted, free of charge, to any person obtaining 
 a copy of this software and associated documentation files (the 
 "Software"), to deal in the Software without restriction, including 
 without limitation the rights to use, copy, modify, merge, publish, 
 distribute, sublicense, and/or sell copies of the Software, and to 
 permit persons to whom the Software is furnished to do so, subject 
 to the following conditions:

 The above copyright notice and this permission notice shall be 
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 (MIT license, http://www.opensource.org/licenses/mit-license.html)
 */
/**
 *
 * @author Project2100
 */
public enum StringSettings {

    EXCEPTION_HANDLER_FILE("ExceptionHandler", "");

    final String identifier;
    final String vDefault;
    private String value;

    private StringSettings(String id, String def) {
        identifier = id;
        vDefault = def;
        value = Main.SETTINGS.get(identifier, def);
    }

    public String get() {
        return value;
    }

    public void set(String value) {
        this.value = value;
        Main.SETTINGS.put(identifier, value);
        try {
            Main.SETTINGS.flush();
        }
        catch (SecurityException | BackingStoreException e) {
            Main.logger.log(Level.SEVERE, "Unable to save string setting: " + identifier, e);
        }
    }

}
