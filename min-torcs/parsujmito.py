import re
import sys

variables = dict()
variablesFinal = dict()
rules = list()
outputVariables = dict()


array = []
with open(sys.argv[1], "r") as ins:
	for line in ins:
		array.append(line)


		
for i in range(len(array)):
	line = array[i]
	rules.append("IF ")
	match = re.search(r"=> (.*?)=(.*?) \(CF = (.*?)\)", line)
	outputVariableName = match.group(1)
	if outputVariableName not in outputVariables:
		outputVariables[outputVariableName] = list()
	
	match = re.findall(r"\((.*?)\)", line)
	for j in range(len(match)):
		varNameMatch = re.search(r"(.*?) in ", match[j]);
		if varNameMatch :
			varName = varNameMatch.group(1)
			if varName not in variables:
				variables[varName] = dict()
			counter = 0
			termName = outputVariableName + "_" + varName + "_" + str(i) + "_" + str(counter)
			while termName in variables[varName]:
				counter = counter + 1
				termName = outputVariableName + "_" + varName + "_" + str(i) + "_" + str(counter)
			variables[varName][termName] = re.search(r"\[(.*?)\]", match[j]).group(1)
			rules[-1] += varName + " IS " + termName
			if (j < len(match)-2):
				rules[-1] += " AND "
	match = re.search(r"=> (.*?)=(.*?) \(CF = (.*?)\)", line)
	
	if match:
		if match.group(2) not in outputVariables[outputVariableName]:
			outputVariables[outputVariableName].append(match.group(2))
		rules[-1] += " THEN " + match.group(1) + " IS " + match.group(2) + " WITH " + match.group(3) + ";"

for varialbeNameValue in variables:
	for termNameValue in variables[varialbeNameValue]:
		termDef = "TERM " + termNameValue + " :="
		values = re.search(r"(.+?), (.+?), (.+?), (.+)", variables[varialbeNameValue][termNameValue])
		whichGroup = 0
		groupWeights = [0, 1, 1, 0]
		for group in values.groups():
			if group != "inf" and group != "-inf":
				termDef += " (" + group + ", " + str(groupWeights[whichGroup]) + ")"
			whichGroup += 1
		termDef += ";"
		if varialbeNameValue not in variablesFinal:
			variablesFinal[varialbeNameValue] = list()
		variablesFinal[varialbeNameValue].append(termDef)

print("FUNCTION_BLOCK fb\n")

print("VAR_INPUT")	
for variable in variables:
	print("\t" + variable + " : REAL;")
print("END_VAR")
	
print("")
print("")

print("VAR_OUTPUT")	
for outputVariableName in outputVariables:
	print("\t" + outputVariableName + " : REAL;")
print("END_VAR")

print("")
print("")

for variableName in variablesFinal:
	print("FUZZIFY " + variableName)
	for term in variablesFinal[variableName]:
		print("\t" + term)
	print("END_FUZZIFY\n")


print("")

for outputVariableName in outputVariables:
	print("DEFUZZIFY " + outputVariableName)
	for termName in outputVariables[outputVariableName]:
		print("\tTERM " + termName + " := ???;")
	print("\n\tMETHOD : COGS;")
	print("\tDEFAULT := 0;")
	print("END_DEFUZZIFY\n")
	

print("RULEBLOCK No1")
print("\tAND : PROD;\n\tACT : MIN;\n\tACCU : MAX;\n")

ruleNumber = 0
for rule in rules:
	print("\tRULE " + str(ruleNumber) + " : " + rule)
	ruleNumber += 1

print("END_RULEBLOCK\n")
print("END_FUNCTION_BLOCK")
	
print("")


