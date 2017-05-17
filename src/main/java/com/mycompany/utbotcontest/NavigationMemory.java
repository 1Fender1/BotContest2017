package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author JB
 */
public class NavigationMemory extends Memory {

    final protected String navEnhanceDir = rootDir + "/navEnhance";
    protected List<NavPoint> fromNavPoints;
    protected List<NavPoint> toNavPoints;
    
    
    public NavigationMemory()
    {
        fromNavPoints = new ArrayList<NavPoint>();
        toNavPoints = new ArrayList<NavPoint>();
    }
    
    
    
    private void createMemory(String mapName) throws IOException
    {
        String fileName = navEnhanceDir + "/" + mapName + ".txt";
        File file = new File(fileName);
        file.setReadable(true);
        file.setWritable(true);
        if (!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                System.out.println("Probleme de création de fichier");
            }
        }

    }
    
    
    public void addInfo(String memoryName, String Loc1, String Loc2) throws IOException
    {
        File file = new File(navEnhanceDir + "/" + memoryName + ".txt");
        if (!file.exists())
        {
            createMemory(memoryName);
        }
        
        //FileOutputStream  writeFile = new FileOutputStream(file);
        
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
navEnhanceDir + "/" + memoryName + ".txt", true));
        
        Loc1 = Loc1 + "~";
        Loc2 = Loc2 + "\n";
        String info = Loc1 + Loc2;
        bufferedWriter.write(info);
        bufferedWriter.close();
    }
    
    
    private List<String> getInfos(String memoryName) throws FileNotFoundException, IOException
    {   
        List<String> lignes = new ArrayList<String>();
        File file = new File(navEnhanceDir + "/" + memoryName + ".txt");
        if (!file.exists())
        {
            System.out.println("Pas d'infos à récupérer car le fichier n'existe pas");
            return null;
        }
        FileInputStream readFile = new FileInputStream(file);
        String ligneTemp = "";
        int temp;
        while ((temp = readFile.read()) != -1)
        {
            if ((char) temp != '\n')
            {
                ligneTemp += (char) temp;
            }
            else
            {
                lignes.add(ligneTemp);
                ligneTemp = "";
            }
        }
        
        return lignes;
    }
    

    public void fillNavPointsFromMemory(String mapName, NavPoints navPointsElems) throws IOException
    {
        List<NavPoint> pointsList = getNavPListWithNavPoints(navPointsElems);
        
        List<String> toNavToString = new ArrayList<String>();
        List<String> fromNavToString = new ArrayList<String>();
        List<String> navPoints = getInfos(mapName);
        String[] tempNav;
        
        if (navPoints == null)
            return;
        
        for (String ligne : navPoints)
        {
            tempNav = ligne.split("~");
            fromNavToString.add(tempNav[0]);
            toNavToString.add(tempNav[1]);
        }
        

        for (NavPoint navP : pointsList)
        {
            for (String stringNavPoint : toNavToString)
            {
                if (navP.toString().equals(stringNavPoint))
                {
                    toNavPoints.add(navP);
                }
            }
            for (String stringNavPoint : fromNavToString)
            {
                if (navP.toString().equals(stringNavPoint))
                {
                    fromNavPoints.add(navP);
                }
            }
        }
    }
    
    private List<NavPoint> getNavPListWithNavPoints(NavPoints navPoints)
    {
        List<NavPoint> res = new ArrayList<NavPoint>();
        for (Entry<UnrealId, NavPoint> navP : navPoints.getNavPoints().entrySet())
        {
            res.add(navP.getValue());
        }
        return res;
    }
    
   
    
    @Override
    public void forget(String info) {
        File newFile = new File(navEnhanceDir + "/" + info + ".txt");
        
        if (newFile.exists())
        {
            newFile.delete();
        }
    }
    
    public List<NavPoint> getFromNavPoints() {
      return fromNavPoints;
    }

    public List<NavPoint> getToNavPoints() {
        return toNavPoints;
    }

     @Override
    public void addInfo(String memoryName, String info) throws IOException
    {
        File file = new File(navEnhanceDir + "/" + memoryName + ".txt");
        if (!file.exists())
        {
            createMemory(memoryName);
        }
        FileWriter writer = new FileWriter(file, true);
        
        info = info + "\n";
        writer.write(info);
        
        writer.close();
    }


  
}
    
