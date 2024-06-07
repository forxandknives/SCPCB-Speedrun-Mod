Type Demos

	Field tick%

	Field gt%
	
	Field px#
	Field py#
	Field pz#	

	Field hx#
	Field hy#
	Field hz#
	
	Field pitch#
	Field yaw#
	Field roll#
	
	Field doorx#
	Field doory#
	Field doorz#
	
	Field demoDoors%[300]
		
End Type

Function DemoMain()

	Repeat 
			
		;This will track how many times the main loop runs in each second, and the average run time for each loop.
		If DebugHUD2 Then							
		
			If StartMainLoopTime = 0 Then
				StartMainLoopTime = MilliSecs()
				TimeThisMainLoop = MilliSecs()
			Else
				If EndMainLoopTime - StartMainLoopTime >= 1000
					FinalLoopCount = MainLoopCount
					If FinalLoopCount <> 0
						FinalLoopTime = AverageMainLoopTime / FinalLoopCount
					EndIf
					
					DebugLog "-----------------------------------------"
					DebugLog "StartMainLoopTime:" + StartMainLoopTime
					DebugLog "EndMainLoopTime: " + EndMainLoopTime
					DebugLog "MainLoopCount: " + MainLoopCount
					DebugLog "TimeThisMainLoop: " + TimeThisMainLoop
					DebugLog "AverageMainLoopTime: " + AverageMainLoopTime
					DebugLog "FinalLoopCount: " + FinalLoopCount
					DebugLog "FinalLoopTime: " + FinalLoopTime
					DebugLog "-----------------------------------------"
					
					;Stop	
					
					MainLoopCount = 0
					AverageMainLoopTime = 0
					StartMainLoopTime = MilliSecs()
					TimeThisMainLoop = MilliSecs()
				Else 				
				
					MainLoopCount = MainLoopCount + 1				
					AverageMainLoopTime = AverageMainLoopTime + (EndMainLoopTime - TimeThisMainLoop)							
					TimeThisMainLoop = MilliSecs()	
					
				EndIf
			EndIf
		EndIf
		
		cls
		
		If ShouldIncreasePlayTime Then
			PlayTime = MilliSecs() - RunStartTime
		EndIf
		
		If Not RunFinished Then
			GameTime = PlayTime - LoadTime; + LoadFromMenuGameTime
		EndIf		
				
		CurTime = MilliSecs2()
		ElapsedTime = (CurTime - PrevTime) / 1000.0
		PrevTime = CurTime
		PrevFPSFactor = FPSfactor
		FPSfactor = Max(Min(ElapsedTime * 70, 5.0), 0.2)
		FPSfactor2 = FPSfactor
		
		FPSFactor = FPSFactor * DemoTimescale
		
		If DemoPaused Or MenuOpen Or InvOpen Or OtherOpen<>Null Or ConsoleOpen Or SelectedDoor <> Null Or SelectedScreen <> Null Or Using294 Then FPSfactor = 0	
		
		If Framelimit > 0 Then
		    ;Framelimit
			Local WaitingTime% = (1000.0 / Framelimit) - (MilliSecs2() - LoopDelay)
			Delay WaitingTime%
			
			LoopDelay = MilliSecs2()
		EndIf
		
		;Counting the fps
		If CheckFPS < MilliSecs2() Then
			FPS = ElapsedLoops
			ElapsedLoops = 0
			CheckFPS = MilliSecs2()+1000
		EndIf
		ElapsedLoops = ElapsedLoops + 1
		
		If Input_ResetTime<=0.0
			DoubleClick = False
			MouseHit1 = MouseHit(1)
			If MouseHit1 Then
				If MilliSecs2() - LastMouseHit1 < 800 Then DoubleClick = True
				LastMouseHit1 = MilliSecs2()
			EndIf
			
			Local prevmousedown1 = MouseDown1
			MouseDown1 = MouseDown(1)
			If prevmousedown1 = True And MouseDown1=False Then MouseUp1 = True Else MouseUp1 = False
			
			MouseHit2 = MouseHit(2)
			
			If (Not MouseDown1) And (Not MouseHit1) Then GrabbedEntity = 0
		Else
			Input_ResetTime = Max(Input_ResetTime-FPSfactor,0.0)
		EndIf
		
		UpdateMusic()
		If EnableSFXRelease Then AutoReleaseSounds()
		
		If MainMenuOpen Then
		
			For demo.Demos = Each Demos
				Delete demo
			Next
		
			Goto EndOfMain
						
		Else
			UpdateStreamSounds()
			
			ShouldPlay = Min(PlayerZone,2)
			
			DrawHandIcon = False
			
			RestoreSanity = True
			ShouldEntitiesFall = True
			
			If FPSfactor > 0 And PlayerRoom\RoomTemplate\Name <> "dimension1499" Then UpdateSecurityCams()
			
			If PlayerRoom\RoomTemplate\Name <> "pocketdimension" And PlayerRoom\RoomTemplate\Name <> "gatea" And PlayerRoom\RoomTemplate\Name <> "exit1" And (Not MenuOpen) And (Not ConsoleOpen) And (Not InvOpen) Then 
				
				If Rand(1500) = 1 Then
					For i = 0 To 5
						If AmbientSFX(i,CurrAmbientSFX)<>0 Then
							If ChannelPlaying(AmbientSFXCHN)=0 Then FreeSound_Strict AmbientSFX(i,CurrAmbientSFX) : AmbientSFX(i,CurrAmbientSFX) = 0
						EndIf			
					Next
					
					PositionEntity (SoundEmitter, EntityX(Camera) + Rnd(-1.0, 1.0), 0.0, EntityZ(Camera) + Rnd(-1.0, 1.0))
					
					If Rand(3)=1 Then PlayerZone = 3
					
					If PlayerRoom\RoomTemplate\Name = "173" Then 
						PlayerZone = 4
					ElseIf PlayerRoom\RoomTemplate\Name = "room860"
						For e.Events = Each Events
							If e\EventName = "room860"
								If e\EventState = 1.0
									PlayerZone = 5
									PositionEntity (SoundEmitter, EntityX(SoundEmitter), 30.0, EntityZ(SoundEmitter))
								EndIf
								
								Exit
							EndIf
						Next
					EndIf
					
					CurrAmbientSFX = Rand(0,AmbientSFXAmount(PlayerZone)-1)
					
					Select PlayerZone
						Case 0,1,2
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Zone"+(PlayerZone+1)+"\ambient"+(CurrAmbientSFX+1)+".ogg")
						Case 3
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\General\ambient"+(CurrAmbientSFX+1)+".ogg")
						Case 4
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Pre-breach\ambient"+(CurrAmbientSFX+1)+".ogg")
						Case 5
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Forest\ambient"+(CurrAmbientSFX+1)+".ogg")
					End Select
					
					AmbientSFXCHN = PlaySound2(AmbientSFX(PlayerZone,CurrAmbientSFX), Camera, SoundEmitter)
				EndIf
				UpdateSoundOrigin(AmbientSFXCHN,Camera, SoundEmitter)
				
				If Rand(50000) = 3 Then
					Local RN$ = PlayerRoom\RoomTemplate\Name$
					If RN$ <> "room860" And RN$ <> "room1123" And RN$ <> "173" And RN$ <> "dimension1499" Then
						If FPSfactor > 0 Then LightBlink = Rnd(1.0,2.0)
						PlaySound_Strict  LoadTempSound("SFX\SCP\079\Broadcast"+Rand(1,7)+".ogg")
					EndIf 
				EndIf
			EndIf
			
			UpdateCheckpoint1 = False
			UpdateCheckpoint2 = False
			
			
			If (Not DemoPaused) And (Not MenuOpen) And (Not InvOpen) And (OtherOpen=Null) And (SelectedDoor = Null) And (ConsoleOpen = False) And (Using294 = False) And (SelectedScreen = Null) And EndingTimer=>0 Then
			
				PlayDemo()
			
				LightVolume = CurveValue(TempLightVolume, LightVolume, 50.0)
				CameraFogRange(Camera, CameraFogNear*LightVolume,CameraFogFar*LightVolume)
				CameraFogColor(Camera, 0,0,0)
				CameraFogMode Camera,1
				CameraRange(Camera, 0.05, Min(CameraFogFar*LightVolume*1.5,28))	
				If PlayerRoom\RoomTemplate\Name<>"pocketdimension" Then
					CameraClsColor(Camera, 0,0,0)
				EndIf
				
				AmbientLight Brightness, Brightness, Brightness	
				PlayerSoundVolume = CurveValue(0.0, PlayerSoundVolume, 5.0)
				
				CanSave% = True
				UpdateDeafPlayer()
				UpdateEmitters()
				DemoMouseLook()
				If PlayerRoom\RoomTemplate\Name = "dimension1499" And QuickLoadPercent > 0 And QuickLoadPercent < 100
					ShouldEntitiesFall = False
				EndIf
				MovePlayer()
				InFacility = CheckForPlayerInFacility()
				If PlayerRoom\RoomTemplate\Name = "dimension1499"
					If QuickLoadPercent = -1 Or QuickLoadPercent = 100
						UpdateDimension1499()
					EndIf
					UpdateLeave1499()
				ElseIf PlayerRoom\RoomTemplate\Name = "gatea" Or (PlayerRoom\RoomTemplate\Name="exit1" And EntityY(Collider)>1040.0*RoomScale)
					UpdateDoors()
					If QuickLoadPercent = -1 Or QuickLoadPercent = 100
						UpdateEndings()
					EndIf
					UpdateScreens()
					UpdateRoomLights(Camera)
				Else
					UpdateDoors()
					If QuickLoadPercent = -1 Or QuickLoadPercent = 100
						UpdateEvents()
					EndIf
					UpdateScreens()
					TimeCheckpointMonitors()
					Update294()
					UpdateRoomLights(Camera)
				EndIf
				UpdateDecals()
				UpdateMTF()
				UpdateNPCs()
				UpdateItems()
				UpdateParticles()
				Use427()
				UpdateMonitorSaving()
				;Added a simple code for updating the Particles function depending on the FPSFactor (still WIP, might not be the final version of it) - ENDSHN
				UpdateParticles_Time# = Min(1,UpdateParticles_Time#+FPSfactor)
				If UpdateParticles_Time#=1
					UpdateDevilEmitters()
					UpdateParticles_Devil()
					UpdateParticles_Time#=0
				EndIf
			EndIf
			
			If InfiniteStamina% Then Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
			
			; HERE
							
			If FPSfactor=0
				UpdateWorld(0)
			Else
				UpdateWorld()
				ManipulateNPCBones()
			EndIf				
			RenderWorld2()
			
			BlurVolume = Min(CurveValue(0.0, BlurVolume, 20.0),0.95)
			If BlurTimer > 0.0 Then
				BlurVolume = Max(Min(0.95, BlurTimer / 1000.0), BlurVolume)
				BlurTimer = Max(BlurTimer - FPSfactor, 0.0)
			End If
			
			UpdateBlur(BlurVolume)
			
			;[Block]
			
			Local darkA# = 0.0
			If (Not MenuOpen)  Then
				If Sanity < 0 Then
					If RestoreSanity Then Sanity = Min(Sanity + FPSfactor, 0.0)
					If Sanity < (-200) Then 
						darkA = Max(Min((-Sanity - 200) / 700.0, 0.6), darkA)
						If KillTimer => 0 Then 
							HeartBeatVolume = Min(Abs(Sanity+200)/500.0,1.0)
							HeartBeatRate = Max(70 + Abs(Sanity+200)/6.0,HeartBeatRate)
						EndIf
					EndIf
				End If
				
				If EyeStuck > 0 Then 
					BlinkTimer = BLINKFREQ
					EyeStuck = Max(EyeStuck-FPSfactor,0)
					
					If EyeStuck < 9000 Then BlurTimer = Max(BlurTimer, (9000-EyeStuck)*0.5)
					If EyeStuck < 6000 Then darkA = Min(Max(darkA, (6000-EyeStuck)/5000.0),1.0)
					If EyeStuck < 9000 And EyeStuck+FPSfactor =>9000 Then 
						Msg = "The eyedrops are causing your eyes to tear up."
						MsgTimer = 70*6
					EndIf
				EndIf
				
				If BlinkTimer < 0 Then
					If BlinkTimer > - 5 Then
						darkA = Max(darkA, Sin(Abs(BlinkTimer * 18.0)))
					ElseIf BlinkTimer > - 15
						darkA = 1.0
					Else
						darkA = Max(darkA, Abs(Sin(BlinkTimer * 18.0)))
					EndIf
					
					If BlinkTimer <= - 20 Then
						;Randomizes the frequency of blinking. Scales with difficulty.
						Select SelectedDifficulty\otherFactors
							Case EASY
								BLINKFREQ = Rnd(490,700)
							Case NORMAL
								BLINKFREQ = Rnd(455,665)
							Case HARD
								BLINKFREQ = Rnd(420,630)
						End Select 
						BlinkTimer = BLINKFREQ
					EndIf
					
					BlinkTimer = BlinkTimer - FPSfactor
				Else
					BlinkTimer = BlinkTimer - FPSfactor * 0.6 * BlinkEffect
					If EyeIrritation > 0 Then BlinkTimer=BlinkTimer-Min(EyeIrritation / 100.0 + 1.0, 4.0) * FPSfactor
					
					darkA = Max(darkA, 0.0)
				End If
				
				EyeIrritation = Max(0, EyeIrritation - FPSfactor)
				
				If BlinkEffectTimer > 0 Then
					BlinkEffectTimer = BlinkEffectTimer - (FPSfactor/70)
				Else
					If BlinkEffect <> 1.0 Then BlinkEffect = 1.0
				EndIf
				
				LightBlink = Max(LightBlink - (FPSfactor / 35.0), 0)
				If LightBlink > 0 Then darkA = Min(Max(darkA, LightBlink * Rnd(0.3, 0.8)), 1.0)
				
				If Using294 Then darkA=1.0
				
				If (Not WearingNightVision) Then darkA = Max((1.0-SecondaryLightOn)*0.9, darkA)
				
				If KillTimer < 0 Then				
				
					If DeathEndTime = 0 Then
						DeathEndTime = MilliSecs()
					EndIf
					
					If ShowDeathTime Then
						Local deathTime% = DeathEndTime - DeathStartTime
						
						Local sec% = deathTime / 1000
						Local msec% = deathTime Mod 1000
						
						AAText(MonitorWidth / 2, MonitorHeight * 0.90, "Died in " + Str(sec) + "." + Str(msec) + " seconds.", True, True)
					EndIf
					
					InvOpen = False
					SelectedItem = Null
					SelectedScreen = Null
					SelectedMonitor = Null
					BlurTimer = Abs(KillTimer*5)
					KillTimer=KillTimer-(FPSfactor*0.8)
					If KillTimer < - 360 Then 
						MenuOpen = True 
						If SelectedEnding <> "" Then EndingTimer = Min(KillTimer,-0.1)
					EndIf
					darkA = Max(darkA, Min(Abs(KillTimer / 400.0), 1.0))
				EndIf
				
				If FallTimer < 0 Then
					If SelectedItem <> Null Then
						If Instr(SelectedItem\itemtemplate\tempname,"hazmatsuit") Or Instr(SelectedItem\itemtemplate\tempname,"vest") Then
							If WearingHazmat=0 And WearingVest=0 Then
								DropItem(SelectedItem)
							EndIf
						EndIf
					EndIf
					InvOpen = False
					SelectedItem = Null
					SelectedScreen = Null
					SelectedMonitor = Null
					BlurTimer = Abs(FallTimer*10)
					FallTimer = FallTimer-FPSfactor
					darkA = Max(darkA, Min(Abs(FallTimer / 400.0), 1.0))				
				EndIf
				
				If SelectedItem <> Null Then
					If SelectedItem\itemtemplate\tempname = "navigator" Or SelectedItem\itemtemplate\tempname = "nav" Then darkA = Max(darkA, 0.5)
				End If
				If SelectedScreen <> Null Then darkA = Max(darkA, 0.5)
				
				EntityAlpha(Dark, darkA)	
			EndIf
			
			If LightFlash > 0 Then
				ShowEntity Light
				EntityAlpha(Light, Max(Min(LightFlash + Rnd(-0.2, 0.2), 1.0), 0.0))
				LightFlash = Max(LightFlash - (FPSfactor / 70.0), 0)
			Else
				HideEntity Light
				;EntityAlpha(Light, LightFlash)
			EndIf
			
			EntityColor Light,255,255,255
			
			;[End block]
			
			If KeyHit(25) And (Not ConsoleOpen) Then
				;If DemoUIOpen Then
				;	ResumeSounds()
				;	MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
				;Else
				;	PauseSounds()
				;EndIf
				DemoUIOpen = Not DemoUIOpen
			EndIf
			
			If KeyHit(KEY_INV) And VomitTimer >= 0 Then
				If (Not UnableToMove) And (Not IsZombie) And (Not Using294) Then
					Local W$ = ""
					Local V# = 0
					If SelectedItem<>Null
						W$ = SelectedItem\itemtemplate\tempname
						V# = SelectedItem\state
					EndIf
					If (W<>"vest" And W<>"finevest" And W<>"hazmatsuit" And W<>"hazmatsuit2" And W<>"hazmatsuit3") Or V=0 Or V=100
						If InvOpen Then
							ResumeSounds()
							MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
						Else
							PauseSounds()
						EndIf
						InvOpen = Not InvOpen
						If OtherOpen<>Null Then OtherOpen=Null
						SelectedItem = Null
					EndIf
				EndIf
			EndIf
			
			If KeyHit(KEY_SAVE) Then
				If SelectedDifficulty\saveType = SAVEANYWHERE Then
					RN$ = PlayerRoom\RoomTemplate\Name$
					If RN$ = "173" Or (RN$ = "exit1" And EntityY(Collider)>1040.0*RoomScale) Or RN$ = "gatea"
						Msg = "You cannot save in this location."
						MsgTimer = 70 * 4
						;SetSaveMSG("You cannot save in this location.")
					ElseIf (Not CanSave) Or QuickLoadPercent > -1
						Msg = "You cannot save at this moment."
						MsgTimer = 70 * 4
						;SetSaveMSG("You cannot save at this moment.")
						If QuickLoadPercent > -1
							Msg = Msg + " (game is loading)"
							;Save_MSG = Save_MSG + " (game is loading)"
						EndIf
					Else
						SaveGame(SavePath + CurrSave + "\")
					EndIf
				ElseIf SelectedDifficulty\saveType = SAVEONSCREENS
					If SelectedScreen=Null And SelectedMonitor=Null Then
						Msg = "Saving is only permitted on clickable monitors scattered throughout the facility."
						MsgTimer = 70 * 4
						;SetSaveMSG("Saving is only permitted on clickable monitors scattered throughout the facility.")
					Else
						RN$ = PlayerRoom\RoomTemplate\Name$
						If RN$ = "173" Or (RN$ = "exit1" And EntityY(Collider)>1040.0*RoomScale) Or RN$ = "gatea"
							Msg = "You cannot save in this location."
							MsgTimer = 70 * 4
							;SetSaveMSG("You cannot save in this location.")
						ElseIf (Not CanSave) Or QuickLoadPercent > -1
							Msg = "You cannot save at this moment."
							MsgTimer = 70 * 4
							;SetSaveMSG("You cannot save at this moment.")
							If QuickLoadPercent > -1
								Msg = Msg + " (game is loading)"
								;Save_MSG = Save_MSG + " (game is loading)"
							EndIf
						Else
							If SelectedScreen<>Null
								GameSaved = False
								Playable = True
								DropSpeed = 0
							EndIf
							SaveGame(SavePath + CurrSave + "\")
						EndIf
					EndIf
				Else
					Msg = "Quick saving is disabled."
					MsgTimer = 70 * 4
					;SetSaveMSG("Quick saving is disabled.")
				EndIf
			Else If SelectedDifficulty\saveType = SAVEONSCREENS And (SelectedScreen<>Null Or SelectedMonitor<>Null)
				If (Msg<>"Game progress saved." And Msg<>"You cannot save in this location."And Msg<>"You cannot save at this moment.") Or MsgTimer<=0 Then
					Msg = "Press "+KeyName(KEY_SAVE)+" to save."
					MsgTimer = 70*4
					;SetSaveMSG("Press "+KeyName(KEY_SAVE)+" to save.")
				EndIf
				
				If MouseHit2 Then SelectedMonitor = Null
			EndIf
			
			If KeyHit(KEY_CONSOLE) Then
				If CanOpenConsole
					If ConsoleOpen Then
						UsedConsole = True
						ResumeSounds()
						MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0 
					Else
						PauseSounds()
					EndIf
					
					;ConsoleOpen = (Not ConsoleOpen)
					If Not(ConsoleOpen) Then
						ConsoleOpen = True
						CursorIndex = Len(ConsoleInput)
					Else
						ConsoleOpen = False
					EndIf
					
					FlushKeys()
				EndIf
			EndIf
			
			DrawGUI()
			
			If EndingTimer < 0 Then
				If SelectedEnding <> "" Then DrawEnding()
			Else
				DrawMenu()			
			EndIf
			
			UpdateConsole()
			
			If PlayerRoom <> Null Then
				If PlayerRoom\RoomTemplate\Name = "173" Then
					For e.Events = Each Events
						If e\EventName = "173" Then
							If e\EventState3 => 40 And e\EventState3 < 50 Then
								If InvOpen Then
									Msg = "Double click on the document to view it."
									MsgTimer=70*7
									e\EventState3 = 50
								EndIf
							EndIf
						EndIf
					Next
				EndIf
			EndIf
			
			If MsgTimer > 0 Then
				Local temp% = False
				If (Not InvOpen%)
					If SelectedItem <> Null
						If SelectedItem\itemtemplate\tempname = "paper" Or SelectedItem\itemtemplate\tempname = "oldpaper"
							temp% = True
						EndIf
					EndIf
				EndIf
				
				If (Not temp%)
					Color 0,0,0
					AAText((GraphicWidth / 2)+1, (GraphicHeight / 2) + 201, Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
					Color 255,255,255;Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255)
					If Left(Msg,14)="Blitz3D Error!" Then
						Color 255,0,0
					EndIf
					AAText((GraphicWidth / 2), (GraphicHeight / 2) + 200, Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
				Else
					Color 0,0,0
					AAText((GraphicWidth / 2)+1, (GraphicHeight * 0.94) + 1, Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
					Color 255,255,255;Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255)
					If Left(Msg,14)="Blitz3D Error!" Then
						Color 255,0,0
					EndIf
					AAText((GraphicWidth / 2), (GraphicHeight * 0.94), Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
				EndIf
				MsgTimer=MsgTimer-FPSfactor2 
			End If
			
			Color 255, 255, 255
			If ShowFPS Then AASetFont ConsoleFont : AAText 20, 20, "FPS: " + FPS : AASetFont Font1
			
			DrawQuickLoading()
			
			UpdateAchievementMsg()
			;UpdateSaveMSG()
		End If
		
		If BorderlessWindowed Then
			If (RealGraphicWidth<>GraphicWidth) Or (RealGraphicHeight<>GraphicHeight) Then
				setbuffer TextureBuffer(fresize_texture)
				clscolor 0,0,0 : cls
				CopyRect 0,0,GraphicWidth,GraphicHeight,1024-GraphicWidth/2,1024-GraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
				setbuffer BackBuffer()
				clscolor 0,0,0 : cls
				ScaleRender(0,0,2050.0 / Float(GraphicWidth) * AspectRatioRatio, 2050.0 / Float(GraphicWidth) * AspectRatioRatio)
				;might want to replace Float(GraphicWidth) with Max(GraphicWidth,GraphicHeight) if portrait sizes cause issues
				;everyone uses landscape so it's probably a non-issue
			EndIf
		EndIf
		
		;not by any means a perfect solution
		;Not even proper gamma correction but it's a nice looking alternative that works in windowed mode
		If ScreenGamma>1.0 Then
			CopyRect 0,0,RealGraphicWidth,RealGraphicHeight,1024-RealGraphicWidth/2,1024-RealGraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
			EntityBlend fresize_image,1
			clscolor 0,0,0 : cls
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			EntityFX fresize_image,1+32
			EntityBlend fresize_image,3
			EntityAlpha fresize_image,ScreenGamma-1.0
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
		ElseIf ScreenGamma<1.0 Then ;todo: maybe optimize this if it's too slow, alternatively give players the option to disable gamma
			CopyRect 0,0,RealGraphicWidth,RealGraphicHeight,1024-RealGraphicWidth/2,1024-RealGraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
			EntityBlend fresize_image,1
			clscolor 0,0,0 : cls
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			EntityFX fresize_image,1+32
			EntityBlend fresize_image,2
			EntityAlpha fresize_image,1.0
			setbuffer TextureBuffer(fresize_texture2)
			clscolor 255*ScreenGamma,255*ScreenGamma,255*ScreenGamma
			cls
			setbuffer BackBuffer()
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			setbuffer(TextureBuffer(fresize_texture2))
			clscolor 0,0,0
			cls
			setbuffer(BackBuffer())
		EndIf
		EntityFX fresize_image,1
		EntityBlend fresize_image,1
		EntityAlpha fresize_image,1.0
		
		OldPlayerRoom = PlayerRoom
		
		If DebugHUD2 Then			
			EndMainLoopTime = MilliSecs()
		EndIf
		
		CatchErrors("Demo Main loop / uncaught")
		
		If Vsync = 0 Then
			Flip 0
		Else 
			Flip 1
		EndIf
	Forever

.EndOfMain

End Function

Function DemoMouseLook()
	Local i%
	
	CameraShake = Max(CameraShake - (FPSfactor / 10), 0)
	
	;CameraZoomTemp = CurveValue(CurrCameraZoom,CameraZoomTemp, 5.0)
	CameraZoom(Camera, Min(1.0+(CurrCameraZoom/400.0),1.1))
	CurrCameraZoom = Max(CurrCameraZoom - FPSfactor, 0)
	
	If KillTimer >= 0 And FallTimer >=0 Then
		
		HeadDropSpeed = 0
		
		;If 0 Then 
		;fixing the black screen bug with some bubblegum code 
		Local Zero# = 0.0
		Local Nan1# = 0.0 / Zero
		If Int(EntityX(Collider))=Int(Nan1) Then
			
			PositionEntity Collider, EntityX(Camera, True), EntityY(Camera, True) - 0.5, EntityZ(Camera, True), True
			Msg = "EntityX(Collider) = NaN, RESETTING COORDINATES    -    New coordinates: "+EntityX(Collider)
			MsgTimer = 300				
		EndIf
		;EndIf
		
		Local up# = (Sin(Shake) / (20.0+CrouchState*20.0))*0.6;, side# = Cos(Shake / 2.0) / 35.0		
		Local roll# = Max(Min(Sin(Shake/2)*2.5*Min(Injuries+0.25,3.0),8.0),-8.0)
		
		;käännetään kameraa sivulle jos pelaaja on vammautunut
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), Max(Min(up*30*Injuries,50),-50)
		PositionEntity Camera, EntityX(Collider), EntityY(Collider), EntityZ(Collider)
		;RotateEntity Camera, 0, EntityYaw(Collider), roll*0.5
		
		MoveEntity Camera, side, up + 0.6 + CrouchState * -0.3, 0
		
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), 0
		;moveentity player, side, up, 0	
		; -- Update the smoothing que To smooth the movement of the mouse.
		mouse_x_speed_1# = CurveValue(MouseXSpeed() * (MouseSens + 0.6) , mouse_x_speed_1, (6.0 / (MouseSens + 1.0))*MouseSmooth) 
		If Int(mouse_x_speed_1) = Int(Nan1) Then mouse_x_speed_1 = 0
		If PrevFPSFactor>0 Then
            If Abs(FPSfactor/PrevFPSFactor-1.0)>1.0 Then
                ;lag spike detected - stop all camera movement
                mouse_x_speed_1 = 0.0
                mouse_y_speed_1 = 0.0
            EndIf
        EndIf
		If InvertMouse Then
			mouse_y_speed_1# = CurveValue(-MouseYSpeed() * (MouseSens + 0.6), mouse_y_speed_1, (6.0/(MouseSens+1.0))*MouseSmooth) 
		Else
			mouse_y_speed_1# = CurveValue(MouseYSpeed () * (MouseSens + 0.6), mouse_y_speed_1, (6.0/(MouseSens+1.0))*MouseSmooth) 
		EndIf
		If Int(mouse_y_speed_1) = Int(Nan1) Then mouse_y_speed_1 = 0
		
		Local the_yaw# = ((mouse_x_speed_1#)) * mouselook_x_inc# / (1.0+WearingVest)
		Local the_pitch# = ((mouse_y_speed_1#)) * mouselook_y_inc# / (1.0+WearingVest)
		
		TurnEntity Collider, 0.0, -the_yaw#, 0.0 ; Turn the user on the Y (yaw) axis.
		user_camera_pitch# = user_camera_pitch# + the_pitch#
		; -- Limit the user;s camera To within 180 degrees of pitch rotation. ;EntityPitch(); returns useless values so we need To use a variable To keep track of the camera pitch.
		If user_camera_pitch# > 70.0 Then user_camera_pitch# = 70.0
		If user_camera_pitch# < - 70.0 Then user_camera_pitch# = -70.0
		
		;RotateEntity Camera, WrapAngle(user_camera_pitch + Rnd(-CameraShake, CameraShake)), WrapAngle(EntityYaw(Collider) + Rnd(-CameraShake, CameraShake)), roll ; Pitch the user;s camera up And down.
		
		If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
			If EntityY(Collider)<2000*RoomScale Or EntityY(Collider)>2608*RoomScale Then
				RotateEntity Camera, WrapAngle(EntityPitch(Camera)),WrapAngle(EntityYaw(Camera)), roll+WrapAngle(Sin(MilliSecs2()/150.0)*30.0) ; Pitch the user;s camera up And down.
			EndIf
		EndIf
		
	Else
		HideEntity Collider
		PositionEntity Camera, EntityX(Head), EntityY(Head), EntityZ(Head)
		
		Local CollidedFloor% = False
		For i = 1 To CountCollisions(Head)
			If CollisionY(Head, i) < EntityY(Head) - 0.01 Then CollidedFloor = True
		Next
		
		If CollidedFloor = True Then
			HeadDropSpeed# = 0
		Else
			
			If KillAnim = 0 Then 
				MoveEntity Head, 0, 0, HeadDropSpeed
				RotateEntity(Head, CurveAngle(-90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) - 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			Else
				MoveEntity Head, 0, 0, -HeadDropSpeed
				RotateEntity(Head, CurveAngle(90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) + 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			EndIf
			
			HeadDropSpeed# = HeadDropSpeed - 0.002 * FPSfactor
		EndIf
		
		If InvertMouse Then
			TurnEntity (Camera, -MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		Else
			TurnEntity (Camera, MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		End If
		
	EndIf
	
	;pölyhiukkasia
	If ParticleAmount=2
		If Rand(35) = 1 Then
			Local pvt% = CreatePivot()
			PositionEntity(pvt, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True))
			RotateEntity(pvt, 0, Rnd(360), 0)
			If Rand(2) = 1 Then
				MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
			Else
				MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
			End If
			
			Local p.Particles = CreateParticle(EntityX(pvt), EntityY(pvt), EntityZ(pvt), 2, 0.002, 0, 300)
			p\speed = 0.001
			RotateEntity(p\pvt, Rnd(-20, 20), Rnd(360), 0)
			
			p\SizeChange = -0.00001
			
			FreeEntity pvt
		End If
	EndIf
	
	; -- Limit the mouse;s movement. Using this method produces smoother mouselook movement than centering the mouse Each loop.
	If (MouseX() > mouse_right_limit) Or (MouseX() < mouse_left_limit) Or (MouseY() > mouse_bottom_limit) Or (MouseY() < mouse_top_limit)
		;MoveMouse viewport_center_x, viewport_center_y
	EndIf
	
	If WearingGasMask Or WearingHazmat Or Wearing1499 Then
		If Wearing714 = False Then
			If WearingGasMask = 2 Or Wearing1499 = 2 Or WearingHazmat = 2 Then
				Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
			EndIf
		EndIf
		If WearingHazmat = 1 Then
			Stamina = Min(60, Stamina)
		EndIf
		
		ShowEntity(GasMaskOverlay)
	Else
		HideEntity(GasMaskOverlay)
	End If
	
	If (Not WearingNightVision=0) Then
		ShowEntity(NVOverlay)
		If WearingNightVision=2 Then
			EntityColor(NVOverlay, 0,100,255)
			AmbientLightRooms(15)
		ElseIf WearingNightVision=3 Then
			EntityColor(NVOverlay, 255,0,0)
			AmbientLightRooms(15)
		Else
			EntityColor(NVOverlay, 0,255,0)
			AmbientLightRooms(15)
		EndIf
		EntityTexture(Fog, FogNVTexture)
	Else
		AmbientLightRooms(0)
		HideEntity(NVOverlay)
		EntityTexture(Fog, FogTexture)
	EndIf
	
	For i = 0 To 5
		If SCP1025state[i]>0 Then
			Select i
				Case 0 ;common cold
					If FPSfactor>0 Then 
						If Rand(1000)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.3
				Case 1 ;chicken pox
					If Rand(9000)=1 And Msg="" Then
						Msg="Your skin is feeling itchy."
						MsgTimer =70*4
					EndIf
				Case 2 ;cancer of the lungs
					If FPSfactor>0 Then 
						If Rand(800)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.1
				Case 3 ;appendicitis
					;0.035/sec = 2.1/min
					If (Not I_427\Using And I_427\Timer < 70*360) Then
						SCP1025state[i]=SCP1025state[i]+FPSfactor*0.0005
					EndIf
					If SCP1025state[i]>20.0 Then
						If SCP1025state[i]-FPSfactor<=20.0 Then Msg="The pain in your stomach is becoming unbearable."
						Stamina = Stamina - FPSfactor * 0.3
					ElseIf SCP1025state[i]>10.0
						If SCP1025state[i]-FPSfactor<=10.0 Then Msg="Your stomach is aching."
					EndIf
				Case 4 ;asthma
					If Stamina < 35 Then
						If Rand(Int(140+Stamina*8))=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
						CurrSpeed = CurveValue(0, CurrSpeed, 10+Stamina*15)
					EndIf
				Case 5;cardiac arrest
					If (Not I_427\Using And I_427\Timer < 70*360) Then
						SCP1025state[i]=SCP1025state[i]+FPSfactor*0.35
					EndIf
					;35/sec
					If SCP1025state[i]>110 Then
						HeartBeatRate=0
						BlurTimer = Max(BlurTimer, 500)
						If SCP1025state[i]>140 Then 
							DeathMSG = Chr(34)+"He died of a cardiac arrest after reading SCP-1025, that's for sure. Is there such a thing as psychosomatic cardiac arrest, or does SCP-1025 have some "
							DeathMSG = DeathMSG + "anomalous properties we are not yet aware of?"+Chr(34)
							Kill()
						EndIf
					Else
						HeartBeatRate=Max(HeartBeatRate, 70+SCP1025state[i])
						HeartBeatVolume = 1.0
					EndIf
			End Select 
		EndIf
	Next
	
	
End Function

Function RecordDemo()

	If Not demoFile Then
		
		Local time$ = CurrentTime()
		;We have to replace here because windows does not allow : to be in folder names.
		time = Replace(time, ":", ".")
		
		Local date$ = CurrentDate()
		
		demoSavePath$ = DemoPath + date + " " + time
				
		CreateDir(demoSavePath)
				
		If FileType(demoSavePath) = 2 Then
		
			demoFile = WriteFile(demoSavePath + "\demo.txt")
			
		Else
		
			CreateConsoleMsg("Unable to write to file. Parent folder was not created.")
			CreateConsoleMsg("Stopping recording.")
			recordingDemo = False
			demoFile = 0
			
		EndIf
		
		WriteString(demoFile, RandomSeed)
		WriteByte(demoFile, SeedRNGDirectly)
		
		Local doorCount% = 0
		Local amongusDoor.Doors
		For amongusDoors.Doors = Each Doors
			doorCount = doorCount + 1
		Next
		
		WriteByte(demoFile, doorCount)		
		
		demoDelayTime = MilliSecs()
		
		;We do not close the demoPath file until we manually stop the demo.
				
	Else		
		
		If (MilliSecs() - demoDelayTime >= 60) Then ; Arbitrary amount of time (60 ms) to delay saving the user input and what not.		
		
			;WriteLine(demoFile, CurrentTime())
			
			WriteInt(demoFile, DemoTick)
			
			WriteInt(demoFile, GameTime)
			
			WriteFloat(demoFile, EntityX(Collider))
			WriteFloat(demoFile, EntityY(Collider))
			WriteFloat(demoFile, EntityZ(Collider))
			
			WriteFloat(demoFile, EntityX(Head))
			WriteFloat(demoFile, EntityY(Head))
			WriteFloat(demoFile, EntityZ(Head))	
			
			WriteFloat(demoFile, EntityPitch(Camera))
			WriteFloat(demoFile, EntityYaw(Collider))
			WriteFloat(demoFile, EntityRoll(Camera))		
		
			For d.Doors = Each Doors 
			
				; We may have to write the xyz of the doorframeobj if the order of the doors
				; in the linked list are not the same between the live game and the demo.
				; Theoretically since we are loading the live game and the demo the same way,
				; the order of the doors should be the same.
			
				WriteByte(demoFile, d\open)
			
			Next
																
			demoDelayTime = MilliSecs()
		
			DemoTick = DemoTick + 1
		
		Else		
					
		EndIf
		
	EndIf
		
End Function

Function StopRecordingDemo()

	If demoFile Then
		CloseFile(demoFile)
		demoFile = 0
		
		CreateConsoleMsg("Demo saved to: " + demoSavePath + " Total Ticks: " + DemoTick)
		
		DemoTick = 0
		
	EndIf

End Function

Function LoadSavedDemos()

	CatchErrors("Uncaught (LoadSavedDemos)")
	SavedDemosAmount = 0
	If FileType(DemoPath)=1 Then RuntimeError "Can't create dir "+Chr(34)+DemoPath+Chr(34)
	If FileType(DemoPath)=0 Then CreateDir(DemoPath)
	myDir=ReadDir(DemoPath) 
	Repeat 
		file$=NextFile$(myDir) 
		If file$="" Then Exit 
		If FileType(DemoPath+"\"+file$) = 2 Then 
			If file <> "." And file <> ".." Then 
				If (FileType(DemoPath + file + "\demo.txt")>0) Then
					SavedDemosAmount = SavedDemosAmount + 1
				EndIf
			EndIf
		End If 
	Forever 
	CloseDir myDir 
	
	Dim SavedDemos$(SavedDemosAmount+1) 
	
	myDir=ReadDir(DemoPath) 
	i = 0
	Repeat 
		file$=NextFile$(myDir) 
		If file$="" Then Exit 
		If FileType(DemoPath+"\"+file$) = 2 Then 
			If file <> "." And file <> ".." Then 
				If (FileType(DemoPath + file + "\demo.txt")>0) Then
					SavedDemos(i) = file
					i=i+1
				EndIf
			EndIf
		End If 
	Forever 
	CloseDir myDir 
	
	;Dim SaveGameTime$(SaveGameAmount + 1)
	;Dim SaveGameDate$(SaveGameAmount + 1)
	;Dim SaveGameVersion$(SaveGameAmount + 1)
	;For i = 1 To SaveGameAmount
	;	DebugLog (SavePath + SaveGames(i - 1) + "\save.txt")
	;	Local f% = ReadFile(SavePath + SaveGames(i - 1) + "\save.txt")
	;	SaveGameTime(i - 1) = ReadString(f)
	;	SaveGameDate(i - 1) = ReadString(f)
	;	;Skip all data until the CompatibleVersion number
	;	ReadInt(f)
	;	For j = 0 To 5
	;		ReadFloat(f)
	;	Next
	;	ReadString(f)
	;	ReadFloat(f)
	;	ReadFloat(f)
	;	;End Skip
	;	SaveGameVersion(i - 1) = ReadString(f)
	;	
	;	CloseFile f
	;Next
	
	CatchErrors("LoadSavedDemos")

End Function

Function ReadDemo(path$)

	If Not demoFile Then
		demoFile = ReadFile(path)
		
		If Not demoFile Then
			CreateConsoleMsg("Could not open file: " + path )
			ConsoleOpen = True
		EndIf
		
		;Here we will read all the stuff we need before we start reading game state.
		RandomSeed$ = ReadString(demoFile)
		SeedDemoDirectly% = ReadByte(demoFile)				
		
		DemoDoorCount% = ReadByte(demoFile)	
		
		; This is a small optimization. The door count for any map is usually bigger than 255.
		; Normally, we would save it as an int, but we can save it into a byte.
		; We can pick an arbitrarily high number (100 in this case). There are usually no more than 280 doors per map.
		; When we read the door count value, we add 256 to it to get the true number of doors on the map.
		If DemoDoorCount < 100 Then 
			DemoDoorCount = DemoDoorCount + 256
		EndIf
		
		While Not(Eof(demoFile))
	
			Local d.Demos = New Demos
		
			d\tick = ReadInt(demoFile)
		
			d\gt = ReadInt(demoFile)
			
			d\px = ReadFloat(demoFile)
			d\py = ReadFloat(demoFile)
			d\pz = ReadFloat(demoFile)
			
			d\hx = ReadFloat(demoFile)
			d\hy = ReadFloat(demoFile)
			d\hz = ReadFloat(demoFile)
			
			d\pitch = ReadFloat(demoFile)
			d\yaw   = ReadFloat(demoFile)
			d\roll  = ReadFloat(demoFile)
	
			Local index% = 0
			Local ddoor.Doors
			For i% = 1 To DemoDoorCount ;ddoor.Doors = Each Doors
				
				Local open% = ReadByte(demoFile)
				d\demoDoors[index] = open
				;Local asdasd% = ReadByte(demoFile)
				index = index + 1											
				
			Next	
	
		Wend
								
		demo.Demos = First Demos
		prevDemo.Demos = demo
		lastDemo.Demos = Last Demos
		
		CloseFile(demoFile)
		demoFile = 0		
	;			
	;		EndIf			
	;	EndIf					
	EndIf
End Function

Function PlayDemo()
		
	If MilliSecs() - demoDelayTime >= 60 Then
	
		;If demo = Null Return
	
		PositionEntity(Collider, demo\px, demo\py, demo\pz)
		ResetEntity(Collider)
		
		PositionEntity(Head, demo\hx, demo\hy, demo\hz)
		ResetEntity(Head)
		
		RotateEntity(Collider, 0, demo\yaw, 0, 0)	
		RotateEntity(Camera, demo\pitch, demo\yaw, demo\roll, 0)
		
		Local index% = 0		
		For de.Doors = Each Doors
		
			Local currentOpen% = demo\demoDoors[index]
			Local oldOpen%     = prevDemo\demoDoors[index]
		
			;CreateConsoleMsg("Tick " + demo\tick + " Door " + index + " Open " + currentOpen)			
		
			If currentOpen <> oldOpen Then
			
				;de\open = demo\demoDoors[index]
				UseDoor(de.Doors, True)
			
			EndIf
			
			index = index + 1
		
			If index+1 > DemoDoorCount Then Exit
		
		Next
		
		demoDelayTime = MilliSecs()
						
		If demo\tick = lastDemo\tick Then
				
			demoUIOpen = True
			demoPaused = True
				
		Else
			
			prevDemo.Demos = demo.Demos
			demo.Demos = After demo	
			
			ShouldLerp = True
			
		EndIf
	
	Else
		
		;We try to lerp the player position and camera pitch/yaw/roll in between tick updates.
		;We will start by doing this only once between ticks.
		
		If ShouldLerp And (MilliSecs() - demoDelayTime >= 30) Then
			
			Local lpx# = Lerp(prevDemo\px, demo\px, 0.50)
			Local lpy# = Lerp(prevDemo\py, demo\py, 0.50)
			Local lpz# = Lerp(prevDemo\pz, demo\pz, 0.50)
			
			Local lhx# = Lerp(prevDemo\hx, demo\hx, 0.50)
			Local lhy# = Lerp(prevDemo\hy, demo\hy, 0.50)
			Local lhz# = Lerp(prevDemo\hz, demo\hz, 0.50)
			
			Local lpitch# = Lerp(prevDemo\pitch, demo\pitch, 0.50)
			Local lyaw#   = Lerp(prevDemo\yaw, demo\yaw, 0.50)
			Local lroll#  = Lerp(prevDemo\roll, demo\roll, 0.50)
			
			PositionEntity(Collider, lpx, lpy, lpz)
			ResetEntity(Collider)
			
			PositionEntity(Head, lhx, lhy, lhz)
			ResetEntity(Head)
			
			RotateEntity(Collider, 0, lyaw, 0, 0)
			RotateEntity(Camera, lpitch, lyaw, lroll, 0)
			
			
			ShouldLerp = False
			
		EndIf
	
	EndIf	


End Function

Function Lerp#(s#, e#, p#) 
	; We do a little lerping...
	Return (s + (e - s) * p)

End Function

Function EndOfFile()
End Function


































