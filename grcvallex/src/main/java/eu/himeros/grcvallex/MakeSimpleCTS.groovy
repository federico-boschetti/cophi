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

package eu.himeros.grvalex

def map=['0011-003':'Aj.', 
         '0012-001':'Il.', 
         '0012-002':'Od.', 
         '0020-001':'Op.', 
         '0020-003':'Sc.', 
         '0085-001':'Su.', 
         '0085-002':'Pe.', 
         '0085-003':'PV', 
         '0085-004':'Se.', 
         '0085-006':'Ch.']
map.each{key, value->
    println value
    def file=new File("cts/tlg${key}.xml")
    new File("cts/tlg${key}.csv").withWriter{out->
        file.eachLine{
            if((m = (it =~ /.+?grc1:([0-9]*\.?[0-9]+).+?>(.+?)<.+/))){
                out.println "${m[0][2]}\t${value}${m[0][1]}"}}}}
