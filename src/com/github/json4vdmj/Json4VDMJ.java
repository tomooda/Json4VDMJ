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

import net.arnx.jsonic.JSON;
import org.overturetool.vdmj.ExitStatus;
import org.overturetool.vdmj.Settings;
import org.overturetool.vdmj.VDMJ;
import org.overturetool.vdmj.lex.Dialect;
import org.overturetool.vdmj.lex.LexLocation;
import org.overturetool.vdmj.lex.LexTokenReader;
import org.overturetool.vdmj.messages.Console;
import org.overturetool.vdmj.messages.InternalException;
import org.overturetool.vdmj.runtime.ContextException;
import org.overturetool.vdmj.runtime.ModuleInterpreter;
import org.overturetool.vdmj.syntax.ModuleReader;
import org.overturetool.vdmj.typechecker.ModuleTypeChecker;
import org.overturetool.vdmj.typechecker.TypeChecker;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Json4VDMJ extends VDMJ {
    private ModuleList modules = new ModuleList();
    public Json4VDMJ() {
        Settings.dialect = Dialect.VDM_SL;
    }

    /**
     * @see org.overturetool.vdmj.VDMJ#parse(java.util.List)
     */

    @Override
    public ExitStatus parse(List<File> files)
    {
        modules.clear();
        LexLocation.resetLocations();
        int perrs = 0;

        for (File file: files)
        {
            ModuleReader reader = null;

            try
            {
                LexTokenReader ltr = new LexTokenReader(file, Settings.dialect, filecharset);
                reader = new ModuleReader(ltr);
                modules.addAll(reader.readModules());
            }
            catch (InternalException e)
            {
                perrs++;
                printError(e.toString());
            }
            catch (Throwable e)
            {
                printError(e.toString());
                perrs++;
            }

            if (reader != null && reader.getErrorCount() > 0)
            {
                StringWriter message = new StringWriter();
                perrs += reader.getErrorCount();
                reader.printErrors(new PrintWriter(message));
                printError(message.toString());
            }

        }
        perrs += modules.combineDefaults();
        return perrs == 0 ? ExitStatus.EXIT_OK : ExitStatus.EXIT_ERRORS;
    }

    /**
     * @see org.overturetool.vdmj.VDMJ#typeCheck()
     */

    @Override
    public ExitStatus typeCheck()
    {
        int terrs = 0;
        try
        {
            TypeChecker typeChecker = new ModuleTypeChecker(modules);
            typeChecker.typeCheck();
        }
        catch (InternalException e)
        {
            printError(e.toString());
        }
        catch (Throwable e)
        {
            printError(e.toString());
            terrs++;
        }
        terrs += TypeChecker.getErrorCount();

        if (terrs > 0)
        {
            StringWriter message = new StringWriter();
            TypeChecker.printErrors(new PrintWriter(message));
            printError(message.toString());
        }

        return terrs == 0 ? ExitStatus.EXIT_OK : ExitStatus.EXIT_ERRORS;
    }

    /**
     * @see org.overturetool.vdmj.VDMJ#interpret(List, String)
     */

    @Override
    protected ExitStatus interpret(List<File> filenames, String defaultName)
    {
        ModuleInterpreter interpreter = null;

        try
        {
            interpreter = getInterpreter();
            interpreter.init(null);
            if (filenames.size() > 0) {
                this.printOK(filenames.get(0));
            }

            if (defaultName != null)
            {
                interpreter.setDefaultName(defaultName);
            }
        }
        catch (ContextException e)
        {
            printError("Initialization: " + e);
            return ExitStatus.EXIT_ERRORS;
        }
        catch (Exception e)
        {
            printError("Initialization: " + e.getMessage());
            return ExitStatus.EXIT_ERRORS;
        }

        try
        {
                CommandReader reader = new CommandReader(interpreter);
                return reader.run(filenames);
        }
        catch (ContextException e)
        {
            printError("Execution: " + e);
        }
        catch (Exception e)
        {
            printError("Execution: " + e);
        }

        return ExitStatus.EXIT_ERRORS;
    }

    @Override
    public ModuleInterpreter getInterpreter() throws Exception
    {
        ModuleInterpreter interpreter = new ModuleInterpreter(modules);
        return interpreter;
    }



    protected void printOK(Object result) {
        Object[] array = new Object[2];
        array[0] = true;
        array[1] = result;
        System.out.println(JSON.encode(array));
    }
    protected void printError(String message) {
        Object[] array = new Object[2];
        array[0] = false;
        array[1] = message;
        System.out.println(JSON.encode(array));
    }

    public static void main(String[] args) {
        Json4VDMJ json4vdmj = new Json4VDMJ();
        ExitStatus status = null;

        List<File> filenames = new ArrayList<File>(0);
        Console.err = new NullRedirector();
        json4vdmj.interpret(filenames, null);
        do
        {
            if (filenames.isEmpty()) {
                status = json4vdmj.interpret(filenames, null);
                if (status == ExitStatus.EXIT_ERRORS) {
                    filenames.clear();
                    json4vdmj = new Json4VDMJ();
                }
            } else {
                status = json4vdmj.parse(filenames);

                if (status == ExitStatus.EXIT_OK)
                {
                    status = json4vdmj.typeCheck();

                    if (status == ExitStatus.EXIT_OK)
                    {
                        status = json4vdmj.interpret(filenames, null);
                        if (status == ExitStatus.EXIT_ERRORS) {
                            filenames.clear();
                            json4vdmj = new Json4VDMJ();
                        }
                    }
                    else
                    {
                        filenames.clear();
                        json4vdmj = new Json4VDMJ();
                    }
                } else {
                    filenames.clear();
                    json4vdmj = new Json4VDMJ();
                }
            }
        }
        while (true);
    }
}

