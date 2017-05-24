
package com.mycompany.utbotcontest;

import java.io.IOException;

/**
 *
 * @author JB
 */
public abstract class Memory {

    final protected String rootDir = "BotMemory";
    
    //abstract void createMemory(String object) throws IOException;
    abstract void addInfo(String memoryName, String info) throws IOException;
    abstract void forget(String info) throws IOException;
    
}
