/*
 * This file is part of eu.himeros_grcvallex_jar_1.0-SNAPSHOT
 *
 * Copyright (C) 2013 federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



Usage: java eu.himeros.GrcVaLex inFile.pml outFile.txt ctsFile.csv

inFile.pml: .pml file from the Ancient Greek and Latin Treebank (http://nlp.perseus.tufts.edu/syntax/treebank). Tests have been performed on the Odyssey treebank.

outFile.txt: result file with argument structures

ctsFile.csv: <TAB> separated file with two columns: 
	     #1 col: sentence-word reference: 
	     	they are the LM id at sentence level 
		and the LM id at word level in .pml files 
		(e.g. 2185541-1)
	     #2 col: citation 
	     	(e.g. Od.1.1)
