package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author JB
 */
public class WeaponMemory extends Memory {
    
    
    final protected String weaponChoiceDir = rootDir + "/weaponChoice";
    
    
    
    private void createMemory(String memoryName) throws IOException
    {
        String fileName = weaponChoiceDir + "/" + memoryName + ".txt";
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
        File file = new File(weaponChoiceDir + "/" + memoryName + ".txt");
        if (!file.exists())
        {
            createMemory(memoryName);
        }
        
        FileOutputStream  writeFile = new FileOutputStream(file);
        
        writeFile.getChannel().position(file.length());
        info = info + "\n";
        writeFile.write(info.getBytes());
        
        writeFile.close();
    }
    
    private UT2004ItemType getType(String type)
    {
        if (type.equals(UT2004ItemType.ROCKET_LAUNCHER.toString()))
        {
            return UT2004ItemType.ROCKET_LAUNCHER;
        }
        else if (type.equals(UT2004ItemType.FLAK_CANNON.toString()))
        {
            return UT2004ItemType.FLAK_CANNON;
        }
        else if (type.equals(UT2004ItemType.LIGHTNING_GUN.toString()))
        {
            return UT2004ItemType.LIGHTNING_GUN;
        }
        else if (type.equals(UT2004ItemType.MINIGUN.toString()))
        {
            return UT2004ItemType.MINIGUN;
        }
        else if (type.equals(UT2004ItemType.LINK_GUN.toString()))
        {
            return UT2004ItemType.LINK_GUN;
        }
        else if (type.equals(UT2004ItemType.ASSAULT_RIFLE.toString()))
        {
            return UT2004ItemType.ASSAULT_RIFLE;
        }
        else if (type.equals(UT2004ItemType.SHOCK_RIFLE.toString()))
        {
            return UT2004ItemType.SHOCK_RIFLE;
        }
        else if (type.equals(UT2004ItemType.BIO_RIFLE.toString()))
        {
            return UT2004ItemType.BIO_RIFLE;
        }
        else if (type.equals(UT2004ItemType.SHIELD_GUN.toString()))
        {
            return UT2004ItemType.SHIELD_GUN;
        }
        else if (type.equals(UT2004ItemType.ION_PAINTER.toString()))
        {
            return UT2004ItemType.ION_PAINTER;
        }
        else if (type.equals(UT2004ItemType.ONS_AVRIL.toString()))
        {
            return UT2004ItemType.ONS_AVRIL;
        }
        else if (type.equals(UT2004ItemType.ONS_GRENADE_LAUNCHER.toString()))
        {
            return UT2004ItemType.ONS_GRENADE_LAUNCHER;
        }
        else if (type.equals(UT2004ItemType.ONS_MINE_LAYER.toString()))
        {
            return UT2004ItemType.ONS_MINE_LAYER;
        }
        else if (type.equals(UT2004ItemType.REDEEMER.toString()))
        {
            return UT2004ItemType.REDEEMER;
        }
        else if (type.equals(UT2004ItemType.SNIPER_RIFLE.toString()))
        {
            return UT2004ItemType.SNIPER_RIFLE;
        }
        else if (type.equals(UT2004ItemType.SUPER_SHOCK_RIFLE.toString()))
        {
            return UT2004ItemType.SUPER_SHOCK_RIFLE;
        }
        else if (type.equals(UT2004ItemType.TRANSLOCATOR.toString()))
        {
            return UT2004ItemType.TRANSLOCATOR;
        }
        else
            return null;
    }
    
    
    private ProbabilitesArmes getDefaultProba(String type)
    {
        if (getType(type).equals(UT2004ItemType.ROCKET_LAUNCHER))
        {
            return new ProbabilitesArmes(UT2004ItemType.ROCKET_LAUNCHER, 0.7, 1, 0, 0, 500, 3000);
        }
        else if (getType(type).equals(UT2004ItemType.FLAK_CANNON))
        {
            return new ProbabilitesArmes(UT2004ItemType.FLAK_CANNON, 0.7, 2, 0, 0, 50, 1800);
        }
        else if (getType(type).equals(UT2004ItemType.LIGHTNING_GUN))
        {
            return new ProbabilitesArmes(UT2004ItemType.LIGHTNING_GUN, 0.7, 2, 0, 0, 700, 4000);
        }
        else if (getType(type).equals(UT2004ItemType.MINIGUN))
        {
            return new ProbabilitesArmes(UT2004ItemType.MINIGUN, 0.6, 3, 0, 0, 50, 1800);
        }
        else if (getType(type).equals(UT2004ItemType.LINK_GUN))
        {
            return new ProbabilitesArmes(UT2004ItemType.LINK_GUN, 0.6, 4, 0, 0, 500, 2000);
        }
        else if (getType(type).equals(UT2004ItemType.ASSAULT_RIFLE))
        {
            return new ProbabilitesArmes(UT2004ItemType.ASSAULT_RIFLE, 0.5, 5, 0, 0, 0, 600);
        }
        else if (getType(type).equals(UT2004ItemType.SHOCK_RIFLE))
        {
            return new ProbabilitesArmes(UT2004ItemType.SHOCK_RIFLE, 0.4, 5, 0, 0, 1000, 4000);
        }
        else if (getType(type).equals(UT2004ItemType.BIO_RIFLE))
        {
            return new ProbabilitesArmes(UT2004ItemType.BIO_RIFLE, 0.4, 100, 0, 0, 50, 1500);
        }
        else if (getType(type).equals(UT2004ItemType.SHIELD_GUN))
        {
            return new ProbabilitesArmes(UT2004ItemType.SHIELD_GUN, 0, 100, 0, 0, 0, 0);
        }
        else
        {
            return new ProbabilitesArmes(getType(type), 0, 100, 0, 0, 0, 0);
        }
        
        
    }
    
    
    public ProbabilitesArmes getProba(String memoryName) throws FileNotFoundException, IOException
    {
        File file = new File(weaponChoiceDir + "/" + memoryName + ".txt");
        
        int temp;
        String mem = "";
        String[] elems;
        
        if (file.exists())
        {
            FileInputStream readFile = new FileInputStream(file);
            while ((temp = readFile.read()) != -1 && (char) temp != '\n')
            {
               mem += (char) temp;
            }

            elems = mem.split(";");
            UT2004ItemType type = getType(elems[0]);
            double proba = Double.parseDouble(elems[1]);
            double poids = Double.parseDouble(elems[2]);
            int nbVictoires = Integer.parseInt(elems[3]);
            int nbDefaites = Integer.parseInt(elems[4]);
            double distanceInf = Double.parseDouble(elems[5]);
            double distanceSup = Double.parseDouble(elems[6]);
        
            return new ProbabilitesArmes(type, proba, poids, nbVictoires, nbDefaites, distanceInf, distanceSup);
        }
        else
        {
            createMemory(memoryName);
            ProbabilitesArmes newP = getDefaultProba(memoryName);
            addInfo(memoryName, newP.toStringForMemory());
            return newP;
        }
    }
    
    @Override
    public void forget(String info) {
        File newFile = new File(weaponChoiceDir + "/" + info + ".txt");
        
        if (newFile.exists())
        {
            newFile.delete();
        }
    }

    
}
