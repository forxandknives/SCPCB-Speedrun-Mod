//Autospliter for SCP Containment Breach Speedrun Mod
//Made by Forx
//December 10 2023

state("SCP - Containment Breach Speedrun Mod", "Speedrun Mod") {
    
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
}

init {
  IntPtr ptr = IntPtr.Zero;
  foreach (var page in game.MemoryPages(true)) {
    var scanner = new SignatureScanner(game, page.BaseAddress, (int)page.RegionSize);
    if (ptr == IntPtr.Zero) {
      ptr = scanner.Scan(new SigScanTarget(2, "89 05 ?? ?? ?? ?? E9 ?? ?? ?? ?? BB ?? ?? ?? ??"));
    } else {
      break;
    }
  }

  if (ptr != IntPtr.Zero) {
    print("RunStartTime found at: " + ptr.ToString("X"));    
  } else {
    print("Failed to find RunStartTime.");
    throw new Exception();
  }

  vars.bytes = game.ReadBytes(ptr, 4);
  int bytes32 = BitConverter.ToInt32(vars.bytes, 0);
  IntPtr bytesPtr = new IntPtr(bytes32);
  //print("Bytes Ptr: " + bytesPtr.ToString("X"));  

  vars.runStartTime = new MemoryWatcher<int>(bytesPtr);
  //vars.realTime     = new MemoryWatcher<int>(bytesPtr + 0x10);
  vars.gameTime     = new MemoryWatcher<int>(bytesPtr + 0x18);
  vars.runFinished  = new MemoryWatcher<int>(bytesPtr + 0x20);
  
  vars.watchers = new MemoryWatcherList() { vars.runStartTime, /*vars.realTime,*/ vars.gameTime, vars.runFinished };
  
}

start {
  return vars.runStartTime.Current != 0;
}

split {
  return vars.runFinished.Current == 1;
}

gameTime {
  if (vars.gameTime.Current == 0) {
    return TimeSpan.Zero;
  }
  return new TimeSpan(0, 0, 0, 0, vars.gameTime.Current);
}

update {
  vars.watchers.UpdateAll(game);
  //print("Run Start Time: " + vars.runStartTime.Current.ToString());
  //print("Real Time     : " + vars.realTime.Current.ToString());
  //print("Game Time     : " + vars.gameTime.Current.ToString());
  //print("Run Finished  : " + vars.runFinished.Current.ToString());
}

reset {
  return vars.runStartTime.Current == 0;
}
