/*******************************************************************************
 *
 *	Copyright (C) 2008 Fujitsu Services Ltd.
 *
 *	Author: Nick Battle
 *
 *	This file is part of VDMJ.
 *
 *	VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

/*******************************************************************************
 * Modified and Merged to Json4VDMJ by Tomohiro Oda, 2015
 *
 *	Json4VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Json4VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Json4VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.github.json4vdmj;

import org.overturetool.vdmj.definitions.Definition;
import org.overturetool.vdmj.modules.ImportFromModule;
import org.overturetool.vdmj.modules.Module;
import org.overturetool.vdmj.modules.ModuleImports;
import org.overturetool.vdmj.syntax.ModuleReader;

import java.util.List;
import java.util.Vector;

/*
 * Created by tomohiro on 15/04/24.
 */
public class ModuleList extends org.overturetool.vdmj.modules.ModuleList {
    public int combineDefaults()
    {
        int rv = 0;

        if (!isEmpty())
        {
            Module def = new Module();

            // In VDM-10, we implicitly import all from the other
            // modules included with the flat specifications (if any).

            List<ImportFromModule> imports = new Vector<ImportFromModule>();

            for (Module m: this)
            {
                if (!m.isFlat)
                {
                    imports.add(ModuleReader.importAll(m.name));
                }
            }

            if (!imports.isEmpty())
            {
                def = new Module(def.name,
                        new ModuleImports(def.name, imports), null, def.defs);
            }            ModuleList named = new ModuleList();

            for (Module m: this)
            {
                if (m.isFlat)
                {
                    def.defs.addAll(m.defs);
                    def.files.add(m.name.location.file);
                    def.typechecked |= m.typechecked;
                }
                else
                {
                    named.add(m);
                }
            }

            clear();
            add(def);
            addAll(named);

            for (Definition d: def.defs)
            {
                if (!d.isTypeDefinition())
                {
                    d.markUsed();	// Mark top-level items as used
                }
            }
        }

        return rv;
    }
}
