//Autospliter for SCP Containment Breach Speedrun Mod
//Made by Forx
//December 10 2023

state("SCP - Containment Breach Speedrun Mod", "Speedrun Mod") {
    float playerY : "SCP - Containment Breach Speedrun Mod.exe", 0x000f5400, 0x160, 0x140, 0x44; // player y pos
}

startup {
    if (timer.CurrentTimingMethod == TimingMethod.RealTime) {
	var gameTimeMessage = MessageBox.Show(
					      "The autosplitter for the speedrun mod uses time without loads, and you are set to real time. Switch to game time?",
					      "Livesplit | SCP - Containment Breach Speedrun Mod",
					      MessageBoxButtons.YesNo, MessageBoxIcon.Question
					      );
	if (gameTimeMessage == DialogResult.Yes) {
	    timer.CurrentTimingMethod = TimingMethod.GameTime;
	}
    }

    settings.Add("Standard", true, "Standard autosplitter");
    settings.SetToolTip("Standard", "Automatically splits when you: " +
			"\n - Start the run." +
			"\n - Reach Heavy Containment." +
			"\n - Reach Entrance Zone." +
			"\n - Reach the surface." +
			"\n - Complete the run." +
			"\n\nDisable this if you just want auto start and stop."
			);
    settings.Add("splitOnOmni", false, "Split on getting an omni card.", "Standard");
    settings.SetToolTip("splitOnOmni", "Automatically splits when you get an omni key card.");

    settings.Add("splitOnSl", false, "Split upon disabling lockdown", "Standard");
    settings.SetToolTip("splitOnSl", "Automatically splits when you disable the light containment lockdown.");
    
    settings.Add("splitOn079", false, "Split on SCP-079", "Standard");
    settings.SetToolTip("splitOn079", "Automatically splits when SCP-079 is activated");

    settings.Add("splitOn008", false, "Split on SCP-008", "Standard");
    settings.SetToolTip("splitOn008", "Automatically splits when SCP-008 is contained.");

    settings.Add("splitOnFirstElectrical", false, "Split when you disable remote door control.", "Standard");
    settings.SetToolTip("splitOnFirstElectrical", "Will split when you disable remote door control for the first time.");

    settings.Add("splitOnSecondElectrical", false, "Split when you enable remote door control", "Standard");
    settings.SetToolTip("splitOnSecondElectrical", "Split when you enable remote door control.");
}

init {

    vars.Splits = new HashSet<string>();

    vars.playerOnSurface = false;
    
    IntPtr runStartTime    = IntPtr.Zero;
    IntPtr eventState008   = IntPtr.Zero;
    IntPtr pointer079Start = IntPtr.Zero;
    IntPtr playerZone      = IntPtr.Zero;
    IntPtr rdc             = IntPtr.Zero;
    IntPtr sl              = IntPtr.Zero;
    
    foreach (var page in game.MemoryPages(true)) {
	var scanner = new SignatureScanner(game, page.BaseAddress, (int)page.RegionSize);
	if (runStartTime == IntPtr.Zero) {
	    runStartTime = scanner.Scan(new SigScanTarget(2, "89 05 ?? ?? ?? ?? E9 ?? ?? ?? ?? BB ?? ?? ?? ??"));
	} else {
	    break;
	}

	if (pointer079Start == IntPtr.Zero) {
	    pointer079Start = vars.pointer079Start = scanner.Scan(new SigScanTarget(2, "03 35 ?? ?? ?? ?? 89 1E 8B 1D ?? ?? ?? ?? 8B 75 18 21 F3 21 DB"));
	} else {
	    break;
	}

	if (playerZone == IntPtr.Zero) {
	    playerZone = vars.playerZone = scanner.Scan(new SigScanTarget(7, "E9 ?? ?? ?? ?? C7 05 ?? ?? ?? ?? ?? ?? ?? ?? C7 05 ?? ?? ?? ?? ?? ?? ?? ?? C7 45 E0 ?? ?? ?? ?? 81 EC"));
	} else {
	    break;
	}

	if (rdc == IntPtr.Zero) {
	    rdc = vars.rdc = scanner.Scan(new SigScanTarget(15, "C7 44 24 04 ?? ?? ?? ?? E8 ?? ?? ?? ?? 89 05 ?? ?? ?? ?? 81 EC ?? ?? ?? ?? 8B 45 D4 89 04 24 E8 ?? ?? ?? ??"));
	} else {
	    break; 
	}
    }

    if (runStartTime != IntPtr.Zero) {
	print("RunStartTime found at: " + runStartTime.ToString("X"));    
    } else {
	print("Failed to find RunStartTime.");
	throw new Exception("Cant find RunStartTime");
    }

    if (pointer079Start != IntPtr.Zero) {
	print("079 Achievement found at: " + pointer079Start.ToString("X"));
    } else {
	throw new Exception("Can't find 079 achievement.");
    }

    if (playerZone != IntPtr.Zero) {
	print("Player Zone found at: " + playerZone.ToString("X"));
    } else {
	throw new Exception("Can't find player zone.");
    }

    if (rdc != IntPtr.Zero) {
	print("Remote Door Control found at: " + rdc.ToString("X"));
    } else {
	throw new Exception("Can't find remote door control.");
    }
    
    game.Suspend();
    
    vars.bytes = game.ReadBytes(runStartTime, 4);
    int bytes32 = BitConverter.ToInt32(vars.bytes, 0);
    IntPtr bytesPtr = new IntPtr(bytes32);   
    
    vars.achievement079Bytes = game.ReadBytes(pointer079Start, 4);
    int bytes079 = BitConverter.ToInt32(vars.achievement079Bytes, 0);
    IntPtr pointer079 = new IntPtr(bytes079);
    
    vars.pointer079Bytes = game.ReadBytes(pointer079, 4);
    int bytes079Achievement = BitConverter.ToInt32(vars.pointer079Bytes, 0);
    IntPtr achievementsStart = new IntPtr(bytes079Achievement);

    IntPtr achievementOmni = achievementsStart + 0x78;
    IntPtr achievement079 = achievementsStart + 0x14;

    vars.playerZoneBytes = game.ReadBytes(playerZone, 4);
    int bytesPlayerZone = BitConverter.ToInt32(vars.playerZoneBytes, 0);
    IntPtr zone = new IntPtr(bytesPlayerZone);

    vars.rdcBytes = game.ReadBytes(rdc, 4);
    int bytesRDC = BitConverter.ToInt32(vars.rdcBytes, 0);
    IntPtr doorControl = new IntPtr(bytesRDC);
    
    vars.runStartTime = new MemoryWatcher<int>(bytesPtr);
    vars.gameTime     = new MemoryWatcher<int>(bytesPtr + 0x18);
    vars.runFinished  = new MemoryWatcher<int>(bytesPtr + 0x20);
    vars.gotOmni      = new MemoryWatcher<int>(achievementOmni);
    vars.room2sl      = new MemoryWatcher<int>(bytesPtr + 0x98);
    vars.contained008 = new MemoryWatcher<int>(bytesPtr + 0x9C);
    vars.activated079 = new MemoryWatcher<int>(achievement079);
    vars.currentZone  = new MemoryWatcher<int>(zone);
    vars.remoteDoorControl = new MemoryWatcher<int>(doorControl);
  
    vars.watchers = new MemoryWatcherList() { vars.runStartTime, vars.gameTime, vars.runFinished, vars.contained008, vars.activated079, vars.currentZone, vars.remoteDoorControl, vars.gotOmni, vars.room2sl };

    game.Resume();       
}

start {    
    return vars.runStartTime.Current != 0;
}

split {

    //For people who just want auto start and stop.
    if (!settings["Standard"]) {
	return vars.runFinished.Current == 1;
    }

    //Split on reaching heavy containment for the first time.
    if (vars.currentZone.Current == 1 && vars.currentZone.Old == 0 && !vars.Splits.Contains("l2h")) {
	return vars.Splits.Add("l2h");
    }

    //Split on reaching entrance zone for the first time.
    if (vars.currentZone.Current == 2 && vars.currentZone.Old == 1 && !vars.Splits.Contains("h2e")) {
	return vars.Splits.Add("h2e");
    }

    //Split on player reaching the surface.
    if (vars.playerOnSurface && !vars.Splits.Contains("surface")) {
	return vars.Splits.Add("surface");
    }

    //Split when the run is finished.
    if (vars.runFinished.Current == 1 && !vars.Splits.Contains("finished")) {
	return vars.Splits.Add("finished");
    }

    //Split on getting an omni card.
    if (settings["splitOnOmni"]) {
	if (vars.gotOmni.Current == 1 && !vars.Splits.Contains("omni")) {
	    return vars.Splits.Add("omni");
	}		
    }

    //Split on disablind the light containment lockdown.
    if (settings["splitOnSl"]) {
	if (vars.room2sl.Current == 1 && vars.room2sl.Old == 0 && !vars.Splits.Contains("sl")) {
	    return vars.Splits.Add("sl");
	}
    }
    
    //Split on 008 containment.
    if (settings["splitOn008"]) {
	if (vars.contained008.Current == 1 && vars.contained008.Old == 0 && !vars.Splits.Contains("008")) {
	    return vars.Splits.Add("008");
	}
    }

    //Split when you disable remote door control.
    if (settings["splitOnFirstElectrical"]) {
	if (vars.remoteDoorControl.Current == 0 && vars.remoteDoorControl.Old == 1 && !vars.Splits.Contains("electrical1")) {
	    return vars.Splits.Add("electrical1");
	}
    }
	
    //Split on SCP079 activation.
    if (settings["splitOn079"]) {
	if (vars.activated079.Current == 1 && vars.activated079.Old == 0 && !vars.Splits.Contains("079")) {
	    return vars.Splits.Add("079"); 
	}
    }

    //Split when you enable remote door control.
    if (settings["splitOnSecondElectrical"]) {
	if (vars.remoteDoorControl.Current == 1 && vars.remoteDoorControl.Old == 0 && vars.Splits.Contains("079")  && !vars.Splits.Contains("electrical2")) {
	    return vars.Splits.Add("electrical2");
	}
    }
}

gameTime {
    if (vars.gameTime.Current == 0) {
	return TimeSpan.Zero;
    }
    return new TimeSpan(0, 0, 0, 0, vars.gameTime.Current);
}

update {
    vars.playerOnSurface = ((current.playerY > 41.0 && current.playerY < 43.0) || (current.playerY > 499.0 && current.playerY < 503.0));
    vars.watchers.UpdateAll(game);
    
    //print("Run Start Time: " + vars.runStartTime.Current.ToString());
    //print("Real Time     : " + vars.realTime.Current.ToString());
    //print("Game Time     : " + vars.gameTime.Current.ToString());
    //print("Run Finished  : " + vars.runFinished.Current.ToString());
    //print("Contained 008 : " + vars.contained008.Current.ToString());
    //print("Activated 079 : " + vars.activated079.Current.ToString());
    //print("Player Zone   : " + vars.currentZone.Current.ToString());
    //print("RDC           : " + vars.remoteDoorControl.Current.ToString());
}

reset {
    return vars.runStartTime.Current == 0;
}

onStart {
    vars.Splits.Clear(); 
}

exit {
    vars.Splits.Clear();
}
