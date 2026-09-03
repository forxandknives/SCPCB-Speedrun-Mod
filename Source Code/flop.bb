
Const SEED_IS_GOOD%     = 0
Const NO_SEED_PROVIDED  = 1
Const SEED_NOT_NUMBER   = 2
Const SEED_NOT_BEATABLE = 3
Const SEED_NOT_99       = 4
Const SEED_NOT_100      = 5
Const INVALID_SEED      = 6

Function FlopFunction()

	FPrint("hello flop")

	Local dllNumber% = NotFlopFunction()
	FPrint("dllNumber: " + Str(dllNumber))

	Local cpuReturn% = CPUGenerator(9780);2536653)
	FPrint("CPU: " + Str(cpuReturn))


End Function