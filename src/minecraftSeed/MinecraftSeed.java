/*
This software and it's source are distributed under the terms of the Modified BSD License, detailed below.

Copyright (c) 2011-2012, Christopher Iverson
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Minecraft Save Seed Reader team nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
*/

package minecraftSeed;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

class DirFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		return new File(dir.getPath() + File.separator + name).isDirectory();
	}
	
}

public class MinecraftSeed implements ActionListener {
	private Tag main;
	private JFrame frame;
	private JPanel panel;
	private JTextField text;
	private String savePath;
	private String[] filePaths;
	private String lastFolder;
	private int validNames;
	private JComboBox<Object> combo;
	private boolean hardcoreEnabled;
	private boolean commandsEnabled;
	private boolean abilitiesExist;     //To control tags added in Beta 1.9 PR 5.
										//If these tags are not changed properly, Creative mode
										//abilities can be enabled in Survival mode.
	
	private int gameType;
	private int playerGameType;
	private boolean usePGT;            //Stands for Use playerGameType.  Older saves didn't have this.
	private boolean initSave;          //This flag is enabled when a save file is first loaded, and before it's been interacted with.
									   //The main use for it is preventing the Gamemode combobox's action event from affecting data when first loaded.
	private JButton btnSave;
	private String selectedFilePath;
	
	//private final Integer version = MinecraftSeed.makeVersion(1,7,1);
	private final String version = "1.7.1";
	private JCheckBox cbHardcore;
	private JCheckBox cbCommands;
	private JLabel lblGamemode;
	private JComboBox<Object> cmbGamemode;
	
	public MinecraftSeed()
	{
		//Set the system look and feel
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		
		//Windows
		if(System.getProperty("os.name").toLowerCase().contains("windows"))
		{
			savePath = System.getenv("APPDATA") + "\\.minecraft\\saves\\";
		}
		
		//Mac
		if(System.getProperty("os.name").toLowerCase().contains("mac os x"))
		{
			savePath = System.getProperty("user.home") + "/Library/Application Support/minecraft/saves/";
		}
		
		//Linux(hopefully)
		if(System.getProperty("os.name").toLowerCase().contains("linux"))
		{
			savePath = System.getProperty("user.home") + "/.minecraft/saves/";
		}
		
		frame = new JFrame("Minecraft Save Seed Reader v" + version);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		panel = new JPanel();
		
		combo = new JComboBox<Object>();
		combo.addActionListener(this);
		
		text = new JTextField();
		text.setColumns(20);
		text.setEditable(false);
		
		
		JMenuBar menuBar = new JMenuBar();
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem howToUse = new JMenuItem("How to use");
		howToUse.setActionCommand("use");
		howToUse.addActionListener(this);
		helpMenu.add(howToUse);
		
		JMenuItem newFolder = new JMenuItem("Select MC Save Folder");
		newFolder.setActionCommand("folder");
		newFolder.addActionListener(this);
		helpMenu.add(newFolder);
		
		helpMenu.addSeparator();
		
		JMenuItem about = new JMenuItem("About");
		about.setActionCommand("about");
		about.addActionListener(this);
		helpMenu.add(about);
		
		menuBar.add(helpMenu);
		frame.setJMenuBar(menuBar);
		panel.setLayout(new MigLayout("", "[28px][166px,grow][95px]", "[23px][][][]"));
		
		panel.add(combo, "cell 0 0,alignx left,aligny center");
		panel.add(text, "cell 1 0 2 1,growx");
		frame.getContentPane().add(panel);
		
		lblGamemode = new JLabel("Gamemode:");
		panel.add(lblGamemode, "cell 0 1,alignx trailing");
		
		btnSave = new JButton("Save Changes");
		btnSave.setEnabled(false);
		btnSave.setActionCommand("save");
		btnSave.addActionListener(this);
		
		cbCommands = new JCheckBox("Enable Commands");
		cbCommands.setEnabled(false);
		cbCommands.setActionCommand("commandstoggle");
		cbCommands.addActionListener(this);
		
		cbHardcore = new JCheckBox("Hardcore Mode");
		cbHardcore.setEnabled(false);
		cbHardcore.setActionCommand("hardcoretoggle");
		cbHardcore.addActionListener(this);
		
		cmbGamemode = new JComboBox<Object>();
		cmbGamemode.setEnabled(false);
		cmbGamemode.setActionCommand("gamemode");
		cmbGamemode.addActionListener(this);
		panel.add(cmbGamemode, "cell 1 1 2 1,growx");
		panel.add(cbHardcore, "cell 1 2");
		panel.add(cbCommands, "cell 2 2");
		panel.add(btnSave, "cell 2 3");
		
		//Try to load the save data
		setupData();
		
		frame.pack();
		
		//Center on screen
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	/*private static int makeVersion(int major, int minor, int rev)
	{
		return (major << (3*8)) + (minor << (2*8)) + rev;
	}*/
	
	public static void main(String[] args) {

		try {
			new MinecraftSeed();
			
		} catch(Exception e) {  //If anything unexpected goes wrong in the main program,
			     				//write it to an error log
			try {
				FileOutputStream fos = new FileOutputStream("MinecraftSeed.error.log");
				e.printStackTrace(new PrintStream(fos));
				fos.close();
				
				JOptionPane.showMessageDialog(null, "An error occured." +
						"\nCheck the MinecraftSeed.error.log file for more information.", 
						"ERROR", JOptionPane.ERROR_MESSAGE);
				
			} catch (FileNotFoundException e1) {  //If the error log messes up,
												  //fall back to console
				e.printStackTrace();
			} catch (IOException e2) {
				e.printStackTrace();
			}
			e.printStackTrace();
		}
		
	}

	private void setupData()
	{
		boolean loop = false;
		
		//Check to reload data, used when checking folders with no valid save files
		do {
			loop = false;
			
			File file = new File(savePath);
			
			//If the save folder selected actually exists
			if(file.exists())
			{
				//Grab a list of sub-directories in the saves directory
				String[] tempPaths = file.list(new DirFilter());
				filePaths = new String[tempPaths.length];
		
				validNames = 0;
				FileInputStream fis;
		
				combo.removeAllItems();
				for(String s : tempPaths)
				{
					//Check each sub-directory for a 'level.dat' file
					//If one is found, then assume it's a valid MC save folder
					file = new File(savePath + File.separator + s + File.separator + "level.dat");
					if(file.exists()) {
						filePaths[validNames] = file.getPath();
						
						lastFolder = file.getParent();
						
						try {
							fis = new FileInputStream(file);
							Tag temp = Tag.readFrom(fis);
							fis.close();
							
							//If the level.dat doesn't have a 'LevelName' entry,
							//go by the folder's name
							Tag name = temp.findTagByName("LevelName");
							if(name == null) {
								combo.addItem(makeObj(file.getParentFile().getName()));
							} else {
								combo.addItem(makeObj((String)name.getValue()));	
							}
							
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e)
						{
							//Unknown problem with loading save file.  Corrupted?
							JOptionPane.showMessageDialog(frame, "There was a problem reading the save file in " + lastFolder
									+ ".\n It will not be loaded in this program.", "Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}
						
						validNames++;
					}
				}
				
				if(validNames == 0) 
			    { 
					//No valid save files were found
					JOptionPane.showMessageDialog(panel, "No valid save files were detected in current folder." +
							"\n\nPlease choose a new one.", "Error", JOptionPane.ERROR_MESSAGE);
					
					if(chooseSaveFolder()) loop = true;
					//This won't loop if the user cancels out of the folder selection dialog.
				}
			} else {  //Save folder doesn't exist
				JOptionPane.showMessageDialog(panel, "Save folder doesn't exist.\n\nPlease choose a new one.",
						"Error", JOptionPane.ERROR_MESSAGE);
				
				if(chooseSaveFolder()) loop = true;
				//Again, won't loop if the user cancels out of the folder selection dialog.
			}
		} while (loop);
	}
	
	private boolean chooseSaveFolder()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int ret = fc.showDialog(panel, "Open Minecraft Saves Folder");
		if(ret == JFileChooser.APPROVE_OPTION) {
			savePath = fc.getSelectedFile().getPath();
			
			//Three tests to see if the folder chosen was a world folder, not the saves folder
			//Test one: check for existence of 'level.dat'
			if((new File(savePath + File.separator + "level.dat")).exists())
			{
				//'level.dat' exists, now check for 'session.lock'
				if((new File(savePath + File.separator + "session.lock")).exists())
				{
					//'session.lock' exists, now the final check: region subfolder.
					if((new File(savePath + File.separator + "region")).exists())
					{
						//At this point, we've almost indisputably gotten a world folder instead
						//of the saves folder.  Let's set 'savePath' to the parent directory.
						savePath = new File(savePath).getParent();
					}
				}
			}
			return true;
		}
		
		return false;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//If a combobox item was selected
		if(arg0.getSource() == combo)
		{
			int val = combo.getSelectedIndex();
			
			//Make sure something was actually chosen
			if(val > -1)
			{
				//Disable the save button
				btnSave.setEnabled(false);
				
				try {
					selectedFilePath = filePaths[val];
					FileInputStream fis = new FileInputStream(new File(selectedFilePath));
					main = Tag.readFrom(fis);
					fis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				usePGT = false;
				initSave = true;
				cmbGamemode.removeAllItems();
				cmbGamemode.addItem("Survival");
				
				
				text.setText(main.findTagByName("RandomSeed").getValue().toString());


				//Only if the save file has a 'GameType' entry will
				//we allow the user to switch between Creative on and off.
				Tag gametype = main.findTagByName("GameType");
				if(gametype==null)
				{
					cmbGamemode.setEnabled(false);
				}
				else
				{
					cmbGamemode.setEnabled(true);
					cmbGamemode.addItem("Creative");

					gameType = (Integer)gametype.getValue();
				}

				//Same as above, only with the "hardcore" toggle.
				Tag hardcore = main.findTagByName("hardcore");
				if(hardcore==null)
				{
					cbHardcore.setEnabled(false);
					cbHardcore.setSelected(false);
				}
				else
				{
					cbHardcore.setEnabled(true);

					//Check the box if hardcore is enabled
					hardcoreEnabled = ((Byte)hardcore.getValue() == 1);
					cbHardcore.setSelected(hardcoreEnabled);
				}

				Tag commands = main.findTagByName("allowCommands");
				if(commands==null)
				{
					cbCommands.setEnabled(false);
					cbCommands.setSelected(false);
				}
				else
				{
					cbCommands.setEnabled(true);

					commandsEnabled = ((Byte)commands.getValue() == 1);
					cbCommands.setSelected(commandsEnabled);
				}
				
				//The reason "Player" is also used here is to make sure we find the correct tag.
				Tag abilities = main.findTagByName("Player").findTagByName("abilities");

				abilitiesExist = (abilities != null);

				if(abilitiesExist)
				{
					Tag mayBuild = abilities.findTagByName("mayBuild");
					if(mayBuild != null)
					{
						cmbGamemode.addItem("Adventure");	
					}
				}
				
				Tag playerGameMode = main.findTagByName("Player").findTagByName("playerGameType");
				if(playerGameMode != null)
				{
					usePGT = true;
					
					playerGameType = (Integer)playerGameMode.getValue();
				} 
				
				initSave = true;
				if(usePGT)
				{
					cmbGamemode.setSelectedIndex(playerGameType);
				}
				else
				{
					cmbGamemode.setSelectedIndex(gameType);
				}

			} // if (val > -1)
			
		} else { //One of the menu items was chosen, or the checkbox was checked,
			     //or "save" was pressed
			String cmd = arg0.getActionCommand();
			
			//"How to use" menu item
			if(cmd.equals("use"))
			{
				String message = "Choose the world's name from the dropdown list." +
								 "\nThe world's seed will show up in the textbox." + 
								 "\n\nIf the list is empty, click on Help > Select MC Save Folder," +
								 "\nand find and open the saves folder in the file chooser.\n" +
								 "\nIf the save is a Beta 1.8+ save, you can switch the world" +
								 "\nbetween Creative and Survival by checking the checkbox, then" +
								 "\nhitting Save.";
				
				JOptionPane.showMessageDialog(panel, message, "How to Use", JOptionPane.INFORMATION_MESSAGE);
			}
			
			//"About" menu item
			if(cmd.equals("about"))
			{
				String message = "Written by Chris Iverson. \n\n" +
								 "Source code available here:\n" +
								 "https://github.com/thedarkfreak/Minecraft-Save-Seed-Reader\n" +
								 "\nProblems? \nReport them either to the github site, in the forum topic,\n" +
								 "or send me an e-mail at cj.no.one@gmail.com.\n" +
								 "\nEnjoy!";
				
				JOptionPane.showMessageDialog(panel, message, "About Minecraft Save Seed Reader v" + version,
						JOptionPane.INFORMATION_MESSAGE);
			}
			
			//"Select MC Save Folder" menu item
			if(cmd.equals("folder"))
			{
				//Only load the data if the user selected a folder
				if(chooseSaveFolder()) setupData();
			}
			
			//Game mode changed
			if(cmd.equals("gamemode"))
			{
				if (cmbGamemode.getSelectedIndex() > -1) 
				{
					
					if (!initSave) {
						gameType = cmbGamemode.getSelectedIndex();
						Tag gamemode = main.findTagByName("GameType");
						gamemode.setValue(gameType);
						
						if (usePGT) {
							playerGameType = gameType;
							Tag pgt = main.findTagByName("Player").findTagByName("playerGameType");
							pgt.setValue(playerGameType);
						}
						//Enable/disable all special abilities depending on selection.
						//while "Flying" is only used by the game to indicate whether or not the player is currently flying,
						//it is toggled here to make sure a player can't remain flying when Creative is turned off.
						if (abilitiesExist) {
							Tag abilities = main.findTagByName("Player")
									.findTagByName("abilities");

							Tag temp;

							byte enable = (byte) ((gameType == 1) ? 1 : 0);

							temp = abilities.findTagByName("instabuild");
							temp.setValue(enable);

							temp = abilities.findTagByName("invulnerable");
							temp.setValue(enable);

							temp = abilities.findTagByName("mayfly");
							temp.setValue(enable);

							temp = abilities.findTagByName("flying");
							temp.setValue(enable);

							enable = (byte) ((gameType == 2) ? 1 : 0);

							temp = abilities.findTagByName("mayBuild");
							if (temp != null)
								temp.setValue(enable);
						}
						//Enable the "Save" button
						btnSave.setEnabled(true);
					} //if(!initSave)
					else
					{
						initSave = false;
					}
				}
			}
			
			
			//Hardcore mode toggled
			if(cmd.equals("hardcoretoggle"))
			{
				hardcoreEnabled = !hardcoreEnabled;
				
				Tag hardcore = main.findTagByName("hardcore");
				hardcore.setValue((byte)(hardcoreEnabled? 1 : 0));
				
				//Enable the "save" button
				btnSave.setEnabled(true);
			}
			
			if(cmd.equals("commandstoggle"))
			{
				commandsEnabled = !commandsEnabled;
				
				Tag commands = main.findTagByName("allowCommands");
				commands.setValue((byte)(commandsEnabled? 1 : 0));
				
				btnSave.setEnabled(true);
			}
			
			//Save button pressed
			if(cmd.equals("save"))
			{
				int chosen = JOptionPane.showConfirmDialog(frame, "Are you sure you want to overwrite your save file?", 
						"Overwrite", JOptionPane.YES_NO_OPTION);
				
				if(chosen == JOptionPane.YES_OPTION)
				{
					try {
						FileOutputStream fos = new FileOutputStream(new File(selectedFilePath));
						main.writeTo(fos);
						fos.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(null, "Problem saving file: File Not Found", "File Not Found", JOptionPane.ERROR_MESSAGE);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(null, "Problem saving file: IO Error", "IO Error", JOptionPane.ERROR_MESSAGE);
					}
					
					JOptionPane.showMessageDialog(frame, "File saved!");
					
					btnSave.setEnabled(false);
				}
			}
		} // else
		
	} //actionPerformed
	
	private Object makeObj(final String item)  {
		return new Object() { public String toString() { return item; } };
	}


}
