package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.VisibilityCreator;
import java.io.File;
/**
 *
 * @author JB
 */
public class MatrixVisibility {
    
    public void MatrixInitialized()
    {
        VisibilityCreator visibilityCreator = new VisibilityCreator();
        String mapName = visibilityCreator.getServer().getMapName();
        File fileName = new File("./" + "VisibilityMatrix-" + mapName + "-All.bin");
        
        //File file = new File("./" + "VisibilityMatrix-" + mapName + "-All.bin");
        File destination = new File(".");
        if (!fileName.exists())
        {
            visibilityCreator.createAndSave(destination);
        }
    }

}
