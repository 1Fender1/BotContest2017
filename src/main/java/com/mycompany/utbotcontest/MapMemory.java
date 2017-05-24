package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
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
public class MapMemory extends Memory {
    
    private final String mapKnowledgeDir = rootDir + "/mapKnowledge";

    private void createMemory(String memoryName) throws IOException
    {
        String fileName = mapKnowledgeDir + "/" + memoryName + ".txt";
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

    
    @Override
    public void addInfo(String memoryName, String info) throws IOException
    {
        File file = new File(mapKnowledgeDir + "/" + memoryName + ".txt");
        if (!file.exists())
        {
            createMemory(memoryName);
        }
        FileWriter writer = new FileWriter(file, true);
        
        info = info + "\n";
        writer.write(info);
        
        writer.close();
    }
    
    private List<Item> getAllItems(Items allItems)
    {
        List<Item> items = new ArrayList<Item>();
        
        for (Entry<UnrealId, Item> item : allItems.getAllItems().entrySet())
        {
            items.add(item.getValue());
        }
        
        return items;
    }
    
    
    public List<Item> getItems(String memoryName, Items items) throws FileNotFoundException, IOException
    {
        List<Item> res = new ArrayList<Item>();
        
        List<String> lignes = new ArrayList<String>();
        File file = new File(mapKnowledgeDir + "/" + memoryName + ".txt");
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
        
        List<Item> allItems = getAllItems(items);
        
        for (Item item : allItems)
        {
            for (String itemString : lignes)
            {
                if (item.getId().toString().equals(itemString))
                {
                    res.add(item);
                }
            }
        }
        
        
        return res;
    }
    

    @Override
    public void forget(String info) {
        File newFile = new File(mapKnowledgeDir + "/" + info + ".txt");
        
        if (newFile.exists())
        {
            newFile.delete();
        }
    }
}
