; ============================================================
; BlitzNav v1.0
; Lightweight 3D Graph + A* for Blitz3D
; CC-BY - Dex Katja Dirks
; ============================================================

; ===== CONFIG =====
Const BlitzNav_Debug = False


; ===== GLOBALS =====
Global BlitzNav_DebugRoot = 0


; ===== TYPES =====

Type BlitzNav_Node
	Field ID
	
	; Optional reference position (not required for movement)
	Field X#,Y#,Z#
	
	; A* runtime
	Field GCost#
	Field HCost#
	Field FCost#
	Field Parent.BlitzNav_Node
	
	Field Opened
	Field Closed
End Type


Type BlitzNav_Edge
	Field FromNode.BlitzNav_Node
	Field ToNode.BlitzNav_Node
	
	Field StartX#,StartY#,StartZ#
	Field EndX#,EndY#,EndZ#
	
	Field Cost#
End Type


; ============================================================
; CORE
; ============================================================

Function BlitzNav_Clear()
	
	; Delete nodes
	For n.BlitzNav_Node = Each BlitzNav_Node
		If BlitzNav_Debug
			If n\DebugMesh <> 0 Then FreeEntity n\DebugMesh
		EndIf
		Delete n
	Next
	
	; Delete edges
	For e.BlitzNav_Edge = Each BlitzNav_Edge
		Delete e
	Next
	
	; Delete debug root
	If BlitzNav_Debug
		If BlitzNav_DebugRoot <> 0
			FreeEntity BlitzNav_DebugRoot
			BlitzNav_DebugRoot = 0
		EndIf
	EndIf
	
End Function



Function BlitzNav_AddNode.BlitzNav_Node(x#,y#,z#)
	
	n.BlitzNav_Node = New BlitzNav_Node
	
	n\ID = MilliSecs()
	n\X = x
	n\Y = y
	n\Z = z
	
	n\GCost = 0
	n\HCost = 0
	n\FCost = 0
	n\Parent = Null
	n\Opened = False
	n\Closed = False
	
	If BlitzNav_Debug
		If BlitzNav_DebugRoot = 0
			BlitzNav_DebugRoot = CreatePivot()
		EndIf
		
		n\DebugMesh = CreateCube()
		ScaleEntity n\DebugMesh,2,2,2
		PositionEntity n\DebugMesh,x,y,z
		EntityColor n\DebugMesh,0,255,0
		EntityParent n\DebugMesh,BlitzNav_DebugRoot
	EndIf
	
	Return n
	
End Function



Function BlitzNav_AddEdge(nodeA.BlitzNav_Node, nodeB.BlitzNav_Node, _
	startX#,startY#,startZ#, _
	endX#,endY#,endZ#, _
	cost# = -1, bidirectional = True)
	
	If nodeA = Null Or nodeB = Null Then Return
	
	If cost < 0
		dx# = startX - endX
		dy# = startY - endY
		dz# = startZ - endZ
		cost# = Sqr(dx*dx + dy*dy + dz*dz)
	EndIf
	
	e.BlitzNav_Edge = New BlitzNav_Edge
	e\FromNode = nodeA
	e\ToNode = nodeB
	
	e\StartX = startX
	e\StartY = startY
	e\StartZ = startZ
	
	e\EndX = endX
	e\EndY = endY
	e\EndZ = endZ
	
	e\Cost = cost
	
	If bidirectional = True
		
		e2.BlitzNav_Edge = New BlitzNav_Edge
		e2\FromNode = nodeB
		e2\ToNode = nodeA
		
		; reverse physical direction
		e2\StartX = endX
		e2\StartY = endY
		e2\StartZ = endZ
		
		e2\EndX = startX
		e2\EndY = startY
		e2\EndZ = startZ
		
		e2\Cost = cost
		
	EndIf
	
End Function

Function BlitzNav_Heuristic#(a.BlitzNav_Node, b.BlitzNav_Node)
	
	dx# = a\X - b\X
	dy# = a\Y - b\Y
	dz# = a\Z - b\Z
	
	Return Sqr(dx*dx + dy*dy + dz*dz)
	
End Function

Function BlitzNav_GetEdge.BlitzNav_Edge(nodeA.BlitzNav_Node, nodeB.BlitzNav_Node)
	
	For e.BlitzNav_Edge = Each BlitzNav_Edge
		If e\FromNode = nodeA And e\ToNode = nodeB
			Return e
		EndIf
	Next
	
	Return Null
	
End Function

Function BlitzNav_ResetRuntime()
	
	For n.BlitzNav_Node = Each BlitzNav_Node
		n\GCost = 0
		n\HCost = 0
		n\FCost = 0
		n\Parent = Null
		n\Opened = False
		n\Closed = False
		
		If BlitzNav_Debug
			If n\DebugMesh <> 0
				EntityColor n\DebugMesh,0,255,0
			EndIf
		EndIf
		
	Next
	
End Function

Function BlitzNav_FindPath(start.BlitzNav_Node, goal.BlitzNav_Node)
	
	If start = Null Or goal = Null Then Return False
	
	BlitzNav_ResetRuntime()
	
	start\Opened = True
	start\GCost = 0
	start\HCost = BlitzNav_Heuristic(start,goal)
	start\FCost = start\HCost
	
	While True
		
		current.BlitzNav_Node = Null
		lowest# = 999999999
		
		; Find lowest F in open set
		For n.BlitzNav_Node = Each BlitzNav_Node
			If n\Opened = True And n\Closed = False
				
				If current = Null
					current = n
					lowest = n\FCost
				Else
					If n\FCost < lowest
						current = n
						lowest = n\FCost
					EndIf
				EndIf
				
			EndIf
		Next
		
		If current = Null
			Return False ; no path
		EndIf
		
		If current = goal
			If BlitzNav_Debug
				BlitzNav_DebugHighlightPath(goal)
			EndIf
			Return True ; path found
		EndIf
		
		current\Closed = True
		
		; Check neighbors
		For e.BlitzNav_Edge = Each BlitzNav_Edge
			If e\FromNode = current
				
				neighbor.BlitzNav_Node = e\ToNode
				
				If neighbor\Closed = False
					
					tentativeG# = current\GCost + e\Cost
					
					If neighbor\Opened = False Or tentativeG < neighbor\GCost
						
						neighbor\Parent = current
						neighbor\GCost = tentativeG
						neighbor\HCost = BlitzNav_Heuristic(neighbor,goal)
						neighbor\FCost = neighbor\GCost + neighbor\HCost
						
						neighbor\Opened = True
						
					EndIf
					
				EndIf
				
			EndIf
		Next
		
	Wend
	
End Function



Function BlitzNav_DebugHighlightPath(goal.BlitzNav_Node)
	
	current.BlitzNav_Node = goal
	
	While current <> Null
		If current\DebugMesh <> 0
			EntityColor current\DebugMesh,255,0,0
		EndIf
		current = current\Parent
	Wend
	
End Function